/*******************************************************************************
 * Copyright (c) 2015, 2016
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
package jsettlers.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import jsettlers.common.ai.EPlayerType;
import jsettlers.logic.player.InitialGameState;
import jsettlers.logic.player.PlayerSetting;

/**
 * @author Andreas Eberle
 */
public class ReplayStartInformation implements Serializable {

	private String mapName;
	private String mapId;
	private InitialGameState initialGameState;

	public ReplayStartInformation() {
	}

	public ReplayStartInformation(String mapName, String mapId, InitialGameState initialGameState) {
		this.initialGameState = initialGameState;
		this.mapName = mapName;
		this.mapId = mapId;
	}

	public String getMapName() {
		return mapName;
	}

	public String getMapId() {
		return mapId;
	}

	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeUTF(mapName);
		dos.writeUTF(mapId);

		initialGameState.serialize(dos);
	}

	public void deserialize(DataInputStream dis) throws IOException {
		mapName = dis.readUTF();
		mapId = dis.readUTF();

		initialGameState = new InitialGameState(dis);
	}

	public InitialGameState getInitialGameState() {
		return initialGameState;
	}

	public InitialGameState getReplayableGameState() {
		return initialGameState.clone();
	}
}
