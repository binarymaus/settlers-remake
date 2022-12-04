/*******************************************************************************
 * Copyright (c) 2015
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
package jsettlers.graphics.image;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import go.graphics.EUnifiedMode;
import go.graphics.GLDrawContext;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import jsettlers.common.Color;
import jsettlers.graphics.image.reader.ImageMetadata;
import go.graphics.ImageData;
import jsettlers.graphics.image.reader.translator.ImageDataProducer;

/**
 * This is the image of something that is displayed as an object on the map, e.g. an settler.
 * <p>
 * It can have a torso, an overlay that is always drawn together with the image.
 * 
 * @author michael
 */
public class SettlerImage extends SingleImage {

	public static float shadow_offset = 0;
	private SingleImage torso = null;
	private SingleImage shadow = null;

	/**
	 * Creates a new settler image.
	 * 
	 * @param metadata
	 *            The mata data to use.
	 * @param data
	 *            The data to use.
	 * @param name
	 * 				The name of the image.
	 */
	public SettlerImage(ImageMetadata metadata, ImageDataProducer data, String name) {
		super(metadata, data, name);
	}

	public ImageMetadata getMetadata() {
		var meta = new ImageMetadata();
		meta.height = height;
		meta.width = width;
		meta.offsetX = offsetX;
		meta.offsetY = offsetY;
		return meta;
	}

	@Override
	public void drawAt(GLDrawContext gl, float x, float y, float z, Color torsoColor, float fow) {
		checkHandles(gl);
		geometryIndex.drawComplexQuad(EUnifiedMode.SETTLER_SHADOW, x, y, z, 1, 1, torsoColor, fow);
	}

	@Override
	public void drawOnlyImageAt(GLDrawContext gl, float x, float y, float z, Color torsoColor, float fow) {
		checkHandles(gl);
		geometryIndex.drawComplexQuad(EUnifiedMode.SETTLER, x, y, z, 1, 1, torsoColor, fow);
	}

	@Override
	public void drawOnlyShadowAt(GLDrawContext gl, float x, float y, float z) {
		checkHandles(gl);
		geometryIndex.drawComplexQuad(EUnifiedMode.SHADOW_ONLY, x, y, z, 1, 1, Color.TRANSPARENT, 1);
	}

	/**
	 * Sets the image overlay.
	 * 
	 * @param torso
	 *            The torso. May be null.
	 */
	public void setTorso(SingleImage torso) {
		this.torso = torso;
	}

	public void setShadow(SingleImage shadow) {
		this.shadow = shadow;
	}

	/**
	 * Gets the torso for this image.
	 * 
	 * @return The torso.
	 */
	public SingleImage getTorso() {
		return this.torso;
	}

	public SingleImage getShadow() {
		return shadow;
	}

	protected ImageData generateTextureData() {
		toffsetX = offsetX;
		toffsetY = offsetY;

		int tx = offsetX+width;
		int ty = offsetY+height;

		ImageData data = getData();

		float scaleX = data.getWidth()/(float) width;
		float scaleY = data.getHeight()/(float) height;
		if(width == 0) {
			scaleX = 1;
		}

		if(height == 0) {
			scaleY = 1;
		}

		ImageData torsoData = null;
		ImageData shadowData = null;

		if(torso != null && torso.width != 0 && torso.height != 0) {
			torsoData = torso.getData();

			scaleX = Math.max(scaleX, torsoData.getWidth()/(float)torso.width);
			scaleY = Math.max(scaleY, torsoData.getHeight()/(float)torso.height);

			if(torso.offsetX < toffsetX) toffsetX = torso.offsetX;
			if(torso.offsetY < toffsetY) toffsetY = torso.offsetY;
			if(torso.offsetX+torso.width > tx) tx = torso.offsetX+torso.width;
			if(torso.offsetY+torso.height > ty) ty = torso.offsetY+torso.height;
		}

		if(shadow != null && shadow.width != 0 && shadow.height != 0) {
			shadowData = shadow.getData();

			scaleX = Math.max(scaleX, shadowData.getWidth()/(float)shadow.width);
			scaleY = Math.max(scaleY, shadowData.getHeight()/(float)shadow.height);

			if(shadow.offsetX < toffsetX) toffsetX = shadow.offsetX;
			if(shadow.offsetY < toffsetY) toffsetY = shadow.offsetY;
			if(shadow.offsetX+shadow.width > tx) tx = shadow.offsetX+shadow.width;
			if(shadow.offsetY+shadow.height > ty) ty = shadow.offsetY+shadow.height;
		}

		twidth = tx-toffsetX;
		theight = ty-toffsetY;

		int texWidth = (int) (twidth*scaleX);
		int texHeight = (int) (theight*scaleY);

		IntBuffer tdata = ByteBuffer.allocateDirect(texWidth * texHeight * 4)
				.order(ByteOrder.nativeOrder())
				.asIntBuffer();

		int[] temp = new int[0];

		if(shadowData != null) {
			int sdWidth = (int) (shadow.width*scaleX);
			int sdHeight = (int) (shadow.height*scaleY);

			shadowData = shadowData.convert(sdWidth, sdHeight);

			int hoffX = (int) ((shadow.offsetX-toffsetX)*scaleX);
			int hoffY = (int) ((shadow.offsetY-toffsetY)*scaleY);

			if(temp.length < sdWidth) temp = new int[sdWidth];

			IntBuffer shadowBfr = shadowData.getReadData32();

			for(int y = 0;y != sdHeight;y++) {
				shadowBfr.position(y*sdWidth);
				shadowBfr.get(temp, 0, sdWidth);

				for(int x = 0;x != sdWidth;x++) {
					if(temp[x] == 0) continue;
					tdata.put((y+hoffY)*texWidth+hoffX+x, (temp[x]&0xFF)<<16); // move alpha to green
				}
			}
		}

		if(torsoData != null) {
			int tdWidth = (int) (torso.width*scaleX);
			int tdHeight = (int) (torso.height*scaleY);

			torsoData = torsoData.convert(tdWidth, tdHeight);

			int toffX = (int) ((torso.offsetX-toffsetX)*scaleX);
			int toffY = (int) ((torso.offsetY-toffsetY)*scaleY);

			if(temp.length < tdWidth) temp = new int[tdWidth];

			IntBuffer torsoBfr = torsoData.getReadData32();

			for(int y = 0;y != tdHeight;y++) {
				torsoBfr.position(y*tdWidth);
				torsoBfr.get(temp, 0, tdWidth);

				for(int x = 0;x != tdWidth;x++) {
					if(temp[x] == 0) continue;
					tdata.put((y+toffY)*texWidth+toffX+x, (temp[x]&0xFF00)|0xFF000000); // strip out everything except blue channel and set full red channel
				}
			}
		}

		int dWidth = (int) (width*scaleX);
		int dHeight = (int) (height*scaleY);

		data = data.convert(dWidth, dHeight);

		int soffX = (int) ((offsetX-toffsetX)*scaleX);
		int soffY = (int) ((offsetY-toffsetY)*scaleY);

		if(temp.length < dWidth) temp = new int[dWidth];

		IntBuffer bfr = data.getReadData32();

		for(int y = 0;y != dHeight;y++) {
			bfr.position(y*dWidth);
			bfr.get(temp, 0, dWidth);

			for(int x = 0;x != dWidth;x++) {
				if(temp[x] != 0) tdata.put((y+soffY)*texWidth+soffX+x, temp[x]);
			}
		}
		return ImageData.of(tdata, texWidth, texHeight);
	}
}
