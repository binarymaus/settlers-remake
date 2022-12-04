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
package jsettlers.common.movable;

import jsettlers.common.images.ImageLink;

public enum EEffectType {
	MOTIVATE_SWORDSMAN(30, "original_1_SETTLER_128_0"),
	INCREASED_MORALE(30, "original_1_SETTLER_129_0"),
	DEFEATISM(30, "original_1_SETTLER_130_0"),
	SHIELDED(30, "original_1_SETTLER_131_0"),
	NO_ARROWS(20, "original_1_SETTLER_132_0"),
	FROZEN(10, "original_1_SETTLER_133_0"),
	GREEN_THUMB(300, "original_1_SETTLER_134_0");

	private int time;
	private ImageLink link;

	EEffectType(int time, String imageLink) {
		this.time = time;
		link = ImageLink.fromName(imageLink);
	}

	public int getTime() {
		return time;
	}

	public ImageLink getImageLink() {
		return link;
	}

	public static final float GREEN_THUMB_GROW_FACTOR = 2f;

	public static final float SHIELDED_DAMAGE_FACTOR = 0.66f;

	public static final float DEFEATISM_DAMAGE_FACTOR = 0.5f;
	public static final float INCREASED_MORALE_DAMAGE_FACTOR = 2f;

	public static final float MOTIVATE_SWORDSMAN_ANIMATION_FACTOR = 0.5f;
	public static final float MOTIVATE_SWORDSMAN_DAMAGE_FACTOR = 1.33f;
}
