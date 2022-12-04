package jsettlers.network.common.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.server.match.Slot;

public class SlotInfoPacket extends Packet {
	private byte team;
	private byte playerType;
	private byte civilisation;
	private byte position;

	public SlotInfoPacket() {
	}

	public SlotInfoPacket(Slot slot) {
		team = slot.getTeam();
		playerType = slot.getType();
		civilisation = slot.getCivilisation();
		position = slot.getPosition();
	}

	public SlotInfoPacket(byte team, byte playerType, byte civilisation, byte position) {
		this.team = team;
		this.playerType = playerType;
		this.civilisation = civilisation;
		this.position = position;
	}

	@Override
	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeByte(team);
		dos.writeByte(playerType);
		dos.writeByte(civilisation);
		dos.writeByte(position);
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		team = dis.readByte();
		playerType = dis.readByte();
		civilisation = dis.readByte();
		position = dis.readByte();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SlotInfoPacket that = (SlotInfoPacket) o;
		return team == that.team && playerType == that.playerType && civilisation == that.civilisation && position == that.position;
	}

	@Override
	public int hashCode() {
		return Objects.hash(team, playerType, civilisation, position);
	}

	@Override
	public String toString() {
		return "SlotInfoPacket{" +
				"team=" + team +
				", playerType=" + playerType +
				", civilisation=" + civilisation +
				", position=" + position +
				'}';
	}

	public byte getTeam() {
		return team;
	}

	public byte getPlayerType() {
		return playerType;
	}

	public byte getCivilisation() {
		return civilisation;
	}

	public byte getPosition() {
		return position;
	}
}
