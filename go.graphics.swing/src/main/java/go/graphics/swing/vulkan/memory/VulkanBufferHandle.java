package go.graphics.swing.vulkan.memory;

import go.graphics.swing.vulkan.VulkanDrawContext;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK10;

import go.graphics.GLDrawContext;

public class VulkanBufferHandle extends AbstractVulkanBuffer {

	private final long bfr, allocation, event;
	private final int size;
	private final EVulkanMemoryType type;

	public VulkanBufferHandle(GLDrawContext dc, VulkanMemoryManager manager, EVulkanMemoryType type, long bfr, long allocation, long event, int size) {
		super(dc, manager, -1);
		this.allocation = allocation;
		this.event = event;
		this.type = type;
		this.size = size;
		this.bfr = bfr;
	}

	@Override
	public EVulkanMemoryType getType() {
		return type;
	}

	@Override
	public long getEvent() {
		return event;
	}

	@Override
	public long getBufferIdVk() {
		return bfr;
	}

	@Override
	public long getAllocation() {
		return allocation;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public void destroy() {
		free();
		memoryManager.remove(this);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [dc=" + dc + ", allocation=" + allocation + ", bfr=" + bfr + "]";
	}

	public void free() {
		Vma.vmaDestroyBuffer(memoryManager.getAllocator(), bfr, allocation);
		VK10.vkDestroyEvent(((VulkanDrawContext)dc).getDevice(), event, null);
	}
}
