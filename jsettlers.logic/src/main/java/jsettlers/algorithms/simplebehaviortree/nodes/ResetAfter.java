package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.Decorator;
import jsettlers.algorithms.simplebehaviortree.INodeStatusActionConsumer;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class ResetAfter<T> extends Decorator<T> {

	private final INodeStatusActionConsumer<T> reset;

	public ResetAfter(INodeStatusActionConsumer<T> reset, Node<T> child) {
		super(child);

		this.reset = reset;
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		return child.execute(tick);
	}

	@Override
	protected void onClose(Tick<T> tick) {
		super.onClose(tick);

		reset.accept(tick.target);
	}
}
