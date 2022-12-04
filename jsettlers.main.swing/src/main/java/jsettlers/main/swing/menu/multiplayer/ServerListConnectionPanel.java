package jsettlers.main.swing.menu.multiplayer;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jsettlers.graphics.localization.Labels;
import jsettlers.main.swing.JSettlersFrame;
import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.main.swing.menu.openpanel.OpenPanel;
import jsettlers.main.swing.settings.ServerEntry;
import jsettlers.network.client.IClientConnection;

public class ServerListConnectionPanel extends ServerConnectionPanel {

	private final ServerEntry entry;

	public ServerListConnectionPanel(ServerEntry entry, Runnable leave, JSettlersFrame settlersFrame, OpenPanel openSinglePlayerPanel) {
		super(settlersFrame, openSinglePlayerPanel);

		this.entry = entry;

		JTextArea logText = new JTextArea();
		logText.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		logText.setEditable(false);
		root.addTab(Labels.getString("multiplayer-log-title"), new JScrollPane(logText));
		entry.setConnectionLogListener(logText::setText);

		root.addTab(Labels.getString("multiplayer-log-settings"), new EditServerEntryPanel(leave, () -> root.setSelectedIndex(0), entry));
	}

	@Override
	protected IClientConnection getConnection() {
		return entry.getConnection();
	}

	@Override
	protected int updateBefore(IClientConnection connection, int i) {
		return 1;
	}

	@Override
	protected int updateAfter(IClientConnection connection, int i) {
		return 1;
	}
}
