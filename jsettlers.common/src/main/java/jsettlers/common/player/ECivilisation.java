/*******************************************************************************
 * Copyright (c) 2015 - 2017
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.common.player;

import jsettlers.common.buildings.EBuildingType;

/**
 * @author codingberlin
 */
public enum ECivilisation {
	ROMAN(1),
	EGYPTIAN(2),
	ASIAN(3),
	AMAZON(4);

	private int fileIndex;
	public final int ordinal;

	public static final ECivilisation[] VALUES = values();

	@Deprecated
	public static final ECivilisation REPLACE_ME = ROMAN;

	ECivilisation(int fileIndex) {
		ordinal = ordinal();
		this.fileIndex = fileIndex;
	}

	public int getFileIndex() {
		return fileIndex;
	}

	public EBuildingType getMannaBuilding() {
		switch (this) {
			case EGYPTIAN:
				return EBuildingType.BREWERY;
			case ASIAN:
				return EBuildingType.DISTILLERY;
			case AMAZON:
				return EBuildingType.MEAD_BREWERY;
			default:
				return EBuildingType.WINEGROWER;
		}
	}
}
