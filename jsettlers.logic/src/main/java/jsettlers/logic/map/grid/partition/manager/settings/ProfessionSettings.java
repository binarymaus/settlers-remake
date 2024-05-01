package jsettlers.logic.map.grid.partition.manager.settings;

import java.io.Serializable;

import jsettlers.common.map.partition.IProfessionSettings;
import jsettlers.common.movable.EMovableType;

public class ProfessionSettings implements Serializable, IProfessionSettings {

	private static final long serialVersionUID = -3565901631440791613L;

	private SingleProfessionLimit bearerSettings;
	private SingleProfessionLimit diggerSettings;
	private SingleProfessionLimit bricklayerSettings;

	private int workerCount;

	public ProfessionSettings() {
		this(0.25f, 0.25f, 0.25f);
	}

	public ProfessionSettings(float minBearerRatio, float maxDiggerRatio, float maxBricklayerRatio) {

		bearerSettings = new RelativeProfessionLimit(this, minBearerRatio);
		diggerSettings = new RelativeProfessionLimit(this, maxDiggerRatio);
		bricklayerSettings = new RelativeProfessionLimit(this, maxBricklayerRatio);

		resetCount();
	}

	public void markTempBearerConversion(EMovableType newType) {
		bearerSettings.decrementTempAmount();

		SingleProfessionLimit settings = getSettings(newType);
		if(settings != null) {
			settings.incrementTempAmount();
		}
	}


	public void abortBearerConversion(EMovableType newType) {
		SingleProfessionLimit settings = getSettings(newType);
		if(settings != null) {
			settings.decrementTempAmount();
		}

		bearerSettings.incrementTempAmount();
	}



	public void applyBearerConversion(EMovableType newType) {
		bearerSettings.decrementRealAmount();
		bearerSettings.incrementTempAmount();

		SingleProfessionLimit settings = getSettings(newType);
		if(settings != null) {
			settings.decrementTempAmount();
			settings.incrementRealAmount();
		}
	}

	public boolean isBearerConversionAllowed(EMovableType newType) {
		if(getSettings(EMovableType.BEARER).getRemainingAmount() > -1) {
			return false;
		}

		if(newType == EMovableType.DIGGER || newType == EMovableType.BRICKLAYER) {
			return getSettings(newType).getRemainingAmount() >= 1;
		} else {
			return true;
		}
	}

	@Override
	public SingleProfessionLimit getSettings(EMovableType movableType) {
		switch (movableType) {
			case BEARER:
				return bearerSettings;
			case DIGGER:
				return diggerSettings;
			case BRICKLAYER:
				return bricklayerSettings;
			default:
				return null;
		}
	}

	public void changeRatio(EMovableType movableType, int value, boolean relative) {
		getSettings(movableType).setLimit(value, relative);
	}

	public void resetCount() {
		this.workerCount = 0;
		bearerSettings.resetCount();
		diggerSettings.resetCount();
		bricklayerSettings.resetCount();
	}
	
	public void increment(EMovableType movableType) {
		if(!movableType.isPlayerControllable()) workerCount++;

		SingleProfessionLimit settings = getSettings(movableType);
		if(settings != null) {
			settings.incrementRealAmount();
		}
	}

	public int getWorkerCount() {
		return workerCount;
	}

	public void changeLimitType(EMovableType movableType, boolean relative) {
		SingleProfessionLimit newSettings;
		SingleProfessionLimit currentSettings = getSettings(movableType);

		if(relative) {
			newSettings = new RelativeProfessionLimit(this, currentSettings);
		} else {
			newSettings = new AbsoluteProfessionLimit(this, currentSettings);
		}

		switch (movableType) {
			case BEARER:
				bearerSettings = newSettings;
				break;
			case DIGGER:
				diggerSettings = newSettings;
				break;
			case BRICKLAYER:
				bricklayerSettings = newSettings;
				break;
			default:
				throw new Error("Unknown movable limit type!");
		}
	}
}
