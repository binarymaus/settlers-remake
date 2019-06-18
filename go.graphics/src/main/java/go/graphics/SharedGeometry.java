package go.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;

public class SharedGeometry {

	static final int CAPACITY = 1000;
	private static final int QUAD_SIZE = 4*4*4;
	private static int maxIndex = 0;

	private int size = 0;
	private int index;
	public GeometryHandle geometry;
	private static final ByteBuffer generate_buffer = ByteBuffer.allocateDirect(QUAD_SIZE).order(ByteOrder.nativeOrder());

	private static final ArrayList<SharedGeometry> geometries = new ArrayList<>();

	public static SharedGeometryHandle addGeometry(GLDrawContext dc, float[] data) throws IllegalBufferException {
		if(staticdc == null) staticdc = dc;
		int sgeometryIndex = 0;

		while(true) {
			// create an instance if needed
			if(geometries.size() == sgeometryIndex) geometries.add(new SharedGeometry(dc, ++maxIndex));

			SharedGeometry geometry = geometries.get(sgeometryIndex);

			// skip if we wont fit
			if (geometry.size < CAPACITY) {
				// add it to our vbo
				generate_buffer.asFloatBuffer().put(data);
				dc.updateGeometryAt(geometry.geometry, QUAD_SIZE*geometry.size, generate_buffer);

				geometry.size++;
				return new SharedGeometryHandle(geometry);
			}

			sgeometryIndex++;
		}
	}

	public static class SharedGeometryHandle {
		public final GeometryHandle geometry;
		public final SharedGeometry parent;
		public final int index;
		private final int iteration = SharedGeometry.iteration;

		private SharedGeometryHandle(SharedGeometry shared) {
			geometry = shared.geometry;
			index = shared.size-1;
			parent = shared;
		}
	}

	private static int iteration = 0;
	private static GLDrawContext staticdc = null;

	public static boolean isInvalid(GLDrawContext dc, SharedGeometryHandle handle) {
		if(dc != staticdc) {
			geometries.clear();
			staticdc = dc;
			iteration++;
		}
		return handle.iteration!=iteration;
	}

	private SharedGeometry(GLDrawContext dc, int index) {
		this.index = index;
		if(index > 1) System.err.println("SharedGeometry count > SharedGeometry.CAPACITY - try increasing CAPACITY");
		geometry = dc.generateGeometry(CAPACITY*4, EGeometryFormatType.Texture2D, true, "sharedgeometry-" + index);
	}


	public static float[] createQuadGeometry(float lx, float ly, float hx, float hy, float lu, float lv, float hu, float hv) {
		return new float[] {
			// bottom right
			hx, ly, hu, lv,
			// top right
			hx, hy, hu, hv,
			// top left
			lx, hy, lu, hv,
			// bottom left
			lx, ly, lu, lv,
		};
	}
}
