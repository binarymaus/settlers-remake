package jsettlers.ai.army;

import jsettlers.common.material.EMaterialType;
import jsettlers.common.movable.EMovableType;

import java.util.Set;

public class SoldierProductionModule extends ArmyModule {

	private static final byte MIN_SWORDSMEN_COUNT = 10;
	private static final byte MIN_PIKEMEN_COUNT = 20;
	public static final int BOWMEN_COUNT_OF_KILLING_INFANTRY = 300;

	public SoldierProductionModule(ArmyFramework parent) {
		super(parent);
	}
	
	private void adjustSoldierProduction() {
		int missingSwordsmenCount = Math.max(0, MIN_SWORDSMEN_COUNT - parent.aiStatistics.getCountOfMovablesOfPlayer(parent.getPlayer(), EMovableType.SWORDSMEN));
		int missingPikemenCount = Math.max(0, MIN_PIKEMEN_COUNT - parent.aiStatistics.getCountOfMovablesOfPlayer(parent.getPlayer(), EMovableType.PIKEMEN));
		int bowmenCount = parent.aiStatistics.getCountOfMovablesOfPlayer(parent.getPlayer(), EMovableType.BOWMEN);

		if (missingSwordsmenCount > 0) {
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.SWORD, missingSwordsmenCount);
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.SPEAR, 0);
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.BOW, 0);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.SWORD, 0F);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.SPEAR, 1F);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.BOW, 0F);
		} else if (missingPikemenCount > 0) {
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.SWORD, 0);
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.SPEAR, missingPikemenCount);
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.BOW, 0);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.SWORD, 0F);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.SPEAR, 0.3F);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.BOW, 1F);
		} else if (bowmenCount * parent.getPlayer().getCombatStrengthInformation().getCombatStrength(false) < SoldierProductionModule.BOWMEN_COUNT_OF_KILLING_INFANTRY) {
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.SWORD, 0);
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.SPEAR, 0);
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.BOW, 0);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.SWORD, 0F);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.SPEAR, 0.3F);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.BOW, 1F);
		} else {
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.SWORD, 0);
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.SPEAR, 0);
			parent.setNumberOfFutureProducedMaterial(parent.getPlayerId(), EMaterialType.BOW, 0);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.SWORD, 0F);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.SPEAR, 0F);
			parent.setRatioOfMaterial(parent.getPlayerId(), EMaterialType.BOW, 1F);
		}
	}

	@Override
	public void applyHeavyRules(Set<Integer> soldiersWithOrders) {
		adjustSoldierProduction();
	}

	@Override
	public void applyLightRules(Set<Integer> soldiersWithOrders) {

	}
}
