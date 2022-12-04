package go.graphics.swing.vulkan.pipeline;

import go.graphics.swing.vulkan.memory.AbstractVulkanBuffer;
import go.graphics.swing.vulkan.VulkanDescriptorPool;
import go.graphics.swing.vulkan.VulkanDrawContext;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanPipelineManager {

	private final Map<EVulkanPipelineType, VulkanPipeline> allPipelines = new EnumMap<>(EVulkanPipelineType.class);

	private final VkCommandBuffer graphCommandBuffer;
	private final VulkanDrawContext dc;

	public VulkanPipelineManager(MemoryStack stack,
								 VulkanDrawContext dc,
								 VulkanDescriptorPool universalDescPool,
								 long renderPass,
								 VkCommandBuffer graphCommandBuffer) {
		for(EVulkanPipelineType type : EVulkanPipelineType.values()) {
			allPipelines.put(type, type.create(stack, dc, universalDescPool, renderPass));
		}

		this.dc = dc;
		this.graphCommandBuffer = graphCommandBuffer;
	}

	public void installUniformBuffers(AbstractVulkanBuffer globalUniformBuffer, AbstractVulkanBuffer backgroundUniformBfr, AbstractVulkanBuffer unifiedUniformBfr) {
		installUniformBuffer(globalUniformBuffer, 0);

		for (EVulkanPipelineType type : EVulkanPipelineType.UNIFIED_TYPES) {
			installUniformBuffer(unifiedUniformBfr, 1, 0, allPipelines.get(type));
		}
		installUniformBuffer(backgroundUniformBfr, 1, 0, allPipelines.get(EVulkanPipelineType.BACKGROUND));
	}

	private final VkDescriptorBufferInfo.Buffer install_uniform_buffer = VkDescriptorBufferInfo.create(1).range(VK_WHOLE_SIZE);

	private final VkWriteDescriptorSet.Buffer install_uniform_buffer_write = VkWriteDescriptorSet.create(1)
			.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
			.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.pBufferInfo(install_uniform_buffer)
			.descriptorCount(1)
			.dstArrayElement(0);

	private void installUniformBuffer(AbstractVulkanBuffer buffer, int binding, int index, VulkanPipeline pipeline) {
		install_uniform_buffer_write.dstBinding(binding);
		install_uniform_buffer_write.dstArrayElement(index);
		install_uniform_buffer.buffer(buffer.getBufferIdVk());

		pipeline.update(install_uniform_buffer_write);

	}

	private void installUniformBuffer(AbstractVulkanBuffer buffer, int binding) {
		install_uniform_buffer_write.dstBinding(binding);
		install_uniform_buffer.buffer(buffer.getBufferIdVk());

		for (VulkanPipeline pipeline : allPipelines.values()) {
			pipeline.update(install_uniform_buffer_write);
		}

	}

	public void destroy() {
		for (VulkanPipeline vulkanPipeline : allPipelines.values()) {
			vulkanPipeline.destroy();
		}
	}

	public void resize(int fbWidth, int fbHeight) {
		for (VulkanPipeline pipeline : allPipelines.values()) {
			pipeline.resize(fbWidth, fbHeight);
		}
	}

	private VulkanPipeline lastPipeline = null;

	public void bind(EVulkanPipelineType pipelineType) {
		VulkanPipeline pipeline = allPipelines.get(pipelineType);

		if(pipeline != lastPipeline) {
			pipeline.bind(graphCommandBuffer, dc.getFrameIndex());
			lastPipeline = pipeline;
		}
	}

	public void clearLastPipeline() {
		lastPipeline = null;
	}

	public boolean isPipelineBound() {
		return lastPipeline != null;
	}

	public void bindVertexBuffers(long... bufferIdVk) {
		lastPipeline.bindVertexBuffers(graphCommandBuffer, bufferIdVk);
	}

	public void bindDescSets(long... descSets) {
		lastPipeline.bindDescSets(graphCommandBuffer, descSets);
	}

	public ByteBuffer getPushConstantBfr() {
		return lastPipeline.pushConstantBfr;
	}

	public void pushConstants() {
		lastPipeline.pushConstants(graphCommandBuffer);
	}

	public void pushGlobalAttr() {
		lastPipeline.pushGlobalAttr(graphCommandBuffer);
	}
}
