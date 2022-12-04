package jsettlers.main.swing.menu.joinpanel;

import jsettlers.common.menu.IJoinPhaseMultiplayerGameConnector;
import jsettlers.common.menu.IJoiningGame;
import jsettlers.main.swing.lookandfeel.ELFStyle;

import javax.swing.JComboBox;

public class NumberOfPlayersComboBox extends JComboBox<Integer> {

	private final JoinGamePanel parent;
	private IJoinPhaseMultiplayerGameConnector joiningGame;
	private int selectedPlayerCount = -1;
	private boolean update = true;

	public NumberOfPlayersComboBox(JoinGamePanel parent) {
		this.parent = parent;
		putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);

		addActionListener(e -> {
			if(!update) return;

			int newPlayerCount = getSelectedIndex()+1;
			if(newPlayerCount != selectedPlayerCount) {
				if(joiningGame != null) {
					joiningGame.setPlayerCount(newPlayerCount);
				}
			}

			if(joiningGame == null) {
				parent.updateNumberOfPlayerSlots(newPlayerCount);
			}
		});
	}

	public void setPlayerCount(int playerCount) {
		if(selectedPlayerCount != playerCount) {
			parent.updateNumberOfPlayerSlots(playerCount);
		}

		selectedPlayerCount = playerCount;
		setSelectedIndex(playerCount - 1);
	}

	public void reset(int maxPlayers) {
		update = false;
		removeAllItems();
		for (int i = 1; i < maxPlayers + 1; i++) {
			addItem(i);
		}
		update = true;
		setPlayerCount(maxPlayers);
	}

	public void informGame(IJoinPhaseMultiplayerGameConnector joiningGame) {
		this.joiningGame = joiningGame;
	}
}
