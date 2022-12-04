package jsettlers.algorithms.simplebehaviortree;

import java.io.Serializable;

public abstract class Node<T> implements Serializable {
	private static final long serialVersionUID = -4544227752720944971L;

	private int     id = -1;

	public int getId() { return id; }

	public NodeStatus execute(Tick<T> tick) {
		if(!tick.isOpen(this)) {
			open(tick);
		}
		enter(tick);
		NodeStatus status = this.tick(tick);
		exit(tick);
		if(status != NodeStatus.RUNNING) {
			close(tick);
		}
		return status;
	}

	private void enter(Tick<T> tick) {
		tick.visitNode(this);
		onEnter(tick);
	}

	private void open(Tick<T> tick) {
		onOpen(tick);
	}

	private NodeStatus tick(Tick<T> tick) {
		tick.tickNode(this);
		return onTick(tick);
	}

	public final void close(Tick<T> tick) {
		if(tick.isOpen(this)) {
			tick.leaveNode(this);
			onClose(tick);
		}
	}

	private void exit(Tick<T> tick) {
		onExit(tick);
	}

	protected void onEnter(Tick<T> tick) { }

	protected void onOpen(Tick<T> tick) { }

	protected abstract NodeStatus onTick(Tick<T> tick);

	protected void onClose(Tick<T> tick) { }

	protected void onExit(Tick<T> tick) { }

	int initiate(int maxId) {
		assert id == -1;

		return this.id = ++maxId;
	}

	public abstract Node<T> findNode(int id);

}
