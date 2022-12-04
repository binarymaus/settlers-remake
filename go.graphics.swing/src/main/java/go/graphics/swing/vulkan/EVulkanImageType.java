package go.graphics.swing.vulkan;

import static org.lwjgl.vulkan.VK10.*;

public enum EVulkanImageType {
	COLOR_IMAGE_RGBA4(VK_FORMAT_R4G4B4A4_UNORM_PACK16, VK_IMAGE_ASPECT_COLOR_BIT, VK_IMAGE_USAGE_SAMPLED_BIT|VK_IMAGE_USAGE_TRANSFER_DST_BIT),
	COLOR_IMAGE_RGBA8(VK_FORMAT_B8G8R8A8_UNORM, VK_IMAGE_ASPECT_COLOR_BIT, VK_IMAGE_USAGE_SAMPLED_BIT|VK_IMAGE_USAGE_TRANSFER_DST_BIT),
	FRAMEBUFFER_RGBA8(VK_FORMAT_B8G8R8A8_UNORM, VK_IMAGE_ASPECT_COLOR_BIT, VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT|VK_IMAGE_USAGE_TRANSFER_SRC_BIT|VK_IMAGE_USAGE_TRANSFER_DST_BIT),
	DEPTH_IMAGE(VK_FORMAT_D32_SFLOAT, VK_IMAGE_ASPECT_DEPTH_BIT, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT),
	;

	private final int format;
	private final int aspect;
	private final int usage;

	EVulkanImageType(int format, int aspect, int usage) {
		this.format = format;
		this.aspect = aspect;
		this.usage = usage;
	}

	public int getFormat() {
		return format;
	}

	public int getAspect() {
		return aspect;
	}

	public int getUsage() {
		return usage;
	}
}
