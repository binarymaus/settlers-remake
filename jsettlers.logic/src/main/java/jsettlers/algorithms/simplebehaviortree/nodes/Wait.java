package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.Decorator;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Tick;

/**
 * @author homoroselaps
 */

public class Wait<T> extends Decorator<T> {
	private static final long serialVersionUID = -6025244799010530015L;

	public Wait(Node<T> condition) {
		super(condition);
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		NodeStatus status = child.execute(tick);
		switch (status) {
			case SUCCESS:
				return NodeStatus.SUCCESS;
			default:
			case FAILURE:
				// emit running
			case RUNNING:
				return NodeStatus.RUNNING;
		}
	}
}
