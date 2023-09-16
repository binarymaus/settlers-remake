package go.graphics;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import go.graphics.text.AbstractTextDrawer;
import go.graphics.text.EFontSize;
import go.graphics.text.TextDrawer;

public abstract class GLDrawContext {

	public GLDrawContext() {
		ManagedHandle.instance_count = 0;
	}

	protected List<ManagedHandle> managedHandles = new ArrayList<>();

	protected int maxUniformBlockSize;
	protected int maxTextureSize;

	public abstract void setShadowDepthOffset(float depth);

	/**
	 * Returns a texture id which is positive or 0. It returns a negative number on error.
	 *
	 * @param image The data as array. It needs to have a length of width * height and each element is a color with: 4 bits red, 4 bits green, 4 bits
	 *              blue and 4 bits alpha.
	 * @return The id of the generated texture.
	 */
	public abstract TextureHandle generateTexture(ImageData image, String name);

	protected abstract void drawMulti(MultiDrawHandle call);
	protected abstract void drawUnifiedArray(UnifiedDrawHandle call, int primitive, int vertexCount, float[] trans, float[] colors, int array_len);
	protected abstract void drawUnified(UnifiedDrawHandle call, int primitive, int vertices, int mode, float x, float y, float z, float sx, float sy, AbstractColor color, float intensity);

	public abstract void drawBackground(BackgroundDrawHandle call);

	public abstract void setHeightMatrix(float[] matrix);

	public abstract void setGlobalAttributes(float x, float y, float z, float sx, float sy, float sz);

	/**
	 * Updates a part of a texture image.
	 *
	 * @param textureIndex The texture to use.
	 * @param left
	 * @param bottom
	 * @param image
	 * @throws IllegalBufferException
	 */
	public abstract void updateTexture(TextureHandle textureIndex, int left, int bottom, ImageData image) throws IllegalBufferException;

	public abstract TextureHandle resizeTexture(TextureHandle textureIndex, ImageData image);

	public abstract void updateBufferAt(BufferHandle handle, int pos, ByteBuffer data) throws IllegalBufferException;

	public abstract BackgroundDrawHandle createBackgroundDrawCall(int vertices, TextureHandle texture);

	protected AbstractTextDrawer textDrawer;
	private final TextDrawer[] sizedTextDrawers = new TextDrawer[EFontSize.values().length];

	/**
	 * Gets a text drawer for the given text size.
	 *
	 * @param size
	 *            The size for the drawer.
	 * @return An instance of a drawer for that size.
	 */
	public TextDrawer getTextDrawer(EFontSize size) {
		if (sizedTextDrawers[size.ordinal()] == null) {
			sizedTextDrawers[size.ordinal()] = textDrawer.derive(size);
		}
		return sizedTextDrawers[size.ordinal()];
	}

	/**
	 * @param vertices Maximum number of vertices
	 * @param name     The label that the OpenGL handles get (nullable)
	 * @param texture  It determines whether this handle is textured or only single colored
	 * @param texture2
	 * @param data     If data is not equal null this will be a readonly buffer filled with data
	 * @return A handle to draw via the unified shader
	 */
	public abstract UnifiedDrawHandle createUnifiedDrawCall(int vertices, String name, TextureHandle texture, TextureHandle texture2, float[] data);

	protected abstract MultiDrawHandle createMultiDrawCall(String name, ManagedHandle source);

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

	private void addNewHandle() {
		int quad_count = getMaxManagedQuads();
		int texture_size = getMaxManagedTextureSize();

		TextureHandle tex = generateTexture(new ImageData(texture_size, texture_size), "managed" + ManagedHandle.instance_count);
		TextureHandle tex2 = generateTexture(new ImageData(texture_size, texture_size), "managed" + ManagedHandle.instance_count + "-2");
		UnifiedDrawHandle parent = createUnifiedDrawCall(quad_count*4, "managed" + ManagedHandle.instance_count, tex, tex2, null);
		managedHandles.add(new ManagedHandle(parent, quad_count, texture_size));
	}

	public ManagedUnifiedDrawHandle createManagedUnifiedDrawCall(ImageData texture, float offsetX, float offsetY, int width, int height) {
		int texWidth = texture.getWidth();
		int texHeight = texture.getHeight();

		for(ManagedHandle handle : managedHandles) {
			int position;
			if(handle.quad_index != handle.quad_count && (position = handle.findTextureHole(texWidth, texHeight)) != -1) {
				UIPoint corner;
				if((corner = handle.addTexture(texture, position)) == null) continue;


				float lu = (float) corner.getX();
				float lv = (float) corner.getY();
				float hu = lu + texWidth/(float) handle.texture_size;
				float hv = lv + texHeight/(float) handle.texture_size;

				float[] data = createQuadGeometry(offsetX, -offsetY, offsetX+width, -offsetY-height, lu, lv, hu, hv);

				handle.addQuad(data);

				return new ManagedUnifiedDrawHandle(handle, lu, lv, hu, hv);
			}
		}

		addNewHandle();
		return createManagedUnifiedDrawCall(texture, offsetX, offsetY, width, height);
	}

	private boolean valid = true;

	public void invalidate() {
		valid = false;
	}

	public boolean isValid() {
		return valid;
	}

	public abstract void clearDepthBuffer();

	protected void add(UnifiedDrawHandle cache) {
		caches.add(cache);
	}

	protected void remove(UnifiedDrawHandle cache) {
		caches.remove(cache);
	}

	private List<UnifiedDrawHandle> caches = new ArrayList<>();

	public void finishFrame() {
		for(int i = 0;i != caches.size(); i++) {
			if(caches.get(i).flush()) i--;
		}

		for(ManagedHandle mh : managedHandles) {
			if(mh.multiCache != null) mh.multiCache.flush();
		}
	}

	protected long frameIndex = 0;

	public long getFrameIndex() {
		return frameIndex;
	}

	public void startFrame() {
		frameIndex++;
	}

    public abstract void resize(int width, int height);

	protected int getMaxManagedQuads() {
		int maxManagedHandleQuads = maxUniformBlockSize / (4*4*4) /*size of one quad*/;

		if (maxManagedHandleQuads >= ManagedHandle.MAX_QUADS) {
			return ManagedHandle.MAX_QUADS;
		}

		return maxManagedHandleQuads;
	}

	protected int getMaxManagedTextureSize() {
		int maxManagedHandleTextureSize = maxTextureSize;

		if (maxManagedHandleTextureSize >= ManagedHandle.MAX_TEXTURE_SIZE) {
			return ManagedHandle.MAX_TEXTURE_SIZE;
		}

		return maxManagedHandleTextureSize;
	}

	protected String getManagedHandleDefine() {
		return "#define MAX_GEOMETRY_DATA_QUAD_COUNT " + getMaxManagedQuads();
	}
}
