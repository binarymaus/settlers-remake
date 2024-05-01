/*
 * Copyright (c) 2018
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
 */
package jsettlers.graphics.image.reader.custom.graphics;

import jsettlers.common.images.AnimationSequence;
import jsettlers.graphics.image.Image;
import jsettlers.graphics.image.sequence.Sequence;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.graphics.image.reader.DatFileReader;
import jsettlers.graphics.image.reader.WrappedAnimation;

class CustomMountainKingDatFile extends CustomDatFile {

	CustomMountainKingDatFile(DatFileReader fallback, ImageProvider imageProvider) {
		super(fallback, imageProvider);
	}

	@Override
	protected Sequence<Image> getCustom(int index) {
		if (index == 0) {
			var seq = new WrappedAnimation(imageProvider, new AnimationSequence("mountain_king_king_king", 0, 49));
			return seq;
		}
		else {
			System.out.println("Different index: " + index);
		}
		return null;

	}
}
