package go.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ManagedHandle {

	public static final int MAX_QUADS = 1024;
	public static final int MAX_TEXTURE_SIZE = 2048;

	protected static int instance_count = 0;

	protected int quad_count;
	protected int quad_index = 0;
	protected int texture_size;
	private final int[] remaining_pixels;

	public final UnifiedDrawHandle bufferHolder;
	protected final MultiDrawHandle multiCache;

	protected ManagedHandle(UnifiedDrawHandle bufferHolder, int quad_count, int texture_size) {
		this.bufferHolder = bufferHolder;
		this.quad_count = quad_count;
		this.texture_size = texture_size;
		remaining_pixels = new int[texture_size];
		multiCache = bufferHolder.dc.createMultiDrawCall("managed" + instance_count, this);

		Arrays.fill(remaining_pixels, texture_size);
		ManagedHandle.instance_count++;
	}

	protected int findTextureHole(int width, int height) {
		int placement_count = texture_size - width;

		int[] placements = new int[placement_count];

		for(int i = 0; i != placement_count; i++) {
			int sum_of_remaining = 0;
			int lowest_remaining = remaining_pixels[i];
			for(int j = 0; j != width; j++) {
				sum_of_remaining += remaining_pixels[i+j];
				if(lowest_remaining > remaining_pixels[i+j]) lowest_remaining = remaining_pixels[i+j];
			}

			if(lowest_remaining < height) {
				placements[i] = -1;
			} else {
				placements[i] = sum_of_remaining - (width * lowest_remaining);
			}
		}

		int lowest_damage_placement = -1;
		for(int i = 0;i != placement_count; i++) {
			if(placements[i] != -1) {
				if(lowest_damage_placement == -1) {
					lowest_damage_placement = i;
				} else if(placements[lowest_damage_placement] > placements[i]) {
					lowest_damage_placement = i;
				}
			}
		}

		return lowest_damage_placement;
	}

	protected UIPoint addTexture(ImageData texture, int start) {
		int width = texture.getWidth();
		int height = texture.getHeight();

		int leastRemaining = remaining_pixels[start];
		for(int i = 0; i != width; i++) {
			if(leastRemaining > remaining_pixels[i+start]) {
				leastRemaining = remaining_pixels[i+start];
			}
		}

		if(leastRemaining < height) return null;

		// claim texture space
		Arrays.fill(remaining_pixels, start, start+width, leastRemaining-height);

		// ...and populate it
		try {
			bufferHolder.dc.updateTexture(bufferHolder.texture, start, texture_size-leastRemaining, texture);
			// TODO properly fill tex2
			bufferHolder.dc.updateTexture(bufferHolder.texture2, start, texture_size-leastRemaining, texture);
		} catch(IllegalBufferException e) {}
		return new UIPoint(start/(float)texture_size, (texture_size-leastRemaining)/(float)texture_size);
	}

	private static final ByteBuffer dataBuffer = ByteBuffer.allocateDirect(4*4*4).order(ByteOrder.nativeOrder());
	protected void addQuad(float[] data) {
		dataBuffer.asFloatBuffer().put(data);
		try {
			bufferHolder.dc.updateBufferAt(bufferHolder.vertices, quad_index*4*4*4, dataBuffer);
		} catch (IllegalBufferException e) {}
	}
}
