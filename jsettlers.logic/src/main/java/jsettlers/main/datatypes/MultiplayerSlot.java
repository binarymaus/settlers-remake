package jsettlers.main.datatypes;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.IMultiplayerPlayer;
import jsettlers.common.menu.IMultiplayerSlot;
import jsettlers.common.player.ECivilisation;
import jsettlers.network.common.packets.SlotInfoPacket;

public class MultiplayerSlot implements IMultiplayerSlot {

	private final IMultiplayerPlayer player;
	private final EPlayerType type;
	private final ECivilisation civilisation;
	private final byte team;
	private final byte position;


	public MultiplayerSlot(SlotInfoPacket packet) {
		this(packet, null);
	}

	public MultiplayerSlot(SlotInfoPacket packet, IMultiplayerPlayer player) {
		this.player = player;
		this.team = packet.getTeam();
		this.position = packet.getPosition();
		this.civilisation = getCivilisation(packet.getCivilisation());
		this.type = getPlayerType(packet.getPlayerType());
	}

	private EPlayerType getPlayerType(byte playerType) {
		if(playerType >= EPlayerType.VALUES.length || playerType < 0) {
			return player != null ? EPlayerType.HUMAN : EPlayerType.AI_VERY_HARD;
		} else {
			return EPlayerType.VALUES[playerType];
		}
	}

	private static ECivilisation getCivilisation(byte civilisation) {
		if(civilisation >= ECivilisation.VALUES.length || civilisation < 0) {
			return ECivilisation.ROMAN;
		} else {
			return ECivilisation.VALUES[civilisation];
		}
	}

	@Override
	public IMultiplayerPlayer getPlayer() {
		return player;
	}

	@Override
	public EPlayerType getType() {
		return type;
	}

	@Override
	public ECivilisation getCivilisation() {
		return civilisation;
	}

	@Override
	public byte getTeam() {
		return team;
	}

	@Override
	public byte getPosition() {
		return position;
	}
}
