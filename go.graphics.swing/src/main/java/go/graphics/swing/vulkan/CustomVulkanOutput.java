package go.graphics.swing.vulkan;

import go.graphics.swing.vulkan.memory.VulkanImage;
import java.awt.Dimension;
import java.nio.LongBuffer;
import java.util.function.BiFunction;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSubmitInfo;
import static org.lwjgl.vulkan.VK10.*;

public class CustomVulkanOutput extends AbstractVulkanOutput {

	private long framebuffer;
	private VulkanImage framebufferImage;
	private Dimension fbDimension;

	@Override
	BiFunction<VkQueueFamilyProperties, Integer, Boolean> getPresentQueueCond(VkPhysicalDevice physicalDevice) {
		return (queue, index) -> false; // we don't present on queues
	}

	@Override
	void init(VulkanDrawContext dc) {
		super.init(dc);

		try(MemoryStack stack = MemoryStack.stackPush()) {
			dc.regenerateRenderPass(stack, EVulkanImageType.FRAMEBUFFER_RGBA8.getFormat());
		}
	}

	@Override
	void destroy() {
		super.destroy();

		if(framebuffer != 0) {
			vkDestroyFramebuffer(dc.getDevice(), framebuffer, null);
		}
	}

	@Override
	Dimension resize(Dimension preferredSize) {
		fbDimension = preferredSize;

		if(framebufferImage != null) {
			framebufferImage.destroy();
			framebufferImage = null;
		}
		framebufferImage = dc.memoryManager.createImage(preferredSize.width, preferredSize.height, EVulkanImageType.FRAMEBUFFER_RGBA8);

		return preferredSize;
	}

	@Override
	void createFramebuffers(VulkanImage depthImage) {
		LongBuffer attachments = BufferUtils.createLongBuffer(2);
		attachments.put(0, framebufferImage.getImageView());
		attachments.put(1, depthImage.getImageView());

		VkFramebufferCreateInfo fbCreateInfo = VkFramebufferCreateInfo.create();
		fbCreateInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
				.renderPass(dc.getRenderPass())
				.layers(1)
				.width(fbDimension.width)
				.height(fbDimension.height)
				.pAttachments(attachments);

		LongBuffer fbPtr = BufferUtils.createLongBuffer(1);
		if(vkCreateFramebuffer(dc.getDevice(), fbCreateInfo, null, fbPtr) < 0) {
			throw new Error("Could not create framebuffer!");
		}
		framebuffer = fbPtr.get(0);
	}

	@Override
	boolean startFrame() {
		return true;
	}

	@Override
	void endFrame(boolean wait) {
		vkQueueWaitIdle(dc.queueManager.getGraphicsQueue());
	}

	@Override
	VulkanImage getFramebufferImage() {
		return framebufferImage;
	}

	@Override
	long getFramebuffer() {
		return framebuffer;
	}

	public Dimension getFramebufferSize() {
		return fbDimension;
	}

	@Override
	public boolean needsPresentQueue() {
		return false;
	}

	@Override
	void configureDrawCommand(MemoryStack stack, VkSubmitInfo graphSubmitInfo) {
	}
}
