package jsettlers.logic.movable.modelmap;

import jsettlers.common.movable.EDirection;

public enum EMovingDirection {
	NORTH_WEST(EDirection.NORTH_WEST),
	NORTH_EAST(EDirection.NORTH_EAST),
	WEST(EDirection.WEST),
	EAST(EDirection.EAST),
	SOUTH_WEST(EDirection.SOUTH_WEST),
	SOUTH_EAST(EDirection.SOUTH_EAST),
	VARIABLE(null),
	;

	private final EDirection realDirection;

	EMovingDirection(EDirection realDirection) {
		this.realDirection = realDirection;
	}

	public EDirection getRealDirection() {
		return realDirection;
	}
}
