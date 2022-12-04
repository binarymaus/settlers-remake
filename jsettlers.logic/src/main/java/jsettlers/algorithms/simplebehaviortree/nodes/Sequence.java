package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.Composite;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class Sequence<T> extends Composite<T> {
	private static final long serialVersionUID = -6313424360855786743L;

	public Sequence(Node<T>[] children) {
		super(children);
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		int index = tick.getProperty(getId());

		for (; index < children.size(); index++) {
			NodeStatus status = children.get(index).execute(tick);

			if(status == NodeStatus.FAILURE) {
				return NodeStatus.FAILURE;
			} else if(status == NodeStatus.RUNNING) {
				tick.setProperty(getId(), index);
				return NodeStatus.RUNNING;
			}
		}
		return NodeStatus.SUCCESS;
	}

	@Override
	protected void onOpen(Tick<T> tick) {
		tick.setProperty(getId(), 0);
	}

	@Override
	protected void onClose(Tick<T> tick) {
		children.get(tick.getProperty(getId())).close(tick);
	}
}
