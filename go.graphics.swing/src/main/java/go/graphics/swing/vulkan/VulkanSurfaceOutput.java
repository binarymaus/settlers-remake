package go.graphics.swing.vulkan;

import go.graphics.swing.vulkan.memory.VulkanImage;
import java.awt.Dimension;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.function.BiFunction;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanSurfaceOutput extends AbstractVulkanOutput {

	private long surface;
	private long swapchain = VK_NULL_HANDLE;
	private int surfaceFormat;
	private int swapchainImageIndex = -1;
	private long waitSemaphore;
	private long signalSemaphore;

	private VulkanImage[] swapchainImages;
	private long[] framebuffers;
	private final VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.create();
	private final VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.create();

	public VulkanSurfaceOutput(long surface) {
		this.surface = surface;
	}

	@Override
	BiFunction<VkQueueFamilyProperties, Integer, Boolean> getPresentQueueCond(VkPhysicalDevice physicalDevice) {
		return (queue, index) -> {
			int[] present = new int[1];
			if(surface == 0) return true;

			vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, index, surface, present);
			return present[0]==1;
		};
	}

	public void setSurface(long surface) {
		this.surface = surface;

		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkSurfaceFormatKHR.Buffer allSurfaceFormats = VulkanUtils.listSurfaceFormats(stack, dc.getDevice().getPhysicalDevice(), surface);
			VkSurfaceFormatKHR surfaceFormat = VulkanUtils.findSurfaceFormat(allSurfaceFormats);
			int newSurfaceFormat = surfaceFormat.format();

			IntBuffer present = stack.callocInt(1);
			vkGetPhysicalDeviceSurfaceSupportKHR(dc.getDevice().getPhysicalDevice(), dc.queueManager.getPresentIndex(), surface, present);
			if(present.get(0) == 0) {
				System.err.println("[VULKAN] can't present anymore");
				return;
			}


			swapchainCreateInfo.surface(surface)
					.imageColorSpace(surfaceFormat.colorSpace())
					.imageFormat(surfaceFormat.format());

			if(dc.getRenderPass() == 0 || this.surfaceFormat != newSurfaceFormat) {
				dc.regenerateRenderPass(stack, newSurfaceFormat);
				framebufferCreateInfo.renderPass(dc.getRenderPass());
			}
			this.surfaceFormat = newSurfaceFormat;
		}

		dc.resize();
	}

	@Override
	void init(VulkanDrawContext dc) {
		super.init(dc);


		waitSemaphore = VulkanUtils.createSemaphore(dc.getDevice());
		signalSemaphore = VulkanUtils.createSemaphore(dc.getDevice());

		swapchainCreateInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
				.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
				.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT|VK_IMAGE_USAGE_TRANSFER_SRC_BIT|VK_IMAGE_USAGE_TRANSFER_DST_BIT)
				.presentMode(VK_PRESENT_MODE_FIFO_KHR) // must be supported by all drivers
				.imageArrayLayers(1)
				.clipped(false);

		if(dc.queueManager.hasUniversalQueue()) {
			swapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
		} else {
			IntBuffer queueFamilies = BufferUtils.createIntBuffer(2);
			queueFamilies.put(0, dc.queueManager.getPresentIndex());
			queueFamilies.put(1, dc.queueManager.getGraphicsIndex());

			swapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT)
					.pQueueFamilyIndices(queueFamilies);
		}

		framebufferCreateInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
				.layers(1);

		setSurface(surface);
	}

	public void removeSurface() {
		destroyFramebuffers(-1);
		destroySwapchainViews(-1);
		vkDestroySwapchainKHR(dc.getDevice(), swapchain, null);
		vkDestroySurfaceKHR(dc.getInstance(), surface, null);

		surface = VK_NULL_HANDLE;
		swapchain = VK_NULL_HANDLE;
	}

	@Override
	void destroy() {
		super.destroy();

		if(waitSemaphore != 0) {
			vkDestroySemaphore(dc.getDevice(), waitSemaphore, null);
		}
		if(signalSemaphore != 0) {
			vkDestroySemaphore(dc.getDevice(), signalSemaphore, null);
		}

		if(swapchain != VK_NULL_HANDLE) {
			destroyFramebuffers(-1);
			destroySwapchainViews(-1);
			vkDestroySwapchainKHR(dc.getDevice(), swapchain, null);
		}
		swapchain = VK_NULL_HANDLE;
	}

	@Override
	Dimension resize(Dimension preferredSize) {
		destroyFramebuffers(-1);
		destroySwapchainViews(-1);

		VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.create();
		if (surface == 0 || vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dc.getDevice().getPhysicalDevice(), surface, surfaceCapabilities) != VK_SUCCESS) {
			return null;
		}

		int fbWidth = preferredSize.width;
		int fbHeight = preferredSize.height;
		int imageCount = surfaceCapabilities.minImageCount() + 1;

		VkExtent2D minDim = surfaceCapabilities.maxImageExtent();
		VkExtent2D maxDim = surfaceCapabilities.maxImageExtent();

		fbWidth = Math.max(Math.min(fbWidth, maxDim.width()), minDim.width());
		fbHeight = Math.max(Math.min(fbHeight, maxDim.height()), minDim.height());
		if (surfaceCapabilities.maxImageCount() != 0)
			imageCount = Math.min(imageCount, surfaceCapabilities.maxImageCount());

		swapchainCreateInfo.preTransform(surfaceCapabilities.currentTransform())
				.minImageCount(imageCount)
				.oldSwapchain(swapchain)
				.imageExtent()
				.width(fbWidth)
				.height(fbHeight);

		LongBuffer swapchainBfr = BufferUtils.createLongBuffer(1);
		boolean error = vkCreateSwapchainKHR(dc.getDevice(), swapchainCreateInfo, null, swapchainBfr) != VK_SUCCESS;
		vkDestroySwapchainKHR(dc.getDevice(), swapchain, null);

		if (error) {
			swapchain = VK_NULL_HANDLE;
			return null;
		}
		swapchain = swapchainBfr.get(0);

		framebufferCreateInfo.width(fbWidth)
				.height(fbHeight);

		return new Dimension(fbWidth, fbHeight);
	}

	private void destroySwapchainViews(int count) {
		if(swapchainImages == null) return;
		if(count == -1) count = swapchainImages.length;

		for(int i = 0; i != count; i++) {
			swapchainImages[i].destroy();
		}
		swapchainImages = null;
	}

	private void destroyFramebuffers(int count) {
		if(framebuffers == null) return;
		if(count == -1) count = framebuffers.length;

		for(int i = 0; i != count; i++) {
			vkDestroyFramebuffer(dc.getDevice(), framebuffers[i], null);
		}
		framebuffers = null;
	}

	@Override
	void createFramebuffers(VulkanImage depthImage) {
		long[] imageHandles = VulkanUtils.getSwapchainImages(dc.getDevice(), swapchain);
		if (imageHandles == null) {
			vkDestroySwapchainKHR(dc.getDevice(), swapchain, null);
			swapchain = VK_NULL_HANDLE;
			return;
		}

		swapchainImages = new VulkanImage[imageHandles.length];
		for (int i = 0; i != swapchainImages.length; i++) {

			try {
				swapchainImages[i] = new VulkanImage(dc, null, imageHandles[i], -1L, surfaceFormat, VK_IMAGE_ASPECT_COLOR_BIT);
			} catch(Throwable thrown) {
				thrown.printStackTrace();
				destroySwapchainViews(i);
				vkDestroySwapchainKHR(dc.getDevice(), swapchain, null);
				swapchain = VK_NULL_HANDLE;
				return;
			}
		}

		LongBuffer framebufferBfr = BufferUtils.createLongBuffer(1);
		LongBuffer attachments = BufferUtils.createLongBuffer(2);
		attachments.put(1, depthImage.getImageView());

		framebuffers = new long[swapchainImages.length];
		for (int i = 0; i != swapchainImages.length; i++) {
			attachments.put(0, swapchainImages[i].getImageView());
			framebufferCreateInfo.pAttachments(attachments);

			if (vkCreateFramebuffer(dc.getDevice(), framebufferCreateInfo, null, framebufferBfr) != VK_SUCCESS) {
				destroyFramebuffers(i);
				destroySwapchainViews(-1);
				vkDestroySwapchainKHR(dc.getDevice(), swapchain, null);
				swapchain = VK_NULL_HANDLE;
			}

			framebuffers[i] = framebufferBfr.get(0);
		}
	}

	@Override
	boolean startFrame() {
		if(swapchain == VK_NULL_HANDLE) {
			dc.resize();
			return false;
		}

		IntBuffer swapchainImageIndexBfr = BufferUtils.createIntBuffer(1);
		int err = vkAcquireNextImageKHR(dc.getDevice(), swapchain, -1L, waitSemaphore, VK_NULL_HANDLE, swapchainImageIndexBfr);
		if(err == VK_ERROR_OUT_OF_DATE_KHR || err == VK_SUBOPTIMAL_KHR) {
			dc.resize();
		}
		if(err != VK_SUBOPTIMAL_KHR && err != VK_SUCCESS) {
			return false;
		}

		swapchainImageIndex = swapchainImageIndexBfr.get(0);
		return true;
	}

	@Override
	void endFrame(boolean wait) {
		if (swapchainImageIndex == -1) {
			return;
		}

		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
					.pImageIndices(stack.ints(swapchainImageIndex))
					.swapchainCount(1)
					.pSwapchains(stack.longs(swapchain));
			if (wait) {
				presentInfo.pWaitSemaphores(stack.longs(signalSemaphore));
			}

			if (vkQueuePresentKHR(dc.queueManager.getPresentQueue(), presentInfo) != VK_SUCCESS) {
				// should not happen but we can't do anything about it
			}
		}
		vkQueueWaitIdle(dc.queueManager.getPresentQueue());
		swapchainImageIndex = -1;
	}

	@Override
	VulkanImage getFramebufferImage() {
		return swapchainImages[swapchainImageIndex];
	}

	@Override
	long getFramebuffer() {
		return framebuffers[swapchainImageIndex];
	}

	@Override
	public boolean needsPresentQueue() {
		return true;
	}

	@Override
	void configureDrawCommand(MemoryStack stack, VkSubmitInfo graphSubmitInfo) {
		graphSubmitInfo.pWaitSemaphores(stack.longs(waitSemaphore))
					.pSignalSemaphores(stack.longs(signalSemaphore))
						.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
						.waitSemaphoreCount(1);
	}
}
