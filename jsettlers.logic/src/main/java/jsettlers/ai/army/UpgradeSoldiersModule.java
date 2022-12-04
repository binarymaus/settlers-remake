package jsettlers.ai.army;

import jsettlers.common.movable.ESoldierType;

import java.util.Set;

public class UpgradeSoldiersModule extends ArmyModule {

	private static final ESoldierType[] SOLDIER_UPGRADE_ORDER = new ESoldierType[] { ESoldierType.BOWMAN, ESoldierType.PIKEMAN, ESoldierType.SWORDSMAN };

	public UpgradeSoldiersModule(ArmyFramework parent) {
		super(parent);
	}

	private void upgradeSoldiers() {
		for (ESoldierType type : SOLDIER_UPGRADE_ORDER) {
			if (parent.canUpgradeSoldiers(type)) {
				parent.upgradeSoldiers(type);
			}
		}
	}

	@Override
	public void applyHeavyRules(Set<Integer> soldiersWithOrders) {
		upgradeSoldiers();
	}

	@Override
	public void applyLightRules(Set<Integer> soldiersWithOrders) {

	}
}
