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
package jsettlers.common.selectable;

/**
 * Types of selections. This defines groups that can be selected together. <br>
 * The order is given by the priority (high value = higher priority)
 * 
 * @author Andreas Eberle
 * 
 */
public enum ESelectionType {
	BUILDING(1),
	PEOPLE(),
	PRIESTS(true),
	SPECIALISTS(),
	SOLDIERS(),
	SHIPS();

	public final int priority;
	public final int maxSelected;
	public boolean perPlayer;

	ESelectionType() {
		this(Integer.MAX_VALUE, false);
	}

	ESelectionType(int maxSelected) {
		this(maxSelected, false);
	}

	ESelectionType(boolean perPlayer) {
		this(Integer.MAX_VALUE, perPlayer);
	}

	ESelectionType(int maxSelected, boolean perPlayer) {
		this.perPlayer = perPlayer;
		this.maxSelected = maxSelected;
		this.priority = super.ordinal();
	}
}
