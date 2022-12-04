package jsettlers.algorithms.simplebehaviortree;

public class Root<T> extends Decorator<T> {
	private static final long serialVersionUID = 4857616270171506110L;

	private         int     maxID = -1;
	private int invocationDelay = 0;

	public int getChildrenCount() {
		return maxID + 1;
	}

	public Root(Node<T> child) {
		super(child);
		maxID = initiate(-1);
	}

	public void setInvocationDelay(int invocationDelay) {
		this.invocationDelay = invocationDelay;
	}

	public int getInvocationDelay() {
		return invocationDelay;
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		return child.execute(tick);
	}
}
