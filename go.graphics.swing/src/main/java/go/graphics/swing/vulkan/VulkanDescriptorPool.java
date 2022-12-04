package go.graphics.swing.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VulkanDescriptorPool {
	private final VkDevice device;
	private final int setCount;
	private final Map<Integer, Integer> allocateCounts;

	private final Set<SingleDescriptorPool> allPools = new HashSet<>();
	private SingleDescriptorPool currentPool;

	VulkanDescriptorPool(VkDevice device, int setCount, Map<Integer, Integer> allocateCounts) {
		this.device = device;
		this.setCount = setCount;
		this.allocateCounts = new HashMap<>(allocateCounts);

		createNewPool();
	}

	public void destroy() {
		allPools.forEach(SingleDescriptorPool::destroy);
		allPools.clear();
	}

	private void updateAllocateCounts(VulkanDescriptorSetLayout descriptorSetLayout) {
		if(!descriptorSetLayout.descriptorCounts.keySet().stream().allMatch(allocateCounts::containsKey)) {
			throw new IllegalArgumentException("This descriptor pool does not support this descriptor type!");
		}

		for(Map.Entry<Integer, Integer> descriptorAmount : descriptorSetLayout.descriptorCounts.entrySet()) {
			int type = descriptorAmount.getKey();
			int requestedAmount = descriptorAmount.getValue();
			int providedAmount = allocateCounts.get(type);

			if(providedAmount < requestedAmount) {
				allocateCounts.put(type, requestedAmount);
			}
		}
	}

	private void createNewPool() {
		currentPool = new SingleDescriptorPool();
		allPools.add(currentPool);
	}

	public long createNewSet(VulkanDescriptorSetLayout descSetLayout) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			long descSet = currentPool.tryAllocate(stack, descSetLayout);

			if(descSet != 0) {
				return descSet;
			}

			updateAllocateCounts(descSetLayout);
			createNewPool();

			descSet = currentPool.tryAllocate(stack, descSetLayout);

			if(descSet == 0) {
				throw new IllegalStateException("Internal error!");
			}

			return descSet;
		}
	}

	private class SingleDescriptorPool {
		private final long descriptorPool;

		private int setsRemaining;
		private final Map<Integer, Integer> descriptorsRemaining;

		public SingleDescriptorPool() {
			this.descriptorPool = VulkanUtils.createDescriptorPool(device, setCount, allocateCounts);

			setsRemaining = setCount;
			descriptorsRemaining = new HashMap<>(allocateCounts);
		}

		public long tryAllocate(MemoryStack stack, VulkanDescriptorSetLayout setLayout) {
			if(setsRemaining == 0) {
				return 0;
			}

			if(!setLayout.descriptorCounts
					.entrySet()
					.stream()
					.allMatch(request -> canProvide(request.getKey(), request.getValue()))) {
				return 0;
			}

			long descSet = VulkanUtils.createDescriptorSet(stack, device, descriptorPool, stack.longs(setLayout.getLayout()));

			setsRemaining--;

			setLayout.descriptorCounts
					.entrySet()
					.forEach(request -> allocate(request.getKey(), request.getValue()));

			return descSet;
		}

		private boolean canProvide(int type, int amount) {
			if(!descriptorsRemaining.containsKey(type)) return false;

			return descriptorsRemaining.get(type) >= amount;
		}

		private void allocate(int type, int amount) {
			int previousRemaining = descriptorsRemaining.get(type);

			descriptorsRemaining.put(type, previousRemaining - amount);
		}

		private void destroy() {
			VK10.vkDestroyDescriptorPool(device, descriptorPool, null);
		}
	}
}
