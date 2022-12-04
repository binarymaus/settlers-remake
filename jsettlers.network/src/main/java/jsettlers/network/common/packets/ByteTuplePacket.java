package jsettlers.network.common.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import jsettlers.network.infrastructure.channel.packet.Packet;

public class ByteTuplePacket extends Packet {

	private byte valueA;
	private byte valueB;

	public ByteTuplePacket() {
	}

	public ByteTuplePacket(byte valueA, byte valueB) {
		this.valueA = valueA;
		this.valueB = valueB;
	}

	public byte getValueA() {
		return valueA;
	}

	public byte getValueB() {
		return valueB;
	}

	@Override
	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeByte(valueA);
		dos.writeByte(valueB);
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		valueA = dis.readByte();
		valueB = dis.readByte();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ByteTuplePacket that = (ByteTuplePacket) o;
		return valueA == that.valueA && valueB == that.valueB;
	}

	@Override
	public int hashCode() {
		return Objects.hash(valueA, valueB);
	}
}
