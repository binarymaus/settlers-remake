package jsettlers.logic.map.random.grid;

import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.player.IPlayerable;

public class BuildingObject implements MapObject, IPlayerable {

	private final EBuildingType type;
	private final byte player;

	public BuildingObject(EBuildingType type, byte player) {
		this.type = type;
		this.player = player;
	}

	public EBuildingType getType() {
		return type;
	}

	@Override
	public byte getPlayer() {
		return player;
	}

}
