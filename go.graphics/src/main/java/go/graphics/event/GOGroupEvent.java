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
package go.graphics.event;

import java.util.Set;

import go.graphics.event.command.EModifier;

/**
 * This is a go key event.
 * 
 * @author michael
 *
 */
public class GOGroupEvent extends SingleHandlerGoEvent {
	private final String keyCode;
	private Set<EModifier> modifiers;

	/**
	 * Creates a new key event for a given key code.
	 * 
	 * @param keyCode
	 *            The key code.
	 */
	public GOGroupEvent(String keyCode, Set<EModifier> modifiers) {
		this.keyCode = keyCode;
		this.modifiers = modifiers;
	}

	/**
	 * Gets the key code the event has.
	 * 
	 * @return The key code.
	 */
	public String getKeyCode() {
		return keyCode;
	}

		/**
	 * Gets the key code the event has.
	 * 
	 * @return The key code.
	 */
	public Set<EModifier> getModifiers() {
		return modifiers;
	}
}
