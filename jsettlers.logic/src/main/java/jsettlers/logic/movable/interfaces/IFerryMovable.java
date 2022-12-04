package jsettlers.logic.movable.interfaces;

import jsettlers.common.movable.IGraphicsFerry;

public interface IFerryMovable extends IAttackableMovable, IGraphicsFerry {

	void unloadFerry();

	boolean addPassenger(IAttackableHumanMovable movable);
}
