package jsettlers.algorithms.simplebehaviortree.nodes;

import java.util.Arrays;

import jsettlers.algorithms.simplebehaviortree.Composite;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class Parallel<T> extends Composite<T> {
	private static final long serialVersionUID = 3614671053589100247L;

    /*
        Run each child until it's completed
        Return SUCCESS when
        | ONE -> one child was successful
        | ALL -> all children were successful

        Return RUNNING when
        | preemptive = true -> successPolicy not fullfilled && any child of it's children still running
        | preemptive = false -> if any of it's children is still running

        Return FAILURE in all other cases
    */

	public enum Policy {
		ONE,
		ALL
	}

	private final Policy       successPolicy;
	private final boolean      preemptive;

	public Parallel(Policy successPolicy, boolean preemptive, Node<T>[] children) {
		super(children);
		this.successPolicy = successPolicy;
		this.preemptive = preemptive;
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		NodeStatus[] childStatus = tick.getProperty(getId());
		int successCount = 0;

		boolean anyRunning = false;
		for (int index = 0; index < childStatus.length; index++) {
			NodeStatus status = childStatus[index];

			if(status == NodeStatus.RUNNING) {
				childStatus[index] = status = children.get(index).execute(tick);
			}

			if(status == NodeStatus.SUCCESS) {
				successCount++;
			} else if(status == NodeStatus.RUNNING) {
				anyRunning = true;
			}
		}

		boolean successCondition = (successPolicy == Policy.ONE && successCount >= 1) || (successPolicy == Policy.ALL && successCount == children.size());

		if (anyRunning && (!preemptive || !successCondition)) {
			return NodeStatus.RUNNING;
		}

		tick.setProperty(getId(), null);

		if (successCondition) {
			return NodeStatus.SUCCESS;
		} else {
			return NodeStatus.FAILURE;
		}
	}

	@Override
	protected void onOpen(Tick<T> tick) {
		NodeStatus[] childStatus = new NodeStatus[children.size()];
		Arrays.fill(childStatus, NodeStatus.RUNNING);
		tick.setProperty(getId(), childStatus);
	}

	@Override
	protected void onClose(Tick<T> tick) {
		NodeStatus[] childStatus = tick.getProperty(getId());
		if(childStatus == null) return;

		for(int i = 0; i < childStatus.length; i++) {
			if(childStatus[i] == NodeStatus.RUNNING) {
				children.get(i).close(tick);
			}
		}
	}
}
