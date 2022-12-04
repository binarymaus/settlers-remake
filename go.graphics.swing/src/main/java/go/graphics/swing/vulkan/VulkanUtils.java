/*******************************************************************************
 * Copyright (c) 2019
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package go.graphics.swing.vulkan;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDebugReportCallbackCreateInfoEXT;
import org.lwjgl.vulkan.VkDebugReportCallbackEXT;
import org.lwjgl.vulkan.VkDebugReportCallbackEXTI;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkEventCreateInfo;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSpecializationInfo;
import org.lwjgl.vulkan.VkSpecializationMapEntry;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import go.graphics.EPrimitiveType;

import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;

import static org.lwjgl.util.vma.Vma.*;

/**
 * This class splits all api calls there are needed to initialize Vulkan into small functions to prevent long functions and verbose names.
 */
public class VulkanUtils {


	public static final int ALLOCATE_TEXTURE_SLOTS = 10;
	public static final int ALLOCATE_UBO_SLOTS = 10;
	public static final int ALLOCATE_SET_SLOTS = 20;

	public static final int TEXTURE_POOL_SIZE = 10;
	public static final int MULTI_POOL_SIZE = 10;

	public static final int MAX_GLOBALTRANS_COUNT = 10;

	public static List<String> defaultExtensionArray(boolean debug) {
		List<String> extensions = new ArrayList<>();
		extensions.add(VK_KHR_SURFACE_EXTENSION_NAME);
		if(debug) extensions.add(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
		return extensions;
	}

	public static VkInstance createInstance(MemoryStack stack, List<String> extensions, boolean debug) {
		// app info
		VkApplicationInfo applicationInfo = VkApplicationInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
				.apiVersion(VK_MAKE_VERSION(1, 0, 0))
				.pEngineName(stack.UTF8("go.graphics.swing"))
				.engineVersion(VK_MAKE_VERSION(1, 0, 0))
				.pApplicationName(stack.UTF8("JSettlers"))
				.applicationVersion(VK_MAKE_VERSION(1, 0, 0));

		PointerBuffer extensionsPointer = stack.pointers(extensions.stream().map(stack::UTF8).toArray(ByteBuffer[]::new));
		PointerBuffer layersPointer = null;

		if(debug) {
			Optional<String> validationLayerOpt = VulkanUtils.listAvailableExtensions(stack)
					.stream().map(VkLayerProperties::layerNameString)
					.filter(name -> name.endsWith("_validation"))// we are searching for validation layers
					.sorted().findFirst(); // sorted insures that we prefer the KHRONOS layer if both (KHRONOS and LUNARG(old name)) are present
			if(validationLayerOpt.isPresent()) {
				layersPointer = stack.pointers(stack.UTF8(validationLayerOpt.get()));
			} else {
				System.err.println("Could not find any validation layer!");
			}
		}

		VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
				.pApplicationInfo(applicationInfo)
				.ppEnabledExtensionNames(extensionsPointer)
				.ppEnabledLayerNames(layersPointer);

		PointerBuffer instancePointer = stack.mallocPointer(1);
		if(vkCreateInstance(createInfo, null, instancePointer) != VK_SUCCESS) throw new Error("Could not create Instance.");
		return new VkInstance(instancePointer.get(0), createInfo);
	}

	private static final VkDebugReportCallbackEXTI DEBUG_CALLBACK = (flags, objectType, object, location, messageCode, pLayerPrefix, pMessage, pUserData) -> {
		System.err.println("[VULKAN] " + VkDebugReportCallbackEXT.getString(pMessage));
		return 0;
	};

	public static long setupDebugging(VkInstance instance) {
		VkDebugReportCallbackCreateInfoEXT debugCreateInfo = VkDebugReportCallbackCreateInfoEXT.create()
				.sType(VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT)
				.pfnCallback(DEBUG_CALLBACK)
				.flags(VK_DEBUG_REPORT_ERROR_BIT_EXT | VK_DEBUG_REPORT_WARNING_BIT_EXT |
						VK_DEBUG_REPORT_INFORMATION_BIT_EXT | VK_DEBUG_REPORT_PERFORMANCE_WARNING_BIT_EXT |
						VK_DEBUG_REPORT_DEBUG_BIT_EXT);

		long[] debugCallbackAddr = new long[1];
		if(vkCreateDebugReportCallbackEXT(instance, debugCreateInfo, null, debugCallbackAddr) != VK_SUCCESS) {
			throw new Error("Could not set DebugReportCallback.");
		}
		return debugCallbackAddr[0];
	}

	public static int findQueue(VkQueueFamilyProperties.Buffer allQueueFamilies, BiFunction<VkQueueFamilyProperties, Integer, Boolean> condition) {
		for(int i = 0; i != allQueueFamilies.limit(); i++) {
			if(condition.apply(allQueueFamilies.get(i), i)) return i;
		}

		return -1;
	}

	public static VkSurfaceFormatKHR findSurfaceFormat(VkSurfaceFormatKHR.Buffer allSurfaceFormats) {
		// TODO proper SurfaceFormat finding
		Optional<VkSurfaceFormatKHR> defaultSurfaceFormat = allSurfaceFormats.stream().filter(format -> format.format() == VK_FORMAT_B8G8R8A8_UNORM).findFirst();
		return defaultSurfaceFormat.orElseGet(() -> allSurfaceFormats.get(0));
	}

	public static VkPhysicalDevice findPhysicalDevice(VkPhysicalDevice[] allPhysicalDevices) {
		if(allPhysicalDevices.length == 0) throw new Error("No PhysicalDevices found.");

		return allPhysicalDevices[0];
	}

	public static VkLayerProperties.Buffer listAvailableExtensions(MemoryStack stack) {
		IntBuffer countBfr = stack.mallocInt(1);
		if(vkEnumerateInstanceLayerProperties(countBfr, null) != VK_SUCCESS) {
			throw new Error("Could not query LayerProperties count.");
		}
		int count = countBfr.get(0);

		VkLayerProperties.Buffer layers = count>100? VkLayerProperties.create(count) : VkLayerProperties.malloc(count);
		if(vkEnumerateInstanceLayerProperties(countBfr, layers) != VK_SUCCESS) {
			throw new Error("Could not query LayerProperties.");
		}
		return layers;
	}

	public static VkQueueFamilyProperties.Buffer listQueueFamilies(MemoryStack stack, VkPhysicalDevice device) {
		IntBuffer countBfr = stack.mallocInt(1);
		vkGetPhysicalDeviceQueueFamilyProperties(device, countBfr, null);
		int count = countBfr.get(0);

		VkQueueFamilyProperties.Buffer queueFamilies = count>100? VkQueueFamilyProperties.create(count) : VkQueueFamilyProperties.malloc(count);
		vkGetPhysicalDeviceQueueFamilyProperties(device, countBfr, queueFamilies);
		return queueFamilies;
	}

	public static VkSurfaceFormatKHR.Buffer listSurfaceFormats(MemoryStack stack, VkPhysicalDevice device, long surface) {
		IntBuffer countBfr = stack.mallocInt(1);
		if(vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, countBfr, null) != VK_SUCCESS) {
			throw new Error("Could not query SurfaceFormat count.");
		}

		int count = countBfr.get(0);
		VkSurfaceFormatKHR.Buffer surfaceFormats = count>100? VkSurfaceFormatKHR.create(count) : VkSurfaceFormatKHR.malloc(count, stack);
		if(vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, countBfr, surfaceFormats) != VK_SUCCESS) {
			throw new Error("Could not query SurfaceFormat descriptors.");
		}

		return surfaceFormats;
	}

	public static VkPhysicalDevice[] listPhysicalDevices(MemoryStack stack, VkInstance instance) {
		IntBuffer countBfr = stack.mallocInt(1);
		if(vkEnumeratePhysicalDevices(instance, countBfr, null) != VK_SUCCESS) {
			throw new Error("Could not query PhysicalDevice count.");
		}

		int count = countBfr.get(0);
		PointerBuffer devicesPtr = count>100? BufferUtils.createPointerBuffer(count) : stack.mallocPointer(count);
		if(vkEnumeratePhysicalDevices(instance, countBfr, devicesPtr) != VK_SUCCESS) {
			throw new Error("Could not query PhysicalDevice descriptors.");
		}

		VkPhysicalDevice[] devices = new VkPhysicalDevice[count];
		for(int i = 0; i != count; i++) devices[i] = new VkPhysicalDevice(devicesPtr.get(i), instance);
		return devices;
	}

	public static VkDevice createDevice(MemoryStack stack, VkPhysicalDevice physicalDevice, List<String> extensions, int[] queueFamilies) {
		VkDeviceQueueCreateInfo.Buffer queueInfos = VkDeviceQueueCreateInfo.calloc(queueFamilies.length, stack);
		for(int i = 0; i != queueFamilies.length; i++) {
			queueInfos.get(i).sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
					.queueFamilyIndex(queueFamilies[i])
					.pQueuePriorities(stack.floats(i == 0 ? 1 : 0.1f));
		}


		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.malloc(stack);
		vkGetPhysicalDeviceFeatures(physicalDevice, deviceFeatures);

		PointerBuffer extensionsPointer = stack.pointers(extensions.stream().map(stack::UTF8).toArray(ByteBuffer[]::new));


		VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
				.pQueueCreateInfos(queueInfos)
				.pEnabledFeatures(deviceFeatures)
				.ppEnabledExtensionNames(extensionsPointer)
				.ppEnabledLayerNames(null);
		// legacy implementations might not enable layers we set at instance creation but layers are a debug/nice-to-have option anyway

		PointerBuffer devicePtr = stack.mallocPointer(1);
		if(vkCreateDevice(physicalDevice, deviceCreateInfo, null, devicePtr) != VK_SUCCESS) {
			throw new Error("Could not create Device.");
		}

		return new VkDevice(devicePtr.get(0), physicalDevice, deviceCreateInfo);
	}

	public static VkQueue getDeviceQueue(VkDevice device, int familyIndex, int queueIndex) {
		PointerBuffer queuePtr = BufferUtils.createPointerBuffer(1);
		vkGetDeviceQueue(device, familyIndex, queueIndex, queuePtr);

		return new VkQueue(queuePtr.get(), device);
	}


	public static long[] getSwapchainImages(VkDevice device, long swapchain) {
		int[] count = new int[1];
		if(vkGetSwapchainImagesKHR(device, swapchain, count, null) != VK_SUCCESS) {
			return null;
		}

		long[] images = new long[count[0]];
		if(vkGetSwapchainImagesKHR(device, swapchain, count, images) != VK_SUCCESS) {
			return null;
		}
		return images;
	}

	private static final VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.create().sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
	private static final VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.create().sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);

	public static long createSemaphore(VkDevice device) {
		LongBuffer bfr = BufferUtils.createLongBuffer(1);

		if(vkCreateSemaphore(device, semaphoreCreateInfo, null, bfr) != VK_SUCCESS) {
			throw new Error("Could not create Semaphore.");
		}
		return bfr.get();
	}

	public static long createFence(LongBuffer fenceBfr, VkDevice device) {
		if(vkCreateFence(device, fenceCreateInfo, null, fenceBfr) != VK_SUCCESS) {
			throw new Error("Could not create Fence.");
		}
		return fenceBfr.get(0);
	}

	public static long createCommandPool(VkDevice device, int queueIndex) {
		VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.create()
				.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
				.queueFamilyIndex(queueIndex)
				.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);

		LongBuffer commandPoolBfr = BufferUtils.createLongBuffer(1);
		if(vkCreateCommandPool(device, commandPoolCreateInfo, null, commandPoolBfr) != VK_SUCCESS) {
			throw new Error("Could not create CommandPool.");
		}
		return commandPoolBfr.get();
	}

	public static VkCommandBuffer createCommandBuffer(VkDevice device, long commandPool) {
		VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.create()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
				.commandPool(commandPool)
				.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
				.commandBufferCount(1);

		PointerBuffer commandBufferBfr = BufferUtils.createPointerBuffer(1);
		if(vkAllocateCommandBuffers(device, commandBufferAllocateInfo, commandBufferBfr) != VK_SUCCESS) {
			throw new Error("Could not allocate CommandBuffer.");
		}
		return new VkCommandBuffer(commandBufferBfr.get(0), device);
	}

	public static long createRenderPass(MemoryStack stack, VkDevice device, int colorFormat) {
		VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(2, stack);
		attachments.get(0).format(colorFormat)
				.samples(VK_SAMPLE_COUNT_1_BIT)
				.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
				.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
				.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
				.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

		attachments.get(1).format(VK_FORMAT_D32_SFLOAT)
				.samples(VK_SAMPLE_COUNT_1_BIT)
				.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
				.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
				.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
				.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

		VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack)
				.attachment(0)
				.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

		VkAttachmentReference depthAttachmentRef = VkAttachmentReference.calloc(stack)
				.attachment(1)
				.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

		VkSubpassDescription.Buffer subPass = VkSubpassDescription.calloc(1, stack)
				.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
				.colorAttachmentCount(1)
				.pColorAttachments(colorAttachmentRef)
				.pDepthStencilAttachment(depthAttachmentRef);

		VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
				.pAttachments(attachments)
				.pSubpasses(subPass);

		LongBuffer renderPassBfr = stack.callocLong(1);
		if(vkCreateRenderPass(device, renderPassCreateInfo, null, renderPassBfr) != VK_SUCCESS) {
			throw new Error("Could not create RenderPass.");
		}

		return renderPassBfr.get(0);
	}

	public static long createPipeline(MemoryStack stack, VkDevice device, int primitive, long pipelineLayout, long renderPass, long vertShader, long fragShader, VkPipelineVertexInputStateCreateInfo vertexInputState, int maxManagedQuads) {
		int topology = primitive-1;
		if(primitive == EPrimitiveType.Line) topology = VK_PRIMITIVE_TOPOLOGY_LINE_LIST;

		VkPipelineInputAssemblyStateCreateInfo inputAssemblyState = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
				.topology(topology);

		VkSpecializationMapEntry.Buffer max_globalattr_count_at = VkSpecializationMapEntry.calloc(2, stack);
		max_globalattr_count_at.get(0).set(0, 0, 4);
		max_globalattr_count_at.get(1).set(1, 4, 4);
		VkSpecializationInfo max_globalattr_count = VkSpecializationInfo.calloc(stack)
				.pData(stack.calloc(8).putInt(0, MAX_GLOBALTRANS_COUNT)
						.putInt(1, maxManagedQuads))
				.pMapEntries(max_globalattr_count_at);


		VkPipelineShaderStageCreateInfo.Buffer stages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
		stages.get(0).sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
				.stage(VK_SHADER_STAGE_VERTEX_BIT)
				.module(vertShader)
				.pSpecializationInfo(max_globalattr_count)
				.pName(stack.UTF8("main"));

		stages.get(1).sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
				.stage(VK_SHADER_STAGE_FRAGMENT_BIT)
				.module(fragShader)
				.pName(stack.UTF8("main"));

		VkPipelineRasterizationStateCreateInfo rasterizationState = VkPipelineRasterizationStateCreateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
				.polygonMode(VK_POLYGON_MODE_FILL)
				.cullMode(VK_CULL_MODE_NONE)
				.frontFace(VK_FRONT_FACE_CLOCKWISE)
				.depthBiasEnable(false)
				.lineWidth(1);

		VkPipelineMultisampleStateCreateInfo multisampleState = VkPipelineMultisampleStateCreateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
				.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

		VkPipelineColorBlendAttachmentState.Buffer attachments = VkPipelineColorBlendAttachmentState.calloc(1, stack);
		attachments.colorWriteMask(VK_COLOR_COMPONENT_R_BIT|VK_COLOR_COMPONENT_G_BIT|VK_COLOR_COMPONENT_B_BIT|VK_COLOR_COMPONENT_A_BIT)
				.blendEnable(true)
				.srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA)
				.srcAlphaBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA)
				.dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
				.dstAlphaBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
				.colorBlendOp(VK_BLEND_OP_ADD)
				.alphaBlendOp(VK_BLEND_OP_ADD);

		VkPipelineColorBlendStateCreateInfo colorBlendState = VkPipelineColorBlendStateCreateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
				.pAttachments(attachments);

		VkPipelineDepthStencilStateCreateInfo depthState = VkPipelineDepthStencilStateCreateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
				.depthTestEnable(true)
				.depthWriteEnable(true)
				.depthCompareOp(VK_COMPARE_OP_LESS_OR_EQUAL);

		VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
				.pDynamicStates(stack.ints(VK_DYNAMIC_STATE_SCISSOR, VK_DYNAMIC_STATE_VIEWPORT));

		VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
				.scissorCount(1)
				.viewportCount(1);


		VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack)
				.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
				.layout(pipelineLayout)
				.renderPass(renderPass)
				.pDynamicState(dynamicState)
				.pVertexInputState(vertexInputState)
				.pInputAssemblyState(inputAssemblyState)
				.pStages(stages)
				.pViewportState(viewportState)
				.pRasterizationState(rasterizationState)
				.pMultisampleState(multisampleState)
				.pColorBlendState(colorBlendState)
				.pDepthStencilState(depthState);

		LongBuffer pipelineBfr = stack.callocLong(1);
		if(vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineCreateInfo, null, pipelineBfr) != VK_SUCCESS) {
			throw new Error("Could not create Pipeline.");
		}
		return pipelineBfr.get(0);
	}

	public static long createPipelineLayout(MemoryStack stack, VkDevice device, VkPipelineLayoutCreateInfo pipelineLayout) {
		LongBuffer pipelineLayoutBfr = stack.callocLong(1);
		if(vkCreatePipelineLayout(device, pipelineLayout, null, pipelineLayoutBfr) != VK_SUCCESS) {
			throw new Error("Could not create -PipelineLayout.");
		}
		return pipelineLayoutBfr.get(0);
	}

	public static long createImageView(VkDevice device, long image, int format, int aspect) {
		VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.create()
				.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
				.viewType(VK_IMAGE_VIEW_TYPE_2D)
				.format(format)
				.image(image);

		createInfo.subresourceRange().set(aspect, 0, 1, 0, 1);

		LongBuffer imageView = BufferUtils.createLongBuffer(1);

		if(vkCreateImageView(device, createInfo, null, imageView) != VK_SUCCESS) {
			throw new Error("Could not create ImageView");
		}
		return imageView.get(0);
	}

	public static long createDescriptorPool(VkDevice device, int maxSets, Map<Integer, Integer> descriptorAmounts) {
		VkDescriptorPoolSize.Buffer sizes = VkDescriptorPoolSize.calloc(descriptorAmounts.keySet().size());

		for(Map.Entry<Integer, Integer> descriptorAmount : descriptorAmounts.entrySet()) {
			sizes.get().set(descriptorAmount.getKey(), descriptorAmount.getValue());
		}
		sizes.rewind();

		VkDescriptorPoolCreateInfo createInfo = VkDescriptorPoolCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pPoolSizes(sizes)
				.maxSets(maxSets);

		LongBuffer poolBfr = BufferUtils.createLongBuffer(1);
		if(vkCreateDescriptorPool(device, createInfo, null, poolBfr) != VK_SUCCESS) {
			throw new Error("Could not create DescriptorPool.");
		}
		return poolBfr.get(0);
	}

	public static long createDescriptorSet(MemoryStack stack, VkDevice device, long descPool, LongBuffer setLayouts) {
		VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
				.descriptorPool(descPool)
				.pSetLayouts(setLayouts);

		LongBuffer descSet = stack.callocLong(1);
		if(vkAllocateDescriptorSets(device, allocInfo, descSet) != VK_SUCCESS) {
			throw new Error("Could not create DescriptorSet.");
		}
		return descSet.get(0);
	}

	private static byte[] readShader(String shaderName) {
		try(InputStream in = VulkanUtils.class.getResourceAsStream(shaderName)) {
			byte[] output = new byte[0];
			int usedCapacity = 0;


			byte[] buffer = new byte[1024];
			do {
				int read = in.read(buffer);

				if(read == -1) {
					byte[] realOutput = new byte[usedCapacity];
					System.arraycopy(output, 0, realOutput, 0, usedCapacity);
					return realOutput;
				}

				if(output.length - usedCapacity < read) {
					byte[] newOutput = new byte[output.length*2 + read];
					System.arraycopy(output, 0, newOutput, 0, usedCapacity);

					output = newOutput;
				}

				System.arraycopy(buffer, 0, output, usedCapacity, read);
				usedCapacity += read;

			} while (true);

		} catch (IOException e) {
			throw new Error(e);
		}
	}

	private static final int SHADER_CODE_ALIGN = 4;

	public static long createShaderModule(MemoryStack stack, VkDevice device, String fileName) {
		byte[] code = readShader(fileName);

		ByteBuffer codeBuffer = BufferUtils.createByteBuffer(code.length);
		//int padding = (int) (SHADER_CODE_ALIGN - (MemoryUtil.memAddress(codeBuffer) % SHADER_CODE_ALIGN));
		//codeBuffer.position(padding);
		codeBuffer.put(code);
		codeBuffer.rewind();

		LongBuffer modulePtr = stack.callocLong(1);

		VkShaderModuleCreateInfo shaderModuleCreateInfo = VkShaderModuleCreateInfo.create()
				.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
				.pCode(codeBuffer);

		if(vkCreateShaderModule(device, shaderModuleCreateInfo, null, modulePtr) != VK_SUCCESS) {
			throw new Error("Could not create ShaderModule.");
		}
		return modulePtr.get(0);
	}

	public static long createDescriptorSetLayout(VkDevice device, VkDescriptorSetLayoutBinding.Buffer bindings) {
		VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
				.pBindings(bindings);
		LongBuffer output = BufferUtils.createLongBuffer(1);
		vkCreateDescriptorSetLayout(device, createInfo, null, output);
		return output.get(0);
	}

	public static long createAllocator(MemoryStack stack, VkInstance instance, VkDevice device, VkPhysicalDevice physicalDevice) {
		VmaAllocatorCreateInfo allocCreateInfo = VmaAllocatorCreateInfo.calloc(stack)
				.pVulkanFunctions(VmaVulkanFunctions.calloc(stack).set(instance, device))
				.physicalDevice(physicalDevice)
				.instance(instance)
				.frameInUseCount(1)
				.device(device);

		PointerBuffer allocatorBfr = stack.callocPointer(1);
		if(vmaCreateAllocator(allocCreateInfo, allocatorBfr) < 0) throw new Error("Could not create an Allocator.");
		return allocatorBfr.get(0);
	}

	public static long createSampler(MemoryStack stack, VkSamplerCreateInfo samplerCreateInfo, VkDevice device) {
		LongBuffer samplerBfr = stack.callocLong(1);

		if(vkCreateSampler(device, samplerCreateInfo, null, samplerBfr) != VK_SUCCESS) {
			throw new Error("Could not create Sampler.");
		}
		return samplerBfr.get(0);
	}


	private final static VkEventCreateInfo EVENT_CREATE_INFO = VkEventCreateInfo.create().sType(VK_STRUCTURE_TYPE_EVENT_CREATE_INFO);

	public static long createEvent(VkDevice device) {
		long[] event = new long[1];
		if(vkCreateEvent(device, EVENT_CREATE_INFO, null, event) != VK_SUCCESS) {
			throw new Error("Could not create Event.");
		}
		return event[0];
	}
}
