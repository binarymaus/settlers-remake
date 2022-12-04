/*******************************************************************************
 * Copyright (c) 2016 - 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.ai.highlevel;

import static jsettlers.ai.highlevel.AiBuildingConstants.*;
import static jsettlers.common.buildings.EBuildingType.FISHER;

import java.util.BitSet;

import java.util.EnumMap;
import java.util.Map;
import jsettlers.algorithms.distances.DistancesCalculationAlgorithm;
import jsettlers.common.buildings.BuildingVariant;
import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.landscape.EResourceType;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.player.IPlayer;
import jsettlers.logic.map.grid.landscape.LandscapeGrid;
import jsettlers.logic.map.grid.partition.PartitionsGrid;

/**
 * This class calculates information about the map for the AI. At the moment it calculates how many buildings of a building type can be build on the map by each player. In a nutshell it divides the
 * landscape and resources of the map by the number of possible players. Then it determines the maximal possible iron mines and coal mines. Depending of mines to smiths ratio it calculates the maximal
 * number of possible smiths. Then it calculates the needed amount of food building by preferring fisher huts over farms. At least it calculates how many lumberjacks and stone cutters are needed to
 * build this economy including the necessary living houses to levy SOLDIERS. Now it checks if the calculated buildings have enough space to be build on the map. If not it reduces the number of smiths
 * unless it hits a threshold. Then it reduces the number of gold smith, winegrowers and big temples to a minimum set of this buildings. If again this are too much buildings. It keeps reducing smiths.
 *
 * @author codingberlin
 */
public class AiMapInformation {

	private static final double FISH_TO_FISHER_HUTS_RATIO = 80F / 1F;
	private static final double COAL_TO_COAL_MINES_RATIO = 100F / 1F;
	private static final double GEMSTONE_TO_GEM_MINES_RATIO = 100F / 1F;
	private static final double IRONORE_TO_IRON_MINES_RATIO = 100F / 1F;
	private static final float SWAMP_TO_RICE_FARM_RATIO = 60F;
	private static final float GRASS_TO_LUMBERJACK_RATIO = 1360F;
	private static final float GRASS_TO_FARM_RATIO = 6200F;
	private static final float STONE_TO_STONECUTTER_RATIO = 10F;
	private static final int MIN_SMITHS_BEFORE_MANNA_AND_GOLD_REDUCTION = 10;
	private static final int MIN_MANNA_PRODUCERS_BEFORE_GOLD_REDUCTION = 2;
	private static final int MIN_LUMBERJACK_COUNT = 3;
	private static final int FISHER_PENALTY_MIN_AMOUNT = 7;
	// throttle the number of FISHER after the 7th one
	// this prevents the AI from overfishing but also takes the high amount of resources on some maps into account
	public final BitSet[] wasFishNearByAtGameStart = new BitSet[ECivilisation.VALUES.length];
	private final AiPartitionResources defaultPartitionResources;
	private final byte numberOfPlayers;

	public AiMapInformation(PartitionsGrid partitionsGrid, LandscapeGrid landscapeGrid, AiPartitionResources defaultPartitionResources) {
		this.defaultPartitionResources = defaultPartitionResources;
		for(ECivilisation civ : ECivilisation.VALUES) {
			wasFishNearByAtGameStart[civ.ordinal] = calculateIsFishNearBy(partitionsGrid, landscapeGrid, civ);
		}

		numberOfPlayers = partitionsGrid.getNumberOfPlayers();
	}

	private BitSet calculateIsFishNearBy(PartitionsGrid partitionsGrid, LandscapeGrid landscapeGrid, ECivilisation civilisation) {
		return DistancesCalculationAlgorithm.calculatePositionsInDistance(partitionsGrid.getWidth(), partitionsGrid.getHeight(),
				(x, y) -> landscapeGrid.getResourceTypeAt(x, y) == EResourceType.FISH && landscapeGrid.getResourceAmountAt(x, y) > 0,
				FISHER.getVariant(civilisation).getWorkRadius());
	}

	public int[] getBuildingCounts(PlayerStatistic playerStatistic, IPlayer player) {
		Map<EResourceType, Long> resourceAmount = new EnumMap<>(EResourceType.class);
		for(EResourceType resource : EResourceType.VALUES) {
			resourceAmount.put(resource, Math.round(defaultPartitionResources.resourceCount[resource.ordinal] / (float)numberOfPlayers) + playerStatistic.partitionResources.resourceCount[resource.ordinal]);
		}

		long playersAndNeverlandGrass = Math.round(defaultPartitionResources.grassCount / (float)numberOfPlayers) + playerStatistic.partitionResources.grassCount;

		long playersAndNeverlandStone = Math.round(defaultPartitionResources.stoneCount / (float)numberOfPlayers) + playerStatistic.partitionResources.stoneCount;
		long playersAndNeverlandSwamp = Math.round(defaultPartitionResources.usableSwampCount / (float)numberOfPlayers) + playerStatistic.partitionResources.usableSwampCount;

		int maxFishermen = (int) Math.ceil(resourceAmount.get(EResourceType.FISH) / FISH_TO_FISHER_HUTS_RATIO);
		if(maxFishermen > FISHER_PENALTY_MIN_AMOUNT) maxFishermen = (int)Math.log(maxFishermen - FISHER_PENALTY_MIN_AMOUNT)*3 + FISHER_PENALTY_MIN_AMOUNT;
		maxFishermen = Math.max(1, maxFishermen);
		int maxCoalMines = (int) Math.ceil(resourceAmount.get(EResourceType.COAL) / COAL_TO_COAL_MINES_RATIO);
		int maxIronMines = (int) Math.ceil(resourceAmount.get(EResourceType.IRONORE) / IRONORE_TO_IRON_MINES_RATIO);
		int maxGoldMelts = resourceAmount.get(EResourceType.GOLDORE) > 0 ? 2 : 0;
		int maxGemsMines = (int) Math.ceil(resourceAmount.get(EResourceType.GEMSTONE) / GEMSTONE_TO_GEM_MINES_RATIO);
		if(player.getCivilisation() != ECivilisation.EGYPTIAN && player.getCivilisation() != ECivilisation.AMAZON) {
			maxGemsMines = 0;
		}

		if (maxIronMines > maxCoalMines / COAL_MINE_TO_IRON_MINE_RATIO + 1)
			maxIronMines = (int) Math.ceil(maxCoalMines / COAL_MINE_TO_IRON_MINE_RATIO + 1);
		if (maxCoalMines > maxIronMines * COAL_MINE_TO_IRON_MINE_RATIO + 1)
			maxCoalMines = (int) Math.ceil(maxIronMines * COAL_MINE_TO_IRON_MINE_RATIO + 1);
		int maxSmiths = (int) Math.floor((float) maxCoalMines / COAL_MINE_TO_SMITH_RATIO);
		int maxRiceFarms = (int) Math.floor((float) playersAndNeverlandSwamp / SWAMP_TO_RICE_FARM_RATIO);
		return calculateBuildingCounts(maxSmiths, maxFishermen, maxGoldMelts, maxGemsMines, 3, 1, maxRiceFarms, playersAndNeverlandGrass, playersAndNeverlandStone, player.getCivilisation());
	}

	private int[] calculateBuildingCounts(int numberOfWeaponSmiths, int maxFishermen, int maxGoldMelts, int maxGemsMines, int maxMannaProducers, int maxBigTemples, int maxRiceFarms, long grassTiles, long stoneCount, ECivilisation civilisation) {
		int[] buildingCounts = new int[EBuildingType.NUMBER_OF_BUILDINGS];
		for (int i = 0; i < buildingCounts.length; i++) {
			buildingCounts[i] = 0;
		}
		buildingCounts[EBuildingType.COALMINE.ordinal] = numberOfWeaponSmiths;

		buildingCounts[EBuildingType.IRONMINE.ordinal] = Math.round(numberOfWeaponSmiths / COAL_MINE_TO_IRON_MINE_RATIO + 1);
		buildingCounts[EBuildingType.IRONMELT.ordinal] = numberOfWeaponSmiths;
		buildingCounts[EBuildingType.WEAPONSMITH.ordinal] = numberOfWeaponSmiths;
		buildingCounts[EBuildingType.BARRACK.ordinal] = (int) Math.ceil((double) numberOfWeaponSmiths / WEAPON_SMITH_TO_BARRACKS_RATIO);
		buildingCounts[EBuildingType.TOOLSMITH.ordinal] = 1;

		int numberOfFisher = Math.min((int) (numberOfWeaponSmiths / WEAPON_SMITH_TO_FISHER_HUT_RATIO), maxFishermen);
		buildingCounts[EBuildingType.FISHER.ordinal] = numberOfFisher;
		int numberOfRemainingWeaponSmiths = Math.max(0, numberOfWeaponSmiths - (int) (numberOfFisher * WEAPON_SMITH_TO_FISHER_HUT_RATIO));

		int numberOfFarms = (int) Math.ceil(numberOfRemainingWeaponSmiths / WEAPON_SMITH_TO_FARM_RATIO);
		if(civilisation == ECivilisation.EGYPTIAN) {
			numberOfFarms += maxMannaProducers;
		}

		int minFarmsForMap = Math.round(grassTiles / GRASS_TO_FARM_RATIO);

		numberOfFarms = Math.max(minFarmsForMap, numberOfFarms);

		buildingCounts[EBuildingType.FARM.ordinal] = numberOfFarms;
		buildingCounts[EBuildingType.BAKER.ordinal] = (int) Math.ceil(numberOfFarms / FARM_TO_BAKER_RATIO);
		buildingCounts[EBuildingType.MILL.ordinal] = (int) Math.ceil(numberOfFarms / FARM_TO_MILL_RATIO);
		int numberOfWaterworks = (int) Math.ceil(numberOfFarms / FARM_TO_WATERWORKS_RATIO);
		if(civilisation == ECivilisation.AMAZON) numberOfWaterworks += maxMannaProducers;
		buildingCounts[EBuildingType.WATERWORKS.ordinal] = numberOfWaterworks;
		buildingCounts[EBuildingType.SLAUGHTERHOUSE.ordinal] = (int) Math.ceil(numberOfFarms / FARM_TO_SLAUGHTER_RATIO);
		buildingCounts[EBuildingType.PIG_FARM.ordinal] = (int) Math.ceil(numberOfFarms / FARM_TO_PIG_FARM_RATIO);

		int lumberJacksForWeaponSmiths = Math.max(8, (int) (numberOfWeaponSmiths / WEAPON_SMITH_TO_LUMBERJACK_RATIO));
		int maxLumberJacksForMap = Math.round((float) grassTiles / GRASS_TO_LUMBERJACK_RATIO);
		int numberOfLumberJacks = Math.max(MIN_LUMBERJACK_COUNT, Math.min(maxLumberJacksForMap, lumberJacksForWeaponSmiths));
		if(civilisation == ECivilisation.ASIAN) numberOfLumberJacks *= 2;
		buildingCounts[EBuildingType.LUMBERJACK.ordinal] = numberOfLumberJacks;
		buildingCounts[EBuildingType.FORESTER.ordinal] = Math.max((int) (numberOfLumberJacks / LUMBERJACK_TO_FORESTER_RATIO), 1);
		buildingCounts[EBuildingType.SAWMILL.ordinal] = Math.max((int) (numberOfLumberJacks / LUMBERJACK_TO_SAWMILL_RATIO), 1);
		buildingCounts[EBuildingType.STONECUTTER.ordinal] = Math.max((int) (stoneCount / STONE_TO_STONECUTTER_RATIO), 1);

		buildingCounts[EBuildingType.GEMSMINE.ordinal] = maxGemsMines;

		if (maxGoldMelts > 0) {
			buildingCounts[EBuildingType.GOLDMELT.ordinal] = maxGoldMelts;
			buildingCounts[EBuildingType.GOLDMINE.ordinal] = 1;
		}

		if (maxBigTemples > 0) {
			buildingCounts[EBuildingType.BIG_TEMPLE.ordinal] = maxBigTemples;
		}

		buildingCounts[EBuildingType.RICE_FARM.ordinal] = maxRiceFarms;
		buildingCounts[EBuildingType.BEEKEEPING.ordinal] = (int) (maxMannaProducers * BEEKEEPING_TO_MEAD_BREWERY_RATIO);

		if (maxMannaProducers > 0) {
			buildingCounts[civilisation.getMannaBuilding().ordinal] = maxMannaProducers;
			buildingCounts[EBuildingType.TEMPLE.ordinal] = maxMannaProducers;
		}

		if (isEnoughSpace(buildingCounts, grassTiles, civilisation)) {
			return buildingCounts;
		} else if (numberOfWeaponSmiths > MIN_SMITHS_BEFORE_MANNA_AND_GOLD_REDUCTION) {
			return calculateBuildingCounts(numberOfWeaponSmiths - 1, maxFishermen, maxGoldMelts, maxGemsMines, maxMannaProducers, maxBigTemples, maxRiceFarms, grassTiles, stoneCount, civilisation);
		} else if (maxMannaProducers > MIN_MANNA_PRODUCERS_BEFORE_GOLD_REDUCTION) {
			return calculateBuildingCounts(numberOfWeaponSmiths, maxFishermen, maxGoldMelts, maxMannaProducers - 1, maxGemsMines, maxBigTemples, maxRiceFarms, grassTiles, stoneCount, civilisation);
		} else if (maxGoldMelts > 1) {
			return calculateBuildingCounts(numberOfWeaponSmiths, maxFishermen, maxGoldMelts - 1, maxGemsMines, maxMannaProducers, maxBigTemples, maxRiceFarms, grassTiles, stoneCount, civilisation);
		} else if (maxMannaProducers > 1) {
			return calculateBuildingCounts(numberOfWeaponSmiths, maxFishermen, maxGoldMelts, maxGemsMines, maxMannaProducers - 1, maxBigTemples, maxRiceFarms, grassTiles, stoneCount, civilisation);
		} else if (maxBigTemples > 1) {
			return calculateBuildingCounts(numberOfWeaponSmiths, maxFishermen, maxGoldMelts, maxGemsMines, maxMannaProducers, 0, maxRiceFarms, grassTiles, stoneCount, civilisation);
		} else if(maxRiceFarms > 0) {
			return calculateBuildingCounts(numberOfWeaponSmiths, maxFishermen, maxGoldMelts, maxGemsMines, maxMannaProducers, 0, maxRiceFarms - 1, grassTiles, stoneCount, civilisation);
		} else if (maxMannaProducers > 0) {
			return calculateBuildingCounts(numberOfWeaponSmiths, maxFishermen, maxGoldMelts, maxGemsMines, maxMannaProducers - 1, 0, maxRiceFarms, grassTiles, stoneCount, civilisation);
		} else if (maxFishermen > 0) {
			return calculateBuildingCounts(numberOfWeaponSmiths, maxFishermen - 1, maxGoldMelts, maxGemsMines, maxMannaProducers, 0, maxRiceFarms, grassTiles, stoneCount, civilisation);
		} else if (numberOfWeaponSmiths > 0) {
			return calculateBuildingCounts(numberOfWeaponSmiths - 1, maxFishermen, maxGoldMelts, maxGemsMines, maxMannaProducers, 0, maxRiceFarms, grassTiles, stoneCount, civilisation);
		} else {
			return new int[EBuildingType.NUMBER_OF_BUILDINGS];
		}
	}

	private boolean isEnoughSpace(int[] buildingCounts, long grassTiles, ECivilisation playerCivilisation) {
		long grassTilesWithoutBuffer = Math.round(grassTiles / 3F);
		for (int i = 0; i < buildingCounts.length; i++) {
			BuildingVariant building = EBuildingType.VALUES[i].getVariant(playerCivilisation);

			if(building != null && !building.isMine()) {
				grassTilesWithoutBuffer -= building.getProtectedTiles().length * buildingCounts[i];
				if (grassTilesWithoutBuffer < 0) {
					return false;
				}
			}
		}

		return true;
	}
}
