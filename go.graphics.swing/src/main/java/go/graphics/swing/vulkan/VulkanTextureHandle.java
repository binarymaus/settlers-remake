package go.graphics.swing.vulkan;

import go.graphics.swing.vulkan.memory.VulkanImage;
import go.graphics.swing.vulkan.memory.VulkanMemoryManager;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import go.graphics.ETextureType;
import go.graphics.TextureHandle;

public class VulkanTextureHandle extends TextureHandle {

	private boolean installed = false;
	private boolean shouldDestroy = false;
	private boolean isDestroyed = false;
	final long descSet;
	private final VulkanMemoryManager manager;
	private final VulkanImage image;

	public VulkanTextureHandle(VulkanDrawContext dc, VulkanMemoryManager manager, VulkanImage image, long descSet) {
		super(dc, -1);
		this.image = image;
		this.descSet = descSet;
		this.manager = manager;
	}

	public VulkanImage getImage() {
		return image;
	}

	public void destroy() {
		if(isDestroyed) return;
		image.destroy();
		isDestroyed = true;
	}

	@Override
	public String toString() {
		return "VulkanTextureHandle{" +
				"installed=" + installed +
				", shouldDestroy=" + shouldDestroy +
				", isDestroyed=" + isDestroyed +
				", descSet=" + descSet +
				", manager=" + manager +
				", image=" + image +
				", id=" + id +
				'}';
	}

	void tick() {
		if(!installed) {
			installed = true;
			install();
		}

		if(shouldDestroy) {
			destroy();
		}
	}

	private void install() {
		if(descSet == 0) return;

		VkDescriptorImageInfo.Buffer install_texture_image = VkDescriptorImageInfo.create(1)
				.imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

		VkWriteDescriptorSet.Buffer install_texture_write = VkWriteDescriptorSet.create(1)
				.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
				.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
				.dstBinding(0)
				.descriptorCount(1)
				.dstArrayElement(0)
				.dstSet(descSet)
				.pImageInfo(install_texture_image);


		install_texture_image.imageView(image.getImageView())
				.sampler(((VulkanDrawContext) dc).samplers[getType().ordinal()]);

		VK10.vkUpdateDescriptorSets(((VulkanDrawContext)dc).getDevice(), install_texture_write, null);
	}

	public void setDestroy() {
		shouldDestroy = true;
	}

	@Override
	public void setType(ETextureType type) {
		super.setType(type);
		installed = false;
	}

	@Override
	public boolean isValid() {
		return !isDestroyed && super.isValid();
	}
}
