package jsettlers.logic.player;

import jsettlers.common.player.IBedInformation;

public class BedInformation implements IBedInformation {

	private static final long serialVersionUID = 7709480763413851338L;
	private int totalBedAmount = 0;
	private int usedBedAmount = 0;

	@Override
	public int getTotalBedAmount() {
		return totalBedAmount;
	}

	public void removeBearer() {
		usedBedAmount--;
	}

	public void removeBeds(int removedBeds) {
		totalBedAmount -= removedBeds;
	}

	public boolean tryReservingBed() {
		if(totalBedAmount > usedBedAmount) {
			usedBedAmount++;
			return true;
		}

		return false;
	}

	public boolean testIfBedStillExists() {
		if(totalBedAmount < usedBedAmount) {
			usedBedAmount--;
			return false;
		}

		return true;
	}

	public void addNewBearer() {
		totalBedAmount++;
		usedBedAmount++;
	}
}
