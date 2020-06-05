/*******************************************************************************
 * Copyright (c) 2020
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
package jsettlers.logic.map.loading;

import java.io.Serializable;

import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.player.EWinState;
import jsettlers.logic.map.grid.MainGrid;
import jsettlers.logic.map.grid.partition.data.BuildingCounts;
import jsettlers.logic.player.Player;
import jsettlers.logic.timer.IScheduledTimerable;
import jsettlers.logic.timer.RescheduleTimer;

public abstract class WinLoseHandler implements IScheduledTimerable, Serializable {
	private static final long serialVersionUID = 1;

	protected final MainGrid mainGrid;
	protected transient Player[] players;

	public WinLoseHandler(MainGrid mainGrid) {
		this.mainGrid = mainGrid;
	}

	public abstract void updateWinLose();

	private static final int UPDATE_DELAY = 5000;

	@Override
	public final int timerEvent() {
		players = mainGrid.getPartitionsGrid().getPlayers();
		updateWinLose();
		return UPDATE_DELAY;
	}

	@Override
	public void kill() {
	}

	public void schedule() {
		RescheduleTimer.add(this, UPDATE_DELAY);
	}

	protected boolean isPlayerDead(Player player) {
		if(player.getWinState() != EWinState.UNDECIDED) return false;

		// Get all buildings in any partition and count military ones
		BuildingCounts buildingCounts = new BuildingCounts(player.playerId, (short) 0);
		int militaryBuildingsCount = buildingCounts.buildings(EBuildingType.TOWER) + buildingCounts.buildings(EBuildingType.BIG_TOWER) + buildingCounts.buildings(EBuildingType.CASTLE);

		return militaryBuildingsCount == 0;
	}

	protected void defeatDeadPlayers() {
		for (Player player : players) {
			if(isPlayerDead(player)) {
				player.setWinState(EWinState.LOST);
				System.out.println(player + " was defeated");
			}
		}
	}
}
