package go.graphics.swing.vulkan;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDevice;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VulkanDescriptorSetLayout {

	private final long layout;

	private final VkDevice device;

	final Map<Integer, Integer> descriptorCounts;

	public VulkanDescriptorSetLayout(VkDevice device, VkDescriptorSetLayoutBinding.Buffer bindings) {
		Map<Integer, Integer> tmpDescriptorCounts = new HashMap<>();

		for(VkDescriptorSetLayoutBinding binding : bindings) {
			int bindingCount = binding.descriptorCount();
			int bindingType = binding.descriptorType();

			Integer oldValue = tmpDescriptorCounts.get(bindingType);
			if(oldValue == null) oldValue = 0;

			tmpDescriptorCounts.put(bindingType, bindingCount + oldValue);
		}

		this.descriptorCounts = Collections.unmodifiableMap(tmpDescriptorCounts);

		this.device = device;

		layout = VulkanUtils.createDescriptorSetLayout(device, bindings);
	}

	public long getLayout() {
		return layout;
	}

	public void destroy() {
		VK10.vkDestroyDescriptorSetLayout(device, layout, null);
	}
}
