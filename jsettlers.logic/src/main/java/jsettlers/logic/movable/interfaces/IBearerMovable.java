package jsettlers.logic.movable.interfaces;

import jsettlers.common.movable.EMovableType;

public interface IBearerMovable extends ILogicMovable {

	ILogicMovable convertTo(EMovableType newMovableType);
}
