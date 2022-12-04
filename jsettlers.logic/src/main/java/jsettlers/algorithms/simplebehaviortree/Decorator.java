package jsettlers.algorithms.simplebehaviortree;

public abstract class Decorator<T> extends Node<T> {

	protected final Node<T> child;

	protected Decorator(Node<T> child) {
		this.child = child;
	}

	@Override
	int initiate(int maxId) {
		maxId = super.initiate(maxId);

		if(child != null) {
			maxId = child.initiate(maxId);
		}
		return maxId;
	}

	@Override
	public Node<T> findNode(int id) {
		if (getId() == id) {
			return this;
		} else if(getId() > id) {
			return null;
		} else {
			return child.findNode(id);
		}
	}

	@Override
	protected void onClose(Tick<T> tick) {
		child.close(tick);
	}
}
