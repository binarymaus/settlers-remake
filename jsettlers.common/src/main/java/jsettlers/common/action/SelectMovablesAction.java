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
package jsettlers.common.action;
import java.util.List;
import jsettlers.common.selectable.ISelectable;;

/**
 * This class hold special information for the action type {@link EActionType#SELECT_AREA}.
 * 
 * @author michael
 */
public class SelectMovablesAction extends Action {
	private final List<ISelectable> selection;

	/**
	 * Creates a new select area action.
	 * 
	 * @param area
	 *            The area.
	 */
	public SelectMovablesAction(List<ISelectable> selectables) {
		super(EActionType.SELECT_MOVABLES);
		this.selection = selectables;		
	}

	/**
	 * Gets the selected movables.
	 * 
	 * @return The selected movables.
	 */
	public List<ISelectable> getSelection() {
		return this.selection;
	}
}
