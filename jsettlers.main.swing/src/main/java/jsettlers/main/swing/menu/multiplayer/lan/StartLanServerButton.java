package jsettlers.main.swing.menu.multiplayer.lan;

import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.network.infrastructure.log.Logger;
import jsettlers.network.server.GameServerThread;
import jsettlers.network.server.match.EPlayerState;

import javax.swing.JToggleButton;
import java.io.IOException;
import java.util.function.Consumer;

public class StartLanServerButton extends JToggleButton {

	private GameServerThread serverHandle;
	private final Logger logger;
	private final Consumer<Boolean> serverActive;

	public StartLanServerButton(Logger logger, Consumer<Boolean> serverActive) {
		this.serverActive = serverActive;
		this.logger = logger;

		putClientProperty(ELFStyle.KEY, ELFStyle.TOGGLE_BUTTON_STONE);
		addActionListener(e -> setSelected(setServerState(isSelected())));
	}

	private boolean setServerState(boolean active) {
		boolean serverState = serverHandle != null;
		if(active == serverState) return serverState;

		if(active) {
			try {
				serverHandle = new GameServerThread(true, logger);
				serverHandle.start();
			} catch (IOException e) {
				logger.error(e);
			}
		} else {
			serverHandle.shutdown();
			serverHandle = null;
		}

		boolean activeAfter = serverHandle != null;
		serverActive.accept(activeAfter);
		return activeAfter;
	}

	public int getPlayerCount() {
		if(serverHandle == null) {
			return -1;
		}

		return serverHandle.getDatabase().getPlayers(EPlayerState.values()).size();
	}
}
