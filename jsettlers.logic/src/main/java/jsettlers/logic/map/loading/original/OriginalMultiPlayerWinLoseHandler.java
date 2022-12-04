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
package jsettlers.logic.map.loading.original;

import jsettlers.common.player.EWinState;
import jsettlers.logic.map.grid.MainGrid;
import jsettlers.logic.map.loading.WinLoseHandler;
import jsettlers.logic.player.Player;

import static java.util.Arrays.stream;

public class OriginalMultiPlayerWinLoseHandler extends WinLoseHandler {
	private static final long serialVersionUID = 1;

	public OriginalMultiPlayerWinLoseHandler(MainGrid mainGrid) {
		super(mainGrid);
	}

	@Override
	public void updateWinLose() {
		// Update defeated status
		defeatDeadPlayers();

		// end game if only one team/nobody is left
		Byte[] teamsWithAlivePlayer = playerStream()
				.filter(player -> player.getWinState() != EWinState.LOST)
				.map(Player::getTeamId)
				.distinct()
				.toArray(Byte[]::new);

		if(teamsWithAlivePlayer.length > 1) return;

		// set the winning team (if present) as winner
		if(teamsWithAlivePlayer.length == 1) {
			byte winnerTeam = teamsWithAlivePlayer[0];

			playerStream().filter(player -> player.getTeamId() == winnerTeam)
					.forEach(player -> player.setWinState(EWinState.WON));
		}

		// game has ended. everybody can know see the whole map
		mainGrid.disableFogOfWar();
	}
}
