package go.graphics.swing.vulkan.memory;

import go.graphics.BufferHandle;
import go.graphics.GLDrawContext;
import go.graphics.swing.vulkan.VulkanDrawContext;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;

public abstract class AbstractVulkanBuffer extends BufferHandle {

	protected final VulkanMemoryManager memoryManager;

	public AbstractVulkanBuffer(GLDrawContext dc, VulkanMemoryManager manager, int vbo) {
		super(dc, vbo);

		this.memoryManager = manager;
	}

	public abstract EVulkanMemoryType getType();

	public abstract long getEvent();

	public abstract long getBufferIdVk();

	public abstract long getAllocation();

	public abstract int getSize();

	public abstract void destroy();

	public final ByteBuffer map() {
		return map(0, getSize());
	}

	public final ByteBuffer map(int start) {
		return map(start, getSize() - start);
	}

	public final ByteBuffer map(int start, int mapSize) {
		assert start <= getSize();
		assert start + mapSize <= getSize();

		PointerBuffer ptr = BufferUtils.createPointerBuffer(1);
		Vma.vmaMapMemory(memoryManager.getAllocator(), getAllocation(), ptr);

		return MemoryUtil.memByteBuffer(ptr.get() + start, mapSize);
	}

	public final void unmap() {
		Vma.vmaUnmapMemory(memoryManager.getAllocator(), getAllocation());
	}

	public void flushChanges(int start, int size) {
		Vma.vmaFlushAllocation(memoryManager.getAllocator(), getAllocation(), start, size);
	}
}
