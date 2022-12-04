package go.graphics.swing.vulkan.memory;

import go.graphics.swing.vulkan.VulkanDrawContext;
import java.util.ArrayList;
import java.util.List;

public class VulkanMultiBufferHandle extends AbstractVulkanBuffer {

    private final int size;
    private final EVulkanMemoryType type;
    private final EVulkanBufferUsage usage;
    private int bufferIndex = -1;
    private final List<VulkanBufferHandle> buffers = new ArrayList<>();

    public VulkanMultiBufferHandle(VulkanDrawContext dc, VulkanMemoryManager manager, EVulkanMemoryType type, EVulkanBufferUsage usage, int size) {
        super(dc, manager, -1);
        this.size = size;
        this.type = type;
        this.usage = usage;
    }

    public void inc() {
        bufferIndex++;
        if(bufferIndex == buffers.size()) buffers.add(memoryManager.createBuffer(size, type, usage));
    }

    public void reset() {
        if(bufferIndex == -1) buffers.add(memoryManager.createBuffer(size, type, usage));
        bufferIndex = 0;
    }

    @Override
    public long getBufferIdVk() {
        return buffers.get(bufferIndex).getBufferIdVk();
    }

    @Override
    public long getAllocation() {
        return buffers.get(bufferIndex).getAllocation();
    }

    @Override
    public long getEvent() {
        return buffers.get(bufferIndex).getEvent();
    }

    @Override
    public void destroy() {}

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[dc=" + dc + ", size=" + size + ", bufferCount=" + buffers.size() + "]";
    }

    @Override
    public EVulkanMemoryType getType() {
        return type;
    }

    @Override
    public int getSize() {
        return size;
    }
}
