package jsettlers.common.menu;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.player.ECivilisation;

public interface IMultiplayerSlot {
	IMultiplayerPlayer getPlayer();

	EPlayerType getType();

	ECivilisation getCivilisation();

	byte getTeam();

	byte getPosition();
}
