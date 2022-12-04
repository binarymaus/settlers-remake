package go.graphics.swing.vulkan.pipeline;

import go.graphics.swing.vulkan.VulkanDescriptorPool;
import go.graphics.swing.vulkan.VulkanDrawContext;
import java.util.Set;
import org.lwjgl.system.MemoryStack;

public enum EVulkanPipelineType {
	UNIFIED_QUAD(VulkanPipeline.UnifiedPipeline::createQuad),
	UNIFIED_LINE(VulkanPipeline.UnifiedPipeline::createLine),
	UNIFIED_ARRAY(VulkanPipeline.UnifiedArrayPipeline::new),
	UNIFIED_MULTI(VulkanPipeline.UnifiedMultiPipeline::new),
	BACKGROUND(VulkanPipeline.BackgroundPipeline::new);

	private final PipelineCreator creator;

	public static final Set<EVulkanPipelineType> UNIFIED_TYPES = Set.of(UNIFIED_QUAD, UNIFIED_LINE, UNIFIED_ARRAY, UNIFIED_MULTI);

	EVulkanPipelineType(PipelineCreator creator) {
		this.creator = creator;
	}

	public VulkanPipeline create(MemoryStack stack, VulkanDrawContext dc, VulkanDescriptorPool descPool, long renderPass) {
		return creator.createPipeline(stack, dc, descPool, renderPass);
	}

	public interface PipelineCreator {
		VulkanPipeline createPipeline(MemoryStack stack, VulkanDrawContext dc, VulkanDescriptorPool descPool, long renderPass);
	}
}
