package jsettlers.graphics.image;

import go.graphics.EPrimitiveType;
import go.graphics.GLDrawContext;
import go.graphics.ImageData;
import go.graphics.TextureHandle;
import go.graphics.UnifiedDrawHandle;
import jsettlers.common.Color;

public class BuildingProgressDrawer {

	public static UnifiedDrawHandle progressHandle = null;
	public static UnifiedDrawHandle triProgressHandle = null;

	private static TextureHandle dummyTexture = null;

	private static final ImageData dummyImage;

	static {
		dummyImage = new ImageData(1, 1);
	}

	private static void checkStaticHandles(GLDrawContext gl) {
		if(dummyTexture == null || !dummyTexture.isValid()) {
			dummyTexture = gl.generateTexture(dummyImage, "dummy-texture");
		}

		if(progressHandle == null || !progressHandle.isValid()) {
			progressHandle = gl.createUnifiedDrawCall(4, "building-progress-quad", dummyTexture, dummyTexture, GLDrawContext.createQuadGeometry(0, 0, 1, 1, 0, 0, 1, 1));
			progressHandle.forceNoCache();
		}

		if(triProgressHandle == null || !triProgressHandle.isValid()) {
			triProgressHandle = gl.createUnifiedDrawCall(3, "building-progress-tri", dummyTexture, dummyTexture, new float[] {0, 0, 0, 0, 0.5f, 1, 0.5f, 1, 1, 0, 1, 0});
			triProgressHandle.forceNoCache();
		}
	}

	public static void drawOnlyImageWithProgressAt(GLDrawContext gl, float x, float y, float z,
												   float u1, float v1,
												   float u2, float v2,
												   float umin, float umax,
												   float vmin, float vmax,
												   TextureHandle texture,
												   TextureHandle texture2,
												   float offsetX,
												   float offsetY,
												   float height,
												   float width,
												   float fow, boolean triangle) {
		checkStaticHandles(gl);

		float nu1 = umin+u1*(umax-umin);
		float nu2 = umin+u2*(umax-umin);

		float nv1 = vmin+v1*(vmax-vmin);
		float nv2 = vmin+v2*(vmax-vmin);

		UnifiedDrawHandle dh = triangle?triProgressHandle:progressHandle;
		dh.texture = texture;
		dh.texture2 = texture2;
		dh.drawProgress(triangle? EPrimitiveType.Triangle:EPrimitiveType.Quad,
				x+offsetX+width*u1,
				y-offsetY-height*v1,
				z,
				width*(u2-u1),
				-height*(v2-v1),
				new Color(nu1, nv1, nu2, nv2),
				fow);
	}
}
