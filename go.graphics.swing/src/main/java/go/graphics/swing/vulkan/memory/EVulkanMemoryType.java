package go.graphics.swing.vulkan.memory;

import org.lwjgl.util.vma.Vma;

public enum EVulkanMemoryType {
	STATIC(Vma.VMA_MEMORY_USAGE_GPU_ONLY, false, true),
	DYNAMIC(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU, false, false),
	STAGING(Vma.VMA_MEMORY_USAGE_CPU_ONLY, true, false),
	READBACK(Vma.VMA_MEMORY_USAGE_GPU_TO_CPU, false, true);

	private final int vmaType;
	private final boolean transferSource;
	private final boolean transferDestination;

	EVulkanMemoryType(int vmaType, boolean transferSource, boolean transferDestination) {
		this.vmaType = vmaType;
		this.transferSource = transferSource;
		this.transferDestination = transferDestination;
	}

	public int getVmaType() {
		return vmaType;
	}

	public boolean isTransferDestination() {
		return transferDestination;
	}

	public boolean isTransferSource() {
		return transferSource;
	}
}
