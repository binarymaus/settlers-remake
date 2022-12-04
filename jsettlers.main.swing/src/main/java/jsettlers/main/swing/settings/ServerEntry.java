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
package jsettlers.main.swing.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.UUID;

import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import jsettlers.common.menu.IMultiplayerConnector;
import jsettlers.graphics.localization.Labels;
import jsettlers.main.MultiplayerConnector;
import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.network.client.EServerType;
import jsettlers.network.client.HTTPConnection;
import jsettlers.network.client.IClientConnection;
import jsettlers.network.infrastructure.log.ConsoleConsumerLogger;
import jsettlers.network.infrastructure.log.Logger;

public class ServerEntry implements Cloneable {

	private String alias;
	private EServerType type;
	private String address;
	private String username;
	private String uuid;
	private String url;

	public static final String URL_EMPTY = "https://";

	private transient IClientConnection connection = null;
	private transient StringBuffer connectionLog;
	private transient Consumer<String> listener = null;

	public ServerEntry() {
		setType(EServerType.JSETTLERS);
		connectionLog = new StringBuffer();
		username = SettingsManager.getInstance().getUserName();
	}

	public String getAlias() {
		return alias;
	}

	public EServerType getType() {
		return type;
	}

	public String getAddress() {
		return address;
	}

	public String getUsername() {
		return username;
	}

	public UUID getUUID() {
		return UUID.fromString(uuid);
	}

	public String getURL() {
		return url;
	}

	public void setAlias(String alias) {
		if(connection != null) throw new AssertionError();
		this.alias = alias;
	}

	public void setType(EServerType type) {
		if(connection != null) throw new AssertionError();
		this.type = type;

		if(type == EServerType.JSETTLERS) {
			setUUID(UUID.randomUUID());
		}
	}

	public void setAddress(String address) {
		if(connection != null) throw new AssertionError();
		this.address = address;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid.toString();
	}

	public void setURL(String url) {
		if(connection != null) throw new AssertionError();
		if(URL_EMPTY.equals(url)) return;
		this.url = url;
	}

	public void set(ServerEntry replace) {
		alias = replace.alias;
		address = replace.address;
		url = replace.url;

		disconnect();
		connect();
	}

	void connect() {
		Logger log = new ConsoleConsumerLogger(alias, line -> {
			connectionLog.append(line);
			synchronized (ServerEntry.this) {
				if (listener != null) {
					listener.accept(connectionLog.toString());
				}
			}
		});

		switch (type) {
			case HTTP:
				connection = new HTTPConnection(url, log);
				break;
			case JSETTLERS:
				connection = new MultiplayerConnector(address, uuid, username, log);
				break;
		}
	}

	public void setConnectionLogListener(Consumer<String> listener) {
		synchronized (this) {
			this.listener = listener;
			listener.accept(connectionLog.toString());
		}
	}

	public IClientConnection getConnection() {
		return connection;
	}

	void disconnect() {
		connection.action(IClientConnection.EClientAction.CLOSE, null);
	}

	@Override
	public ServerEntry clone() {
		ServerEntry clone = new ServerEntry();
		clone.address = address;
		clone.alias = alias;
		clone.type = type;
		clone.url = url;
		return clone;
	}

	public static class ServerEntryCellRenderer implements ListCellRenderer<ServerEntry> {

		private final Color SELECTION_BACKGROUND = UIManager.getColor("ServerEntryCellRenderer.backgroundSelected");
		private final Color BACKGROUND_EVEN = UIManager.getColor("ServerEntryCellRenderer.backgroundColorEven");
		private final Color BACKGROUND_ODD = UIManager.getColor("ServerEntryCellRenderer.backgroundColorOdd");

		private final Color FEATURE_MISSING = UIManager.getColor("ServerEntryCellRenderer.featureMissing");
		private final Color FEATURE_PRESENT = UIManager.getColor("ServerEntryCellRenderer.featurePresent");

		private final Color FONT_COLOR = UIManager.getColor("ServerEntryCellRenderer.foregroundColor");

		private JPanel renderPanel = new JPanel();

		private JLabel serverName = new JLabel();
		private JLabel status = new JLabel();

		private JPanel southPanel = new JPanel();
		private JLabel matchIcon = new JLabel(Labels.getString("multiplayer-indicator-match"));
		private JLabel mapsIcon = new JLabel(Labels.getString("multiplayer-indicator-maps"));
		private JLabel resIcon = new JLabel(Labels.getString("multiplayer-indicator-res"));
		private JLabel authIcon = new JLabel(Labels.getString("multiplayer-indicator-auth"));
		private JLabel online = new JLabel();

		public ServerEntryCellRenderer() {;

			southPanel.setLayout(new FlowLayout());
			southPanel.add(matchIcon);
			southPanel.add(mapsIcon);
			southPanel.add(resIcon);
			southPanel.add(authIcon);
			southPanel.add(online);

			renderPanel.setLayout(new BorderLayout());
			renderPanel.putClientProperty(ELFStyle.KEY, ELFStyle.PANEL_DRAW_BG_CUSTOM);

			status.setForeground(FONT_COLOR);
			serverName.setForeground(FONT_COLOR);

			renderPanel.add(status, BorderLayout.EAST);
			renderPanel.add(serverName, BorderLayout.WEST);
			renderPanel.add(southPanel, BorderLayout.PAGE_END);
			SwingUtilities.updateComponentTreeUI(renderPanel);
		}

		private void setColor(boolean present, JLabel label) {
			label.setForeground(present ? FEATURE_PRESENT : FEATURE_MISSING);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends ServerEntry> jList, ServerEntry serverEntry, int i, boolean b, boolean b1) {
			serverName.setText(serverEntry.alias);

			IClientConnection connection = serverEntry.getConnection();
			if(connection.isConnected()) {
				status.setText(Labels.getString("multiplayer-online"));
			} else if(connection.hasConnectionFailed()){
				status.setText(Labels.getString("multiplayer-offline"));
			} else {
				status.setText(Labels.getString("multiplayer-connecting"));
			}

			if (b1) {
				renderPanel.setBackground(SELECTION_BACKGROUND);
			} else {
				if (i % 2 == 0) {
					renderPanel.setBackground(BACKGROUND_EVEN);
				} else {
					renderPanel.setBackground(BACKGROUND_ODD);
				}
			}

			setColor(connection.isConnected() && connection instanceof IMultiplayerConnector, matchIcon);
			setColor(connection.getMaps("/")!=null, mapsIcon);
			setColor(false, resIcon);
			setColor(false, authIcon);

			online.setText("0");

			return renderPanel;
		}
	}
}
