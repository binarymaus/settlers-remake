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
package jsettlers.network.common.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import java.util.Objects;
import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.server.match.Match;

/**
 * 
 * @author Andreas Eberle
 * 
 */
public class MatchInfoPacket extends Packet {
	private String id;
	private String matchName;
	private int maxPlayers;
	private MapInfoPacket mapInfo;
	private PlayerInfoPacket[] players;
	private SlotInfoPacket[] slots;

	public MatchInfoPacket() {
	}

	public MatchInfoPacket(String id, String matchName, int maxPlayers, MapInfoPacket mapInfo, PlayerInfoPacket[] players, SlotInfoPacket[] slots) {
		this.id = id;
		this.matchName = matchName;
		this.maxPlayers = maxPlayers;
		this.mapInfo = mapInfo;
		this.players = players;
		this.slots = slots;
	}

	public MatchInfoPacket(Match match) {
		this();
		id = match.getId();
		matchName = match.getName();
		maxPlayers = match.getMaxPlayers();
		mapInfo = match.getMap();
		players = match.getPlayerInfos();
		slots = match.getSlotInfos();
	}

	@Override
	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeUTF(id);
		dos.writeUTF(matchName);
		dos.writeInt(maxPlayers);
		mapInfo.serialize(dos);

		PlayerInfoPacket[] players = this.players;
		dos.writeInt(players.length);
		for (PlayerInfoPacket curr : players) {
			curr.serialize(dos);
		}

		SlotInfoPacket[] slots = this.slots;
		dos.writeInt(slots.length);
		for(SlotInfoPacket curr : slots) {
			curr.serialize(dos);
		}
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		id = dis.readUTF();
		matchName = dis.readUTF();
		maxPlayers = dis.readInt();
		mapInfo = new MapInfoPacket();
		mapInfo.deserialize(dis);

		int playersLength = dis.readInt();
		PlayerInfoPacket[] players = new PlayerInfoPacket[playersLength];
		for (int i = 0; i < playersLength; i++) {
			PlayerInfoPacket curr = new PlayerInfoPacket();
			curr.deserialize(dis);
			players[i] = curr;
		}
		this.players = players;

		int slotsLength = dis.readInt();
		SlotInfoPacket[] slots = new SlotInfoPacket[slotsLength];
		for (int i = 0; i < slotsLength; i++) {
			SlotInfoPacket curr = new SlotInfoPacket();
			curr.deserialize(dis);
			slots[i] = curr;
		}

		this.slots = slots;
	}

	public String getId() {
		return id;
	}

	public String getMatchName() {
		return matchName;
	}

	public MapInfoPacket getMapInfo() {
		return mapInfo;
	}

	public PlayerInfoPacket[] getPlayers() {
		return players;
	}

	public SlotInfoPacket[] getSlots() {
		return slots;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MatchInfoPacket that = (MatchInfoPacket) o;
		return maxPlayers == that.maxPlayers && Objects.equals(id, that.id) && Objects.equals(matchName, that.matchName) && Objects.equals(mapInfo, that.mapInfo) && Arrays.equals(players, that.players) && Arrays.equals(slots, that.slots);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(id, matchName, maxPlayers, mapInfo);
		result = 31 * result + Arrays.hashCode(players);
		result = 31 * result + Arrays.hashCode(slots);
		return result;
	}

	@Override
	public String toString() {
		return "MatchInfoPacket [id=" + id + ", matchName=" + matchName + ", maxPlayers=" + maxPlayers + ", mapInfo=" + mapInfo + ", players="
				+ Arrays.toString(players) + ", slots=" + Arrays.toString(slots) + "]";
	}
}
