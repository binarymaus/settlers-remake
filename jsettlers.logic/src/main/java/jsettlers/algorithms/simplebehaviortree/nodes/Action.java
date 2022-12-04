package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.INodeStatusActionConsumer;
import jsettlers.algorithms.simplebehaviortree.INodeStatusActionFunction;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Leaf;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class Action<T> extends Leaf<T> {
	private static final long serialVersionUID = -4535362950446826714L;

	private final INodeStatusActionFunction<T> action;

	public Action(INodeStatusActionFunction<T> action) {
		super();
		this.action = action;
	}

	public Action(INodeStatusActionConsumer<T> action) {
		this(t -> {
			action.accept(t);
			return NodeStatus.SUCCESS;
		});
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		return action.apply(tick.target);
	}
}
