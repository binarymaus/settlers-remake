/*******************************************************************************
 * Copyright (c) 2015 - 2017
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
package jsettlers.logic.map.loading.original.data;

import jsettlers.common.buildings.EBuildingType;

/**
 * @author Thomas Zeugner
 * @author codingberlin
 */
public enum EOriginalMapBuildingType {

	NOT_A_BUILDING(null), // - 0 is not defined

	STOCK(EBuildingType.STOCK),
	LUMBERJACK(EBuildingType.LUMBERJACK),
	STONECUTTER(EBuildingType.STONECUTTER),
	SAWMILL(EBuildingType.SAWMILL),
	FORESTER(EBuildingType.FORESTER),
	LOOKOUT_TOWER(EBuildingType.LOOKOUT_TOWER),
	COALMINE(EBuildingType.COALMINE),
	GOLDMINE(EBuildingType.GOLDMINE),
	IRONMINE(EBuildingType.IRONMINE),
	GOLDMELT(EBuildingType.GOLDMELT),
	Eisenschmelze(EBuildingType.IRONMELT),
	TOOLSMITH(EBuildingType.TOOLSMITH),
	WEAPONSMITH(EBuildingType.WEAPONSMITH),
	WINEGROWER(EBuildingType.WINEGROWER),
	TOWER(EBuildingType.TOWER),
	TOWER_BIG(EBuildingType.BIG_TOWER),
	Muehle(EBuildingType.MILL),
	CASTLE(EBuildingType.CASTLE),
	BARRACK(EBuildingType.BARRACK),
	BAKER(EBuildingType.BAKER),
	Metzger(EBuildingType.SLAUGHTERHOUSE),
	Destille(EBuildingType.DISTILLERY),
	PIG_FARM(EBuildingType.PIG_FARM),
	FARM(EBuildingType.FARM),
	FISHER(EBuildingType.FISHER),
	LIVINGHOUSE_SMALL(EBuildingType.SMALL_LIVINGHOUSE),
	LIVINGHOUSE_MEDIUM(EBuildingType.MEDIUM_LIVINGHOUSE),
	LIVINGHOUSE_BIG(EBuildingType.BIG_LIVINGHOUSE),
	SULFURMINE(EBuildingType.SULFURMINE),
	WATERWORKS(EBuildingType.WATERWORKS),
	Katapultwerk(null),
	DOCKYARD(EBuildingType.DOCKYARD),
	HARBOR(EBuildingType.HARBOR),
	Marktplatz(EBuildingType.MARKET_PLACE),
	HOSPITAL(EBuildingType.HOSPITAL),
	Reisfarm(EBuildingType.RICE_FARM),
	GEMSMINE(EBuildingType.GEMSMINE),
	Brauerei(EBuildingType.BREWERY),
	CHARCOAL_BURNER(EBuildingType.CHARCOAL_BURNER),
	Pulvermacherei(null),
	Pyramide(EBuildingType.BIG_TEMPLE),
	Sphinx(EBuildingType.TEMPLE),
	BIG_TEMPLE(EBuildingType.BIG_TEMPLE), // TODO : does not work?!
	TEMPLE(EBuildingType.TEMPLE),
	grosse_Pagode(EBuildingType.BIG_TEMPLE),
	kleine_Pagode(EBuildingType.TEMPLE),
	Ballistenwerkstatt(null),
	Kanonenwerkstatt(null),
	DONKEY_FARM(EBuildingType.DONKEY_FARM),
	grosser_Gong(null),
	BEEKEEPING(EBuildingType.BEEKEEPING),
	Metwinzerei(EBuildingType.MEAD_BREWERY),
	Labortorium(EBuildingType.LABORATORY),
	kleiner_Tempel(EBuildingType.TEMPLE),
	grosser_Tempel(EBuildingType.BIG_TEMPLE),
	SULFURMINE_AMAZON(EBuildingType.SULFURMINE);

	private static final EOriginalMapBuildingType[] VALUES = EOriginalMapBuildingType.values();
	private final EBuildingType value;

	EOriginalMapBuildingType(EBuildingType value) {
		this.value = value;
	}

	public EBuildingType getValue() {
		return value;
	}

	public static EOriginalMapBuildingType getTypeByInt(int type) {
		if (type < 0 || type >= VALUES.length) {
			return NOT_A_BUILDING;
		} else {
			return VALUES[type];
		}
	}

}