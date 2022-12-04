package go.graphics.swing.vulkan;

import go.graphics.swing.vulkan.memory.VulkanImage;
import java.awt.Dimension;
import java.util.function.BiFunction;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSubmitInfo;

public abstract class AbstractVulkanOutput {

	protected VulkanDrawContext dc;

	abstract BiFunction<VkQueueFamilyProperties, Integer, Boolean> getPresentQueueCond(VkPhysicalDevice physicalDevice);

	void init(VulkanDrawContext dc) {
		this.dc = dc;
	}

	void destroy() {
	}

	abstract Dimension resize(Dimension preferredSize);

	abstract void createFramebuffers(VulkanImage depthImage);

	abstract boolean startFrame();

	abstract void endFrame(boolean wait);

	abstract VulkanImage getFramebufferImage();

	abstract long getFramebuffer();

	public abstract boolean needsPresentQueue();

	abstract void configureDrawCommand(MemoryStack stack, VkSubmitInfo graphSubmitInfo);
}
