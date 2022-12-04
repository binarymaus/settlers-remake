package go.graphics.swing.vulkan.memory;

import static org.lwjgl.vulkan.VK10.*;

public enum EVulkanBufferUsage {
	NONE(0),
	VERTEX_BUFFER(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT),
	INDEX_BUFFER(VK_BUFFER_USAGE_INDEX_BUFFER_BIT),
	UNIFORM_BUFFER(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT),
	VERTEX_UNIFORM_BUFFER(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT|VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
	;

	private final int usageFlags;

	EVulkanBufferUsage(int usageFlags) {
		this.usageFlags = usageFlags;
	}

	public int getUsageFlags() {
		return usageFlags;
	}
}
