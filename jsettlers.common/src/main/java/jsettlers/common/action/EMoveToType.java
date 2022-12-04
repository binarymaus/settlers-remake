package jsettlers.common.action;

public enum EMoveToType {
	DEFAULT(true, true),
	FORCED(false, false),
	/**
	 * patrol (for soldiers; behaves like {@link #DEFAULT} in all other cases)
	 */
	PATROL(true, true);
	
	public static EMoveToType[] VALUES = values();

	private final boolean attackOnTheWay;
	private final boolean workOnDestination;

	EMoveToType(boolean attackOnTheWay, boolean workOnDestination) {
		this.attackOnTheWay = attackOnTheWay;
		this.workOnDestination = workOnDestination;
	}

	public boolean isAttackOnTheWay() {
		return attackOnTheWay;
	}

	public boolean isWorkOnDestination() {
		return workOnDestination;
	}
}