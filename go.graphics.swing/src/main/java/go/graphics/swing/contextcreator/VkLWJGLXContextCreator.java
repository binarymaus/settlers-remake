package go.graphics.swing.contextcreator;

import go.graphics.swing.ContextContainer;
import go.graphics.swing.event.swingInterpreter.GOSwingEventConverter;
import go.graphics.swing.vulkan.VulkanSurfaceOutput;
import go.graphics.swing.vulkan.VulkanUtils;
import java.util.List;
import javax.swing.JOptionPane;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.awt.AWTVKCanvas;
import org.lwjgl.vulkan.awt.VKData;
import static org.lwjgl.vulkan.EXTDebugReport.vkDestroyDebugReportCallbackEXT;
import static org.lwjgl.vulkan.EXTMetalSurface.VK_EXT_METAL_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRWin32Surface.VK_KHR_WIN32_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRXlibSurface.VK_KHR_XLIB_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

public class VkLWJGLXContextCreator extends ContextCreator<AWTVKCanvas> {

	private VkInstance instance;
	private long debugCallback;

	public VkLWJGLXContextCreator(ContextContainer ac, boolean debug) {
		super(ac, debug);

		JOptionPane.showMessageDialog(parent, "Vulkan via LWJGLX is experimental!", "Warning", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void stop() {
		if(debug) vkDestroyDebugReportCallbackEXT(instance, debugCallback, null);
		vkDestroySurfaceKHR(instance, canvas.surface, null);
		vkDestroyInstance(instance, null);
	}

	@Override
	public void initSpecific() {
		VKData data = new VKData();

		try(MemoryStack stack = MemoryStack.stackPush()) {
			// instance extensions
			List<String> extensions = VulkanUtils.defaultExtensionArray(debug);
			if (Platform.get() == Platform.LINUX)
				extensions.add(VK_KHR_XLIB_SURFACE_EXTENSION_NAME);
			if (Platform.get() == Platform.WINDOWS)
				extensions.add(VK_KHR_WIN32_SURFACE_EXTENSION_NAME);
			if(Platform.get() == Platform.MACOSX)
				extensions.add(VK_EXT_METAL_SURFACE_EXTENSION_NAME);

			instance = VulkanUtils.createInstance(stack, extensions, debug);
		}
		debugCallback = debug ? VulkanUtils.setupDebugging(instance) : 0;
		data.instance = instance;

		canvas = new AWTVKCanvas(data) {

			@Override
			public void initVK() {
				parent.wrapNewVkContext(instance, new VulkanSurfaceOutput(canvas.surface));
			}

			@Override
			public void paintVK() {
				try {
					synchronized (wnd_lock) {
						if (change_res) {
							width = new_width;
							height = new_height;

							parent.resizeContext(width, height);
							change_res = false;
						}
					}
					parent.draw();
					parent.finishFrame();
					parent.swapBuffersVk();
				} catch (ContextException e) {
					e.printStackTrace();
				}
			}
		};

		new GOSwingEventConverter(canvas, parent);
	}
}
