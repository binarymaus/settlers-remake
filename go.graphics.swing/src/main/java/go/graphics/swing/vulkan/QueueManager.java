package go.graphics.swing.vulkan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import static org.lwjgl.vulkan.VK10.*;

public class QueueManager {

	private final int universalIndex;
	private final int graphicsIndex;
	private final int presentIndex;

	private VkQueue graphicsQueue;
	private VkQueue presentQueue;

	private final VulkanDrawContext dc;

	public QueueManager(MemoryStack stack, VulkanDrawContext dc, VkPhysicalDevice physicalDevice, BiFunction<VkQueueFamilyProperties, Integer, Boolean> presentQueueCond) {
		this.dc = dc;
		VkQueueFamilyProperties.Buffer allQueueFamilies = VulkanUtils.listQueueFamilies(stack, physicalDevice);

		universalIndex = VulkanUtils.findQueue(allQueueFamilies, (queue, index) -> presentQueueCond.apply(queue, index) && isGraphicsQueue(queue, index));
		if (universalIndex != -1) {
			graphicsIndex = universalIndex;
			presentIndex = universalIndex;
		} else {
			graphicsIndex = VulkanUtils.findQueue(allQueueFamilies, QueueManager::isGraphicsQueue);
			presentIndex = VulkanUtils.findQueue(allQueueFamilies, presentQueueCond);
		}
	}

	public int getGraphicsIndex() {
		return graphicsIndex;
	}

	public VkQueue getGraphicsQueue() {
		return graphicsQueue;
	}

	public int getPresentIndex() {
		return presentIndex;
	}

	public VkQueue getPresentQueue() {
		return presentQueue;
	}

	public boolean hasUniversalQueue() {
		return universalIndex != -1;
	}

	public boolean hasGraphicsSupport() {
		return graphicsIndex != -1;
	}

	public boolean hasPresentSupport() {
		return presentIndex != -1;
	}

	public int[] getQueueIndices() {
		List<Integer> indices = new ArrayList<>();
		if(hasUniversalQueue()) {
			indices.add(universalIndex);
		} else {
			if(hasGraphicsSupport()) {
				indices.add(graphicsIndex);
			}

			if(hasPresentSupport()) {
				indices.add(presentIndex);
			}
		}
		return indices.stream().mapToInt(i -> i).toArray();
	}

	private static boolean isGraphicsQueue(VkQueueFamilyProperties queue, int index) {
		return (queue.queueFlags()&VK_QUEUE_GRAPHICS_BIT)>0;
	}

	public void registerQueues() {
		if(hasUniversalQueue()) {
			VkQueue universalQueue = VulkanUtils.getDeviceQueue(dc.getDevice(), universalIndex, 0);

			graphicsQueue = universalQueue;
			presentQueue = universalQueue;
		} else {
			if(hasGraphicsSupport()) {
				graphicsQueue = VulkanUtils.getDeviceQueue(dc.getDevice(), graphicsIndex, 0);
			}

			if(hasPresentSupport()) {
				presentQueue = VulkanUtils.getDeviceQueue(dc.getDevice(), presentIndex, 0);
			}
		}
	}
}
