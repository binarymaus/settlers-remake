package jsettlers.common.movable;

import java.util.List;

public interface IGraphicsFerry extends IGraphicsMovable {

	List<? extends IGraphicsMovable> getPassengers();
}
