package go.graphics.swing.vulkan.memory;

import go.graphics.swing.vulkan.EVulkanImageType;
import go.graphics.swing.vulkan.VulkanDrawContext;
import go.graphics.swing.vulkan.VulkanUtils;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanImage {
	private final long image;
	private final long imageView;
	private final long allocation;
	private final VulkanMemoryManager memoryManager;
	private int imageLayout;
	private final int aspect;
	private final VulkanDrawContext dc;

	public VulkanImage(VulkanDrawContext dc, VulkanMemoryManager manager, long image, long allocation, EVulkanImageType imageType) {
		this(dc, manager, image, allocation, imageType.getFormat(), imageType.getAspect());
	}

	public VulkanImage(VulkanDrawContext dc, VulkanMemoryManager manager, long image, long allocation, int format, int aspect) {
		this.dc = dc;
		this.image = image;
		this.imageView = VulkanUtils.createImageView(dc.getDevice(), image, format, aspect);
		this.allocation = allocation;
		this.memoryManager = manager;
		this.aspect = aspect;

		imageLayout = VK_IMAGE_LAYOUT_UNDEFINED;
	}

	public long getImage() {
		return image;
	}

	public long getImageView() {
		return imageView;
	}

	public void destroy() {
		free();
		if(memoryManager != null) {
			memoryManager.remove(this);
		}
	}

	public void free() {
		vkDestroyImageView(dc.getDevice(), imageView, null);
		if(memoryManager != null) {
			Vma.vmaDestroyImage(memoryManager.getAllocator(), image, allocation);
		}
	}

	@Override
	public String toString() {
		return "VulkanImage{" +
				"image=" + image +
				", imageView=" + imageView +
				", allocation=" + allocation +
				", memoryManager=" + memoryManager +
				'}';
	}

	public void changeLayout(VkCommandBuffer commandBuffer, int newLayout) {
		VkImageMemoryBarrier.Buffer layoutTransition = VkImageMemoryBarrier.create(1)
				.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
				.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.srcAccessMask(VK_ACCESS_MEMORY_WRITE_BIT|VK_ACCESS_MEMORY_READ_BIT)
				.dstAccessMask(VK_ACCESS_MEMORY_WRITE_BIT|VK_ACCESS_MEMORY_READ_BIT);

		layoutTransition.image(image)
				.oldLayout(imageLayout)
				.newLayout(newLayout);

		layoutTransition.subresourceRange().set(aspect, 0, 1, 0, 1);

		vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, 0, null, null, layoutTransition);
		imageLayout = newLayout;
	}
}
