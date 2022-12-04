/*******************************************************************************
 * Copyright (c) 2020
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
package jsettlers.logic.map.loading.original.data;

import java.io.Serializable;

public final class OriginalSurviveDurationWinCondition implements Serializable {
	private static final long serialVersionUID = 1;

	private final byte playerId;
	private final int time;

	public OriginalSurviveDurationWinCondition(byte playerId, int time) {
		this.playerId = playerId;
		this.time = time;
	}

	public byte getPlayerId() {
		return playerId;
	}

	public int getTime() {
		return time;
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof OriginalSurviveDurationWinCondition) {
			OriginalSurviveDurationWinCondition obj2 = (OriginalSurviveDurationWinCondition) obj;
			return obj2.playerId == playerId && obj2.time == time;
		} else {
			return false;
		}
	}
}
