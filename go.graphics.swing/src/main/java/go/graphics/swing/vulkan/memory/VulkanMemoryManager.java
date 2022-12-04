package go.graphics.swing.vulkan.memory;

import go.graphics.swing.vulkan.EVulkanImageType;
import go.graphics.swing.vulkan.VulkanDrawContext;
import go.graphics.swing.vulkan.VulkanUtils;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;

import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanMemoryManager {

	private final long allocator;
	private final VulkanDrawContext dc;

	protected final List<VulkanMultiBufferHandle> multiBuffers = new ArrayList<>();
	protected final List<VulkanBufferHandle> buffers = new ArrayList<>();
	private final List<VulkanImage> images = new ArrayList<>();

	public VulkanMemoryManager(MemoryStack stack, VulkanDrawContext dc) {
		this.allocator = VulkanUtils.createAllocator(stack, dc.getInstance(), dc.getDevice(), dc.getDevice().getPhysicalDevice());

		this.dc = dc;
	}

	public void destroy() {
		for (VulkanBufferHandle buffer : buffers) {
			buffer.free();
		}
		for(VulkanImage image : images) {
			image.free();
		}

		buffers.clear();

		vmaDestroyAllocator(allocator);
	}

	public VulkanMultiBufferHandle createMultiBuffer(int size, EVulkanMemoryType type, EVulkanBufferUsage bufferUsage) {
		VulkanMultiBufferHandle vkMultiBfrHandle = new VulkanMultiBufferHandle(dc, this, type, bufferUsage, size);
		multiBuffers.add(vkMultiBfrHandle);
		return vkMultiBfrHandle;
	}

	public VulkanBufferHandle createBuffer(int size, EVulkanMemoryType memoryType, EVulkanBufferUsage usage) {
		VmaAllocationCreateInfo allocInfo = VmaAllocationCreateInfo.create();
		allocInfo.usage(memoryType.getVmaType());

		VkBufferCreateInfo createInfo = VkBufferCreateInfo.create();
		createInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
				.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
				.size(size);

		int usageFlags = usage.getUsageFlags();
		if(memoryType.isTransferSource()) {
			usageFlags |= VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
		}

		if(memoryType.isTransferDestination()) {
			usageFlags |= VK_BUFFER_USAGE_TRANSFER_DST_BIT;
		}
		createInfo.usage(usageFlags);

		long event;
		try {
			event = VulkanUtils.createEvent(dc.getDevice());
		} catch(Throwable thrown) {
			thrown.printStackTrace();
			return null;
		}

		LongBuffer bufferPtr = BufferUtils.createLongBuffer(1);
		PointerBuffer allocPtr = BufferUtils.createPointerBuffer(1);
		if(vmaCreateBuffer(allocator, createInfo, allocInfo, bufferPtr, allocPtr, null) < 0) {
			vkDestroyEvent(dc.getDevice(), event, null);
			return null;
		}


		VulkanBufferHandle vkBfrHandle = new VulkanBufferHandle(dc, this, memoryType, bufferPtr.get(), allocPtr.get(), event, size);
		buffers.add(vkBfrHandle);
		return vkBfrHandle;
	}


	public VulkanImage createImage(int width, int height, EVulkanImageType imageType) {
		LongBuffer imagePtr = BufferUtils.createLongBuffer(1);
		PointerBuffer allocationPtr = BufferUtils.createPointerBuffer(1);

		VmaAllocationCreateInfo allocInfo = VmaAllocationCreateInfo.create();
		allocInfo.usage(VMA_MEMORY_USAGE_GPU_ONLY);

		VkImageCreateInfo createInfo = VkImageCreateInfo.create()
				.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
				.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
				.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
				.tiling(VK_IMAGE_TILING_OPTIMAL)
				.samples(VK_SAMPLE_COUNT_1_BIT)
				.imageType(VK_IMAGE_TYPE_2D)
				.format(imageType.getFormat())
				.arrayLayers(1)
				.mipLevels(1)
				.usage(imageType.getUsage());

		createInfo.extent().set(width, height, 1);

		if(vmaCreateImage(allocator, createInfo, allocInfo, imagePtr, allocationPtr, null) < 0) {
			throw new Error("Could not create Image.");
		}

		long image = imagePtr.get();
		long allocation = allocationPtr.get();

		VulkanImage imageHandle = null;
		try {
			imageHandle = new VulkanImage(dc, this, image, allocation, imageType);
			images.add(imageHandle);
		} finally {
			if(imageHandle == null) {
				Vma.vmaDestroyImage(allocator, image, allocation);
			}
		}
		return imageHandle;
	}


	public void startFrame() {
		for (VulkanMultiBufferHandle multiBuffer : multiBuffers) {
			multiBuffer.reset();
		}
	}

	public long getAllocator() {
		return allocator;
	}

	void remove(VulkanBufferHandle buffer) {
		buffers.remove(buffer);
	}

	void remove(VulkanImage image) {
		images.remove(image);
	}
}
