/*******************************************************************************
 * Copyright (c) 2015 - 2017
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
package go.graphics;

import java.util.Locale;

/**
 * This class represents a color with an alpha value.
 * 
 * @author Michael Zangl
 */
public final class Color {

	private static final int SHIFT_ARGB_A = 24;
	private static final int SHIFT_ARGB_R = 16;
	private static final int SHIFT_ARGB_G = 8;
	private static final int SHIFT_ARGB_B = 0;
	private static final int ARGB_FIELD_MAX = 0xff;
	private static final int SHORT_SHIFT_RED = 12;
	private static final int SHORT_SHIFT_GREEN = 8;
	private static final int SHORT_SHIFT_BLUE = 4;
	private static final int SHORT_FIELD_MAX = 0xf;
	private static final int SHORT_MASK_ALPHA = 0xf;

	/**
	 * Converts a color given in float values to ARGB.
	 * 
	 * @param red
	 *            The red component. Range 0..1
	 * @param green
	 *            The green component. Range 0..1
	 * @param blue
	 *            The blue component. Range 0..1
	 * @param alpha
	 *            The alpha component. Range 0..1
	 * @return The color in argb notation.
	 */
	public static int getARGB(float red, float green, float blue,
			float alpha) {
		return floatToARGBField(alpha) << SHIFT_ARGB_A
				| floatToARGBField(red) << SHIFT_ARGB_R
				| floatToARGBField(green) << SHIFT_ARGB_G
				| floatToARGBField(blue) << SHIFT_ARGB_B;
	}

	private static int floatToARGBField(float f) {
		return floatToAnyField(f, ARGB_FIELD_MAX);
	}

	private static int floatToAnyField(float f, int fieldMax) {
		return (int) (f * fieldMax) & fieldMax;
	}

	/**
	 * Convert a 16 bit color to a 32 bit color
	 * 
	 * @param color16bit
	 *            The 16 bit color in
	 * @return The 32 bit color;
	 */
	public static int convertTo32Bit(int color16bit) {
		// TODO: Make faster
		float red = (float) ((color16bit >> SHORT_SHIFT_RED) & SHORT_FIELD_MAX) / SHORT_FIELD_MAX;
		float green = (float) ((color16bit >> SHORT_SHIFT_GREEN) & SHORT_FIELD_MAX) / SHORT_FIELD_MAX;
		float blue = (float) ((color16bit >> SHORT_SHIFT_BLUE) & SHORT_FIELD_MAX) / SHORT_FIELD_MAX;
		float alpha = (float) (color16bit & SHORT_MASK_ALPHA) / SHORT_FIELD_MAX;
		return Color.getARGB(red, green, blue, alpha);
	}
}
