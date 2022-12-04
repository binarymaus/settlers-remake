package jsettlers.logic.map.grid.partition.manager.settings;

import jsettlers.common.map.partition.ISingleProfessionLimit;

import java.io.Serializable;

public abstract class SingleProfessionLimit implements ISingleProfessionLimit, Serializable {

	private static final long serialVersionUID = -1146142499912621153L;
	protected final ProfessionSettings parent;

	protected int currentAmount;
	protected int tempAmount;

	protected SingleProfessionLimit(ProfessionSettings parent, SingleProfessionLimit predecessor) {
		this.parent = parent;

		this.currentAmount = predecessor.currentAmount;
		this.tempAmount = predecessor.tempAmount;
	}

	protected SingleProfessionLimit(ProfessionSettings parent) {
		this.parent = parent;

		this.currentAmount = 0;
		this.tempAmount = 0;
	}

	public abstract void setLimit(int value, boolean relative);

	public final void incrementRealAmount() {
		currentAmount++;
	}

	public final void incrementTempAmount() {
		tempAmount++;
	}

	public final void decrementRealAmount() {
		currentAmount--;
	}

	public final void decrementTempAmount() {
		tempAmount--;
	}

	public final void resetCount() {
		currentAmount = 0;
	}

	@Override
	public final int getCurrentCount() {
		return currentAmount + tempAmount;
	}

	@Override
	public final float getCurrentRatio() {
		return getCurrentCount() / (float) parent.getWorkerCount();
	}

	public abstract float getRemainingAmount();
}
