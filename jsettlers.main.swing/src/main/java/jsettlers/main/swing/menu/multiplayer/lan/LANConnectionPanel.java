package jsettlers.main.swing.menu.multiplayer.lan;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import jsettlers.graphics.localization.Labels;
import jsettlers.main.MultiplayerConnector;
import jsettlers.main.swing.JSettlersFrame;
import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.main.swing.menu.multiplayer.ServerConnectionPanel;
import jsettlers.main.swing.settings.SettingsManager;
import jsettlers.network.client.IClientConnection;
import jsettlers.network.client.NullConnection;
import jsettlers.network.infrastructure.log.ConsoleConsumerLogger;
import jsettlers.network.infrastructure.log.Logger;
import jsettlers.network.server.lan.LanServerAddressBroadcastListener;

import java.awt.GridLayout;
import java.net.InetAddress;

public class LANConnectionPanel extends ServerConnectionPanel {

	private final JButton toggleClientButton;
	private final StartLanServerButton toggleServerButton;
	private final JList<InetAddress> localServerList;
	private IClientConnection connection;

	private final Logger logger;
	private final StringBuffer logText = new StringBuffer();

	private final JTextArea logPane;
	private final AgingServerList serverList;
	private final JLabel statusLabel;
	private InetAddress serverIP = null;

	private ELANState state = ELANState.NONE;

	public LANConnectionPanel(JSettlersFrame settlersFrame) {
		super(settlersFrame, null);

		JPanel overviewPanel = new JPanel();
		overviewPanel.setLayout(new BoxLayout(overviewPanel, BoxLayout.Y_AXIS));
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(2, 1));

		logPane = new JTextArea();
		logPane.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		logger = new ConsoleConsumerLogger("jsettlers-swing-lan", line -> {
			logText.append(line);
			SwingUtilities.invokeLater(() -> logPane.setText(logText.toString()));
		});


		serverList = new AgingServerList();
		LanServerAddressBroadcastListener broadcastListener = new LanServerAddressBroadcastListener(serverList);
		broadcastListener.start();
		localServerList = serverList.createSwingComponent();
		localServerList.setOpaque(false);
		localServerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		localServerList.addListSelectionListener(e -> updateGUIState());


		toggleClientButton = new JButton();
		toggleClientButton.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_STONE);
		toggleClientButton.addActionListener(e -> setServerIP(localServerList.getSelectedValue()));
		topPanel.add(toggleClientButton);

		toggleServerButton = new StartLanServerButton(logger, this::setServerActive);
		topPanel.add(toggleServerButton);

		statusLabel = new JLabel();
		statusLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		topPanel.add(statusLabel);

		overviewPanel.add(topPanel);


		bottomPanel.add(new JScrollPane(localServerList));

		logPane.setEditable(false);
		bottomPanel.add(new JScrollPane(logPane));

		overviewPanel.add(bottomPanel);

		closeConnection();

		root.addTab(Labels.getString("multiplayer-lan-overview"), overviewPanel);

		updateGUIState();
	}

	private void setServerIP(InetAddress serverIP) {
		if(state == ELANState.CLIENT) {
			this.serverIP = null;
			setState(ELANState.CLIENT, ELANState.NONE);
			closeConnection();
		} else {
			assert serverIP != null;
			this.serverIP = serverIP;
			setState(ELANState.NONE, ELANState.CLIENT);

			openConnection(serverIP.getHostAddress());
		}
	}

	private void setServerActive(boolean active) {
		if(active) {
			setState(ELANState.NONE, ELANState.HOST);
			openConnection(null);
		} else {
			setState(ELANState.HOST, ELANState.NONE);
			closeConnection();
		}
	}

	private void setState(ELANState prev, ELANState next) {
		assert state == prev;

		state = next;
		updateGUIState();
	}

	private void updateGUIState() {
		boolean toggleClientEnabled = state == ELANState.NONE && localServerList.getSelectedValue() != null || state == ELANState.CLIENT;

		localServerList.setEnabled(state == ELANState.NONE);

		if(state == ELANState.CLIENT) {
			toggleClientButton.setText(Labels.getString("multiplayer-lan-disconnect"));
		} else {
			toggleClientButton.setText(Labels.getString("multiplayer-lan-connect"));
		}
		toggleClientButton.setEnabled(toggleClientEnabled);

		if(state == ELANState.HOST) {
			toggleServerButton.setText(Labels.getString("multiplayer-lan-stop-server"));
		} else {
			toggleServerButton.setText(Labels.getString("multiplayer-lan-start-server"));
		}
		toggleServerButton.setEnabled(state != ELANState.CLIENT);

		switch (state) {
			case CLIENT:
				statusLabel.setText(Labels.getString("multiplayer-lan-client-label", serverIP.getHostAddress()));
				break;
			case HOST:
				statusLabel.setText(Labels.getString("multiplayer-lan-server-label", toggleServerButton.getPlayerCount()));
				break;
			case NONE:
				statusLabel.setText(Labels.getString("multiplayer-lan-search-label", serverList.getSize()));
				break;
		}
	}

	private void openConnection(String host) {
		closeConnection();

		connection = new MultiplayerConnector(host,
				SettingsManager.getInstance().getUUID(),
				SettingsManager.getInstance().getUserName(),
				logger);
	}

	private void closeConnection() {
		if(connection != null) {
			connection.action(IClientConnection.EClientAction.CLOSE, null);
		}
		connection = new NullConnection();
	}

	@Override
	protected IClientConnection getConnection() {
		return connection;
	}

	@Override
	protected int updateBefore(IClientConnection connection, int i) {
		serverList.update();

		if(state != ELANState.NONE && connection.hasConnectionFailed()) {
			closeConnection();
			setState(state, ELANState.NONE);
		}

		updateGUIState();
		return 1;
	}

	@Override
	protected int updateAfter(IClientConnection connection, int i) {
		return 0;
	}

	private enum ELANState {
		NONE,
		HOST,
		CLIENT
	}
}
