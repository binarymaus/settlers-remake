package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.common.CommonConstants;
import jsettlers.algorithms.simplebehaviortree.Decorator;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class Debug<T> extends Decorator<T> {
	private static final long serialVersionUID = 9019598003328102086L;

	private final String message;

	public Debug(String message) {
		super(null);
		this.message = message;
	}

	public Debug(String message, Node<T> child) {
		super(child);
		this.message = message;
	}

	@Override
	public NodeStatus onTick(Tick<T> tick) {
		if (CommonConstants.DEBUG_BEHAVIOR_TREES) {
			//System.out.println(indent(tick, message));
		}

		if (child != null) {
			NodeStatus result = child.execute(tick);

			if (CommonConstants.DEBUG_BEHAVIOR_TREES) {
				//System.out.println(indent(tick, message + ": " + result));
			}

			return result;
		}

		return NodeStatus.SUCCESS;
	}

	private String indent(Tick<T> tick, String message) {
		return message;
	}
}
