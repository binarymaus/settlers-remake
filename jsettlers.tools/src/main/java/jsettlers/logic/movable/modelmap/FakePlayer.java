package jsettlers.logic.movable.modelmap;

import jsettlers.common.player.ECivilisation;
import jsettlers.common.player.EWinState;
import jsettlers.common.player.IPlayer;

public class FakePlayer implements IPlayer {

	private byte playerId;
	private ECivilisation civilisation;

	public FakePlayer() {
		playerId = 0;
		civilisation = ECivilisation.VALUES[0];
	}

	@Override
	public byte getPlayerId() {
		return playerId;
	}

	public void setPlayerId(byte playerId) {
		this.playerId = playerId;
	}

	@Override
	public byte getTeamId() {
		return 0;
	}

	@Override
	public EWinState getWinState() {
		return EWinState.UNDECIDED;
	}

	@Override
	public ECivilisation getCivilisation() {
		return civilisation;
	}

	public void setCivilisation(ECivilisation civilisation) {
		this.civilisation = civilisation;
	}
}
