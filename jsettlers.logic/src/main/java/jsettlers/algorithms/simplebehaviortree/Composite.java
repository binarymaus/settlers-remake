package jsettlers.algorithms.simplebehaviortree;

import java.util.List;

public abstract class Composite<T> extends Node<T> {
	private static final long serialVersionUID = 8795400757387672902L;

	protected final List<Node<T>> children;

	protected Composite(Node<T>[] children) {
		this.children = List.of(children);
	}

	@Override
	int initiate(int maxId) {
		maxId = super.initiate(maxId);

		for (Node<T> child : children) {
			maxId = child.initiate(maxId);
		}

		return maxId;
	}

	@Override
	public Node<T> findNode(int id) {
		if(getId() == id) {
			return this;
		} else if(getId() > id) {
			return null;
		} else {
			for(Node<T> child : children) {
				Node<T> target = child.findNode(id);

				if(target != null) return target;
			}
			return null;
		}
	}
}
