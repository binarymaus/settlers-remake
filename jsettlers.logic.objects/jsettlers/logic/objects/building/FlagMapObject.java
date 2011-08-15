package jsettlers.logic.objects.building;

import jsettlers.common.mapobject.EMapObjectType;
import jsettlers.common.player.IPlayerable;
import jsettlers.logic.map.hex.interfaces.AbstractHexMapObject;

public class FlagMapObject extends AbstractHexMapObject implements IPlayerable {

	private final EMapObjectType flagType;
	private final byte player;

	public FlagMapObject(EMapObjectType flagType, byte player) {
		this.flagType = flagType;
		this.player = player;
		assert flagType == EMapObjectType.FLAG_DOOR || flagType == EMapObjectType.FLAG_ROOF : "flag must be a flag";

	}

	@Override
	public EMapObjectType getObjectType() {
		return flagType;
	}

	@Override
	public float getStateProgress() {
		return 0;
	}

	@Override
	public boolean cutOff() {
		return false;
	}

	@Override
	public boolean isBlocking() {
		return false;
	}

	@Override
	public boolean canBeCut() {
		return false;
	}

	@Override
	public byte getPlayer() {
		return player;
	}

}
