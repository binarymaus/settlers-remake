package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.Decorator;
import jsettlers.algorithms.simplebehaviortree.IBooleanConditionFunction;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class Repeat<T> extends Decorator<T> {
    /*
        Run the child if condition=SUCCESS
        As long as the child is running the condition is not checked

        Return RUNNING if child=RUNNING || condition=RUNNING
        Return SUCCESS if condition=FAILURE
        Return FAILURE if child=FAILURE
     */

	public enum Policy {
		PREEMPTIVE,
		NONPREEMPTIVE
	}

	private static final long serialVersionUID = -661870259301299858L;

	private final IBooleanConditionFunction<T> condition;
	private final Policy  policy;

	public Repeat(IBooleanConditionFunction<T> condition, Node<T> child) {
		this(Policy.NONPREEMPTIVE, condition, child);
	}

	public Repeat(Policy policy, IBooleanConditionFunction<T> condition, Node<T> child) {
		super(child);
		this.condition = condition;
		this.policy = policy;
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		while(true) {
			if((policy == Policy.PREEMPTIVE || !tick.isOpen(child)) && !condition.test(tick.target)) {
				return NodeStatus.SUCCESS;
			}

			NodeStatus status = child.execute(tick);
			switch (status) {
				case SUCCESS:
					break;
				case RUNNING:
					return NodeStatus.RUNNING;
				case FAILURE:
					return NodeStatus.FAILURE;
			}
		}
	}
}