package jsettlers.ai.army;

import java.util.Set;

public abstract class ArmyModule {
	protected final ArmyFramework parent;

	public ArmyModule(ArmyFramework parent) {
		this.parent = parent;
		parent.addModule(this);
	}

	/**
	 * ticks every 10 seconds
	 *
	 * @param soldiersWithOrders List of soldiers that already received an order
	 */
	public abstract void applyHeavyRules(Set<Integer> soldiersWithOrders);

	/**
	 * ticks every second
	 *
	 * @param soldiersWithOrders List of soldiers that already received an order
	 */
	public abstract void applyLightRules(Set<Integer> soldiersWithOrders);
}
