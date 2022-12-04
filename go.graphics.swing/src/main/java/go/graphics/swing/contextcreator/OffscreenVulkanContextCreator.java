package go.graphics.swing.contextcreator;

import go.graphics.swing.ContextContainer;
import go.graphics.swing.event.swingInterpreter.GOSwingEventConverter;
import go.graphics.swing.vulkan.CustomVulkanOutput;
import go.graphics.swing.vulkan.VulkanUtils;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkInstance;

import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.vulkan.VK10.*;

public class OffscreenVulkanContextCreator extends ContextCreator<Canvas> {

	private final VkInstance instance;
	private final long debugCallback;
	private final CustomVulkanOutput output;

	private BufferedImage img;
	private IntBuffer fbReadback = null;

	public OffscreenVulkanContextCreator(ContextContainer ac, boolean debug) {
		super(ac, debug);

		try(MemoryStack stack = MemoryStack.stackPush()) {
			List<String> extensions = new ArrayList<>();
			if(debug) {
				extensions.add(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
			}

			instance = VulkanUtils.createInstance(stack, extensions, debug);
			debugCallback = debug ? VulkanUtils.setupDebugging(instance) : 0;

			output = new CustomVulkanOutput();
			parent.wrapNewVkContext(instance, output);
		}
	}

	@Override
	public void stop() {
		if(debug) vkDestroyDebugReportCallbackEXT(instance, debugCallback, null);
		vkDestroyInstance(instance, null);
	}

	@Override
	public void initSpecific() {
		canvas = new Canvas() {
			@Override
			public void update(Graphics graphics) {
				if (isShowing()) paint(graphics);
			}

			public void paint(Graphics graphics) {
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

					int width = output.getFramebufferSize().width;
					int height = output.getFramebufferSize().height;
					if (img == null || img.getWidth() != width || img.getHeight() != height) {
						img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
					}

					if (fbReadback == null || fbReadback.remaining() != width * height) {
						fbReadback = BufferUtils.createIntBuffer(width * height);
					}

					parent.readFramebuffer(fbReadback, width, height);

					for (int y = 0; y != height; y++) {
						for (int x = 0; x != width; x++) {
							img.setRGB(x, height - y - 1, (fbReadback.get(y * width + x)));
						}
					}

					graphics.drawImage(img, 0, 0, null);
				} catch (ContextException e) {
					e.printStackTrace();
				}
			}
		};

		new GOSwingEventConverter(canvas, parent);
	}
}
