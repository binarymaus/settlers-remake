package jsettlers.algorithms.simplebehaviortree;

public abstract class Leaf<T> extends Node<T> {
	@Override
	public Node<T> findNode(int id) {
		if(getId() == id) return this;

		return null;
	}
}
