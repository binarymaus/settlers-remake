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
package jsettlers.logic.map.grid.partition.manager.settings;

import java.io.Serializable;

import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.buildings.MaterialsOfBuildings;
import jsettlers.common.map.partition.IMaterialDistributionSettings;
import jsettlers.common.material.EMaterialType;

import java.util.Arrays;
import jsettlers.common.player.ECivilisation;

/**
 * This class holds the distribution settings for a given {@link EMaterialType}.
 *
 * @author Andreas Eberle
 */
public final class MaterialDistributionSettings implements IMaterialDistributionSettings, Serializable {
	private static final long serialVersionUID = -8519244429973606793L;

	private final ECivilisation civilisation;
	private final EMaterialType materialType;
	private final RelativeSettings<EBuildingType> distributionSettings = new RelativeSettings<>(EBuildingType.NUMBER_OF_BUILDINGS, index -> EBuildingType.VALUES[index], false);
	private float requestValueSum = 0f;

	/**
	 * Creates a new object of {@link MaterialDistributionSettings} holding the settings for the given {@link EMaterialType}.
	 *
	 * @param materialType
	 * 		Defines the {@link EMaterialType}, this settings are used for.
	 */
	MaterialDistributionSettings(EMaterialType materialType, ECivilisation civilisation) {
		this.materialType = materialType;
		this.civilisation = civilisation;

		EBuildingType[] requestingBuildings = getBuildingTypes();
		requestValueSum = requestingBuildings.length;	
		Arrays.stream(requestingBuildings).forEach(buildingType -> {
			float initialValue = getInitialValue(materialType, buildingType);
			distributionSettings.setUserValue(buildingType, initialValue);
		});
	}

	private float getInitialValue(EMaterialType materialType, EBuildingType buildingType) {
		switch(materialType) {
			case FISH:
				switch(buildingType) {
					case GOLDMINE: return 1.0f;
					case IRONMINE: return 0.0f;
					case COALMINE: return 0.0f;
					default: return 1.0f;
				}
			case MEAT:
				switch(buildingType) {
					case GOLDMINE: return 0.0f;
					case IRONMINE: return 1.0f;
					case COALMINE: return 0.0f;
					default: return 1.0f;
				}
			case BREAD:
				switch(buildingType) {
					case GOLDMINE: return 0.0f;
					case IRONMINE: return 0.0f;
					case COALMINE: return 1.0f;
					default: return 1.0f;
				}
			default: return 1.0f;
		}
	}

	public final EBuildingType[] getBuildingTypes() {
		return MaterialsOfBuildings.getBuildingTypesRequestingMaterial(materialType, civilisation);
	}

	public void setUserConfiguredDistributionValue(EBuildingType buildingType, float value) {
		float oldValue = distributionSettings.getUserValue(buildingType);
		requestValueSum -= oldValue;
		requestValueSum += value;
		distributionSettings.setUserValue(buildingType, value);
	}

	@Override
	public float getUserConfiguredDistributionValue(EBuildingType buildingType) {
		return distributionSettings.getUserValue(buildingType);
	}

	@Override
	public float getDistributionProbability(EBuildingType buildingType) {
		return distributionSettings.getUserValue(buildingType) / requestValueSum;
	}

	@Override
	public EMaterialType getMaterialType() {
		return materialType;
	}

	public EBuildingType drawRandomBuilding() {
		return distributionSettings.drawRandom();
	}
}