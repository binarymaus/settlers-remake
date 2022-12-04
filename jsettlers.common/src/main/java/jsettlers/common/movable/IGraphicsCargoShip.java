package jsettlers.common.movable;

import jsettlers.common.material.EMaterialType;

public interface IGraphicsCargoShip extends IGraphicsMovable {

	int getNumberOfCargoStacks();

	EMaterialType getCargoType(int stack);

	int getCargoCount(int stack);
}
