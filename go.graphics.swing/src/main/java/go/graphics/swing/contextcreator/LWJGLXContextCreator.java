package go.graphics.swing.contextcreator;

import go.graphics.swing.ContextContainer;
import go.graphics.swing.event.swingInterpreter.GOSwingEventConverter;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

public class LWJGLXContextCreator extends ContextCreator<AWTGLCanvas> {

	public LWJGLXContextCreator(ContextContainer ac, boolean debug) {
		super(ac, debug);
	}

	@Override
	public void stop() {
		canvas.disposeCanvas();
	}

	@Override
	public void initSpecific() {
		GLData data = new GLData();
		data.profile = GLData.Profile.CORE;
		data.majorVersion = 3;
		data.minorVersion = 3;
		data.debug = debug;

		canvas = new AWTGLCanvas(data) {
			@Override
			public void initGL() {
				parent.wrapNewGLContext();
			}

			@Override
			public void paintGL() {
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
					swapBuffers();
				} catch (ContextException e) {}
			}

			@Override
			public void repaint() {
				render();

				super.repaint();
			}
		};

		new GOSwingEventConverter(canvas, parent);
	}
}
