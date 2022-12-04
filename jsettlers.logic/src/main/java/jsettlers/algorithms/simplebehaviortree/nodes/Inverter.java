package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.Decorator;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class Inverter<T> extends Decorator<T> {
	private static final long serialVersionUID = -3568446114722874065L;

	public Inverter(Node<T> child) {
		super(child);
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		NodeStatus result = child.execute(tick);
		switch (result) {
			case SUCCESS:
				return NodeStatus.FAILURE;
			case FAILURE:
				return NodeStatus.SUCCESS;
			case RUNNING:
				return NodeStatus.RUNNING;
			default:
				throw new IllegalStateException("Unknown NodeStatus: " + result);
		}
	}
}
