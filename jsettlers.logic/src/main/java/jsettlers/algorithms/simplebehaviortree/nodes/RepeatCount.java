package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.Decorator;
import jsettlers.algorithms.simplebehaviortree.IIntegerSupplier;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class RepeatCount<T> extends Decorator<T> {

	private static final long serialVersionUID = 3896956062570134419L;

	private final IIntegerSupplier<T> iterationsSupplier;

	public RepeatCount(IIntegerSupplier<T> times, Node<T> child) {
		super(child);
		iterationsSupplier = times;
	}

	@Override
	protected void onOpen(Tick<T> tick) {
		tick.setProperty(getId(), iterationsSupplier.apply(tick.target));
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		int iterations = tick.getProperty(getId());

		// zero iterations -> do nothing and return SUCCESS
		for(int i = iterations; i > 0; i--) {
			NodeStatus newStatus = child.execute(tick);
			switch (newStatus) {
				// try again next time
				case RUNNING:
					tick.setProperty(getId(), i);
					// stop on failure
				case FAILURE:
					return newStatus;
					// continue on success
				case SUCCESS:
					break;
			}
		}

		return NodeStatus.SUCCESS;
	}
}