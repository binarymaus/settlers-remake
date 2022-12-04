package go.graphics.swing.vulkan.pipeline;

import go.graphics.swing.vulkan.VulkanDescriptorPool;
import go.graphics.swing.vulkan.VulkanDescriptorSetLayout;
import go.graphics.swing.vulkan.VulkanDrawContext;
import go.graphics.swing.vulkan.VulkanUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

import go.graphics.EPrimitiveType;

import static org.lwjgl.vulkan.VK10.*;

public abstract class VulkanPipeline {
	private long pipeline = VK_NULL_HANDLE;
	protected long pipelineLayout = VK_NULL_HANDLE;
	protected VulkanDrawContext dc;

	protected VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo; // MUST NOT BE ALLOCATED FROM STACK
	protected final LongBuffer setLayouts;
	protected long descSet = 0;
	protected ByteBuffer writtenPushConstantBfr;
	protected ByteBuffer pushConstantBfr;
	private VulkanDescriptorSetLayout ownDescriptorSetLayout = null;

	private long[] writtenBfrs;
	private long[] writtenDescSets;

	public VulkanPipeline(MemoryStack stack,
						  VulkanDrawContext dc,
						  String prefix,
						  VulkanDescriptorPool descPool,
						  long renderPass,
						  int primitive,
						  VulkanDescriptorSetLayout... additionalDescSetLayout) {
		this.dc = dc;

		long vertShader = VK_NULL_HANDLE;
		long fragShader = VK_NULL_HANDLE;

		setLayouts = BufferUtils.createLongBuffer(1 + additionalDescSetLayout.length);
		setLayouts.put(0);
		for(VulkanDescriptorSetLayout additionalSetLayout : additionalDescSetLayout) {
			setLayouts.put(additionalSetLayout.getLayout());
		}
		setLayouts.rewind();

		try {
			vertShader = VulkanUtils.createShaderModule(stack, dc.getDevice(), prefix + ".vert.spv");
			fragShader = VulkanUtils.createShaderModule(stack, dc.getDevice(), prefix + ".frag.spv");

			int pushConstantSize = getPushConstantSize();

			VkPushConstantRange.Buffer pushConstantRanges = VkPushConstantRange.create(1);
			pushConstantRanges.get(0).set(VK_SHADER_STAGE_ALL_GRAPHICS, 0, pushConstantSize);

			pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.create()
					.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
					.pPushConstantRanges(pushConstantRanges);

			if(pushConstantSize == 4) {
				pushConstantBfr = null;
				writtenPushConstantBfr = null;
			} else {
				pushConstantBfr = BufferUtils.createByteBuffer(pushConstantSize - 4);
				writtenPushConstantBfr = BufferUtils.createByteBuffer(pushConstantSize - 4);
			}

			ownDescriptorSetLayout = new VulkanDescriptorSetLayout(dc.getDevice(), getDescriptorSetLayoutBindings());
			setLayouts.put(0, ownDescriptorSetLayout.getLayout());
			pipelineLayoutCreateInfo.pSetLayouts(setLayouts);

			VkPipelineVertexInputStateCreateInfo inputStateCreateInfo = getVertexInputState(stack);
			writtenBfrs = new long[inputStateCreateInfo.vertexBindingDescriptionCount()];
			Arrays.fill(writtenBfrs, VK_NULL_HANDLE);

			pipelineLayout = VulkanUtils.createPipelineLayout(stack, dc.getDevice(), pipelineLayoutCreateInfo);
			pipeline = VulkanUtils.createPipeline(stack, dc.getDevice(), primitive, pipelineLayout, renderPass, vertShader, fragShader, inputStateCreateInfo, dc.getMaxManagedQuads());

			descSet = descPool.createNewSet(ownDescriptorSetLayout);

			writtenDescSets = new long[1];
			writtenDescSets[0] = descSet;
		} finally {
			if(vertShader != VK_NULL_HANDLE) VK10.vkDestroyShaderModule(dc.getDevice(), vertShader, null);
			if(fragShader != VK_NULL_HANDLE) VK10.vkDestroyShaderModule(dc.getDevice(), fragShader, null);

			if(pipeline == VK_NULL_HANDLE) destroy();
		}
	}

	protected abstract int getPushConstantSize();
	protected abstract VkPipelineVertexInputStateCreateInfo getVertexInputState(MemoryStack stack);
	protected abstract VkDescriptorSetLayoutBinding.Buffer getDescriptorSetLayoutBindings();

	public void destroy() {
		if(pipeline != VK_NULL_HANDLE) vkDestroyPipeline(dc.getDevice(), pipeline, null);
		if(pipelineLayout != VK_NULL_HANDLE) vkDestroyPipelineLayout(dc.getDevice(), pipelineLayout, null);
		if(ownDescriptorSetLayout != null) ownDescriptorSetLayout.destroy();
	}

	private VkViewport.Buffer viewportUpdate = VkViewport.create(1);
	private VkRect2D.Buffer scissorUpdate = VkRect2D.create(1);
	private long frame = -1;

	public void resize(int width, int height) {
		viewportUpdate.width(width).height(height).minDepth(0).maxDepth(1);
		scissorUpdate.extent().width(width).height(height);
	}

	public void bind(VkCommandBuffer commandBuffer, long frame) {
		vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
		vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, writtenDescSets, null);

		if(writtenBfrs.length>0 && writtenBfrs[0] != VK_NULL_HANDLE) {
			vkCmdBindVertexBuffers(commandBuffer, 0, writtenBfrs, new long[writtenBfrs.length]);
		}

		if(this.frame != frame) {
			vkCmdSetViewport(commandBuffer, 0, viewportUpdate);
			vkCmdSetScissor(commandBuffer, 0, scissorUpdate);
			this.frame = frame;
		}

		pushGlobalAttr(commandBuffer);
		if(writtenPushConstantBfr != null) {
			vkCmdPushConstants(commandBuffer, pipelineLayout, VK_SHADER_STAGE_ALL_GRAPHICS, 4, writtenPushConstantBfr);
		}
	}

	public void pushGlobalAttr(VkCommandBuffer commandBuffer) {
		vkCmdPushConstants(commandBuffer, pipelineLayout, VK_SHADER_STAGE_ALL_GRAPHICS, 0, new int[] {dc.getGlobalAttrIndex()});
	}

	public void pushConstants(VkCommandBuffer commandBuffer) {
		if(pushConstantBfr == null) throw new IllegalStateException("This pipeline has no special push constants!");

		if(pushConstantBfr.compareTo(writtenPushConstantBfr) != 0) {
			vkCmdPushConstants(commandBuffer, pipelineLayout, VK_SHADER_STAGE_ALL_GRAPHICS, 4, pushConstantBfr);
			writtenPushConstantBfr.put(pushConstantBfr);
			writtenPushConstantBfr.rewind();
			pushConstantBfr.rewind();
		}
	}

	public void update(VkWriteDescriptorSet.Buffer write) {
		int pos = write.position();
		int count = write.remaining();
		for(int i = 0; i != count; i++) write.get(i+pos).dstSet(descSet);

		vkUpdateDescriptorSets(dc.getDevice(), write, null);
	}

	public void bindVertexBuffers(VkCommandBuffer commandBuffer, long... bfrs) {
		for(int i = 0; i != bfrs.length; i++) {
			if(writtenBfrs[i] != bfrs[i]) {
				vkCmdBindVertexBuffers(commandBuffer, 0, bfrs, new long[bfrs.length]);
				writtenBfrs = bfrs;
				return;
			}
		}
	}

	public void bindDescSets(VkCommandBuffer commandBuffer, long... descSets) {
		long[] newWrittenDescSets = new long[1 + descSets.length];
		newWrittenDescSets[0] = descSet;
		System.arraycopy(descSets, 0, newWrittenDescSets, 1, descSets.length);

		if(!Arrays.equals(writtenDescSets, newWrittenDescSets)) {
			writtenDescSets = newWrittenDescSets;
			vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, writtenDescSets, null);
		}
	}

	public static class BackgroundPipeline extends VulkanPipeline {

		@Override
		protected int getPushConstantSize() {
			return 4;
		}

		@Override
		protected VkDescriptorSetLayoutBinding.Buffer getDescriptorSetLayoutBindings() {
			VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(2);
			bindings.get(0).set(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK_SHADER_STAGE_ALL_GRAPHICS, null);
			bindings.get(1).set(1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK_SHADER_STAGE_ALL_GRAPHICS, null); // height matrix
			return bindings;
		}

		@Override
		protected VkPipelineVertexInputStateCreateInfo getVertexInputState(MemoryStack stack) {
			VkVertexInputBindingDescription.Buffer bindings = VkVertexInputBindingDescription.calloc(1, stack);
			bindings.get(0).set(0, 6*4, VK_VERTEX_INPUT_RATE_VERTEX);

			VkVertexInputAttributeDescription.Buffer attributes = VkVertexInputAttributeDescription.calloc(3, stack);
			attributes.get(0).set(0, 0, VK_FORMAT_R32G32B32_SFLOAT, 0);
			attributes.get(1).set(1, 0, VK_FORMAT_R32G32_SFLOAT, 3*4);
			attributes.get(2).set(2, 0, VK_FORMAT_R32_SFLOAT, 5*4);

			return VkPipelineVertexInputStateCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
					.pVertexAttributeDescriptions(attributes)
					.pVertexBindingDescriptions(bindings);
		}

		public BackgroundPipeline(MemoryStack stack, VulkanDrawContext dc, VulkanDescriptorPool descPool, long renderPass) {
			super(stack, dc, "background", descPool, renderPass, EPrimitiveType.Triangle, dc.textureDescLayout);
		}
	}

	public static class UnifiedPipeline extends VulkanPipeline {

		@Override
		protected int getPushConstantSize() {
			return (2*4+2+4)*4;
		}

		@Override
		protected VkDescriptorSetLayoutBinding.Buffer getDescriptorSetLayoutBindings() {
			VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(2);
			bindings.get(0).set(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK_SHADER_STAGE_ALL_GRAPHICS, null);
			bindings.get(1).set(1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK_SHADER_STAGE_ALL_GRAPHICS, null); // shadow depth
			return bindings;
		}

		@Override
		protected VkPipelineVertexInputStateCreateInfo getVertexInputState(MemoryStack stack) {
			VkVertexInputBindingDescription.Buffer bindings = VkVertexInputBindingDescription.calloc(2, stack);
			bindings.get(0).set(0, 4*4, VK_VERTEX_INPUT_RATE_VERTEX);
			bindings.get(1).set(1, 2*4, VK_VERTEX_INPUT_RATE_VERTEX);

			VkVertexInputAttributeDescription.Buffer attributes = VkVertexInputAttributeDescription.calloc(3, stack);
			attributes.get(0).set(0, 0, VK_FORMAT_R32G32_SFLOAT, 0);
			attributes.get(1).set(1, 0, VK_FORMAT_R32G32_SFLOAT, 2*4);
			attributes.get(2).set(2, 1, VK_FORMAT_R32G32_SFLOAT, 0);

			return VkPipelineVertexInputStateCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
					.pVertexAttributeDescriptions(attributes)
					.pVertexBindingDescriptions(bindings);
		}

		public static UnifiedPipeline createLine(MemoryStack stack, VulkanDrawContext dc, VulkanDescriptorPool descPool, long renderPass) {
			return new UnifiedPipeline(stack, dc, descPool, renderPass, EPrimitiveType.Line);
		}

		public static UnifiedPipeline createQuad(MemoryStack stack, VulkanDrawContext dc, VulkanDescriptorPool descPool, long renderPass) {
			return new UnifiedPipeline(stack, dc, descPool, renderPass, EPrimitiveType.Quad);
		}

		public UnifiedPipeline(MemoryStack stack, VulkanDrawContext dc, VulkanDescriptorPool descPool, long renderPass, int primitive) {
			super(stack, dc, "unified", descPool, renderPass, primitive, dc.textureDescLayout);
		}
	}

	public static class UnifiedArrayPipeline extends VulkanPipeline {

		@Override
		protected int getPushConstantSize() {
			return 4;
		}

		@Override
		protected VkPipelineVertexInputStateCreateInfo getVertexInputState(MemoryStack stack) {
			VkVertexInputBindingDescription.Buffer bindings = VkVertexInputBindingDescription.calloc(3, stack);
			bindings.get(0).set(0, 4*4, VK_VERTEX_INPUT_RATE_VERTEX);
			bindings.get(1).set(1, 2*4, VK_VERTEX_INPUT_RATE_VERTEX);
			bindings.get(2).set(2, 4*4, VK_VERTEX_INPUT_RATE_INSTANCE);

			VkVertexInputAttributeDescription.Buffer attributes = VkVertexInputAttributeDescription.calloc(5, stack);
			attributes.get(0).set(0, 0, VK_FORMAT_R32G32_SFLOAT, 0);
			attributes.get(1).set(1, 0, VK_FORMAT_R32G32_SFLOAT, 2*4);
			attributes.get(2).set(2, 1, VK_FORMAT_R32G32_SFLOAT, 0);
			attributes.get(3).set(3, 2, VK_FORMAT_R32G32B32A32_SFLOAT, 0);
			attributes.get(4).set(4, 2, VK_FORMAT_R32G32B32A32_SFLOAT, 4*4*100);

			return VkPipelineVertexInputStateCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
					.pVertexAttributeDescriptions(attributes)
					.pVertexBindingDescriptions(bindings);
		}

		@Override
		protected VkDescriptorSetLayoutBinding.Buffer getDescriptorSetLayoutBindings() {
			VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(2);
			bindings.get(0).set(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK_SHADER_STAGE_ALL_GRAPHICS, null);
			bindings.get(1).set(1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK_SHADER_STAGE_ALL_GRAPHICS, null); // shadow depth
			return bindings;
		}

		public UnifiedArrayPipeline(MemoryStack stack, VulkanDrawContext dc, VulkanDescriptorPool descPool, long renderPass) {
			super(stack, dc, "unified-array", descPool, renderPass, EPrimitiveType.Quad, dc.textureDescLayout);
		}
	}

    public static class UnifiedMultiPipeline extends VulkanPipeline {

		@Override
		protected int getPushConstantSize() {
			return 4;
		}

		@Override
		protected VkPipelineVertexInputStateCreateInfo getVertexInputState(MemoryStack stack) {
			VkVertexInputBindingDescription.Buffer bindings = VkVertexInputBindingDescription.calloc(1, stack);
			bindings.get(0).set(0, 12*4, VK_VERTEX_INPUT_RATE_INSTANCE);

			VkVertexInputAttributeDescription.Buffer attributes = VkVertexInputAttributeDescription.calloc(4, stack);
			attributes.get(0).set(0, 0, VK_FORMAT_R32G32B32_SFLOAT, 0);
			attributes.get(1).set(1, 0, VK_FORMAT_R32G32_SFLOAT, 3*4);
			attributes.get(2).set(2, 0, VK_FORMAT_R32G32B32A32_SFLOAT, 5*4);
			attributes.get(3).set(3, 0, VK_FORMAT_R32G32B32_SFLOAT, 9*4);

			return VkPipelineVertexInputStateCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
					.pVertexAttributeDescriptions(attributes)
					.pVertexBindingDescriptions(bindings);
		}

		@Override
		protected VkDescriptorSetLayoutBinding.Buffer getDescriptorSetLayoutBindings() {
			VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(2);
			bindings.get(0).set(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK_SHADER_STAGE_ALL_GRAPHICS, null);
			bindings.get(1).set(1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK_SHADER_STAGE_ALL_GRAPHICS, null); // shadow depth
			return bindings;
		}

		public UnifiedMultiPipeline(MemoryStack stack, VulkanDrawContext dc, VulkanDescriptorPool descPool, long renderPass) {
			super(stack, dc, "unified-multi", descPool, renderPass, EPrimitiveType.Quad, dc.textureDescLayout, dc.multiDescLayout);
		}
	}
}
