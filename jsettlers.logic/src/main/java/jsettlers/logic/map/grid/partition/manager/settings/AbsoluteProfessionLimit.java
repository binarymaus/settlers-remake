package jsettlers.logic.map.grid.partition.manager.settings;

public class AbsoluteProfessionLimit extends SingleProfessionLimit {

	private static final long serialVersionUID = -6005696902851232760L;
	private int targetCount;

	public AbsoluteProfessionLimit(ProfessionSettings parent, int initialTargetCount) {
		super(parent);

		this.targetCount = initialTargetCount;
	}

	public AbsoluteProfessionLimit(ProfessionSettings parent, SingleProfessionLimit predecessor) {
		super(parent, predecessor);

		this.targetCount = predecessor.getTargetCount();
	}

	@Override
	public int getTargetCount() {
		return targetCount;
	}

	@Override
	public float getTargetRatio() {
		return targetCount / (float) parent.getWorkerCount();
	}

	@Override
	public void setLimit(int value, boolean relative) {
		if(relative) {
			targetCount += value;
		} else {
			targetCount = value;
		}

		if(targetCount < 0) {
			targetCount = 0;
		}
	}

	@Override
	public String toString() {
		return "AbsoluteProfessionLimit{" +
				"targetCount=" + targetCount +
				", parent=" + parent +
				", currentAmount=" + currentAmount +
				", tempAmount=" + tempAmount +
				'}';
	}

	@Override
	public boolean isRelative() {
		return false;
	}

	@Override
	public float getRemainingAmount() {
		return targetCount - getCurrentCount();
	}
}
