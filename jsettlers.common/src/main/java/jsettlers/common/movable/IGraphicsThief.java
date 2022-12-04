package jsettlers.common.movable;

public interface IGraphicsThief extends IGraphicsMovable {

	boolean isUncoveredBy(byte teamId);
}
