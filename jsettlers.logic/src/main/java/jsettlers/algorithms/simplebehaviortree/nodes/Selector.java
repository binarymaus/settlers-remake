package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.Composite;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class Selector<T> extends Composite<T> {
	private static final long serialVersionUID = 6187523767823138311L;

	public Selector(Node<T>[] children) {
		super(children);
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		int index = tick.getProperty(getId());

		for (; index < children.size(); index++) {
			NodeStatus status = children.get(index).execute(tick);

			if(status == NodeStatus.SUCCESS) {
				return NodeStatus.SUCCESS;
			} else if(status == NodeStatus.RUNNING) {
				tick.setProperty(getId(), index);
				return NodeStatus.RUNNING;
			}
		}
		return NodeStatus.FAILURE;
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
