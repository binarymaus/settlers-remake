/*******************************************************************************
 * Copyright (c) 2015 - 2020
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.main.swing.menu.mainmenu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import jsettlers.common.menu.IStartingGame;
import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.logic.map.loading.newmap.MapFileHeader;
import jsettlers.logic.map.loading.savegame.SavegameLoader;
import jsettlers.logic.player.InitialGameState;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.swing.JSettlersFrame;
import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.main.swing.lookandfeel.components.SplitedBackgroundPanel;
import jsettlers.main.swing.menu.multiplayer.lan.LANConnectionPanel;
import jsettlers.main.swing.menu.multiplayer.EditServerEntryPanel;
import jsettlers.main.swing.menu.multiplayer.ServerConnectionPanel;
import jsettlers.main.swing.menu.multiplayer.ServerListConnectionPanel;
import jsettlers.main.swing.menu.openpanel.OpenPanel;
import jsettlers.main.swing.menu.settingsmenu.SettingsMenuPanel;
import jsettlers.main.swing.settings.ServerEntry;
import jsettlers.main.swing.settings.ServerManager;

/**
 * @author codingberlin
 */
public class MainMenuPanel extends SplitedBackgroundPanel {
	private static final long serialVersionUID = -6745474019479693347L;

	private final JSettlersFrame settlersFrame;
	private final JPanel         emptyPanel  = new JPanel();
	private final ButtonGroup    buttonGroup = new ButtonGroup();
	private JList<ServerEntry> serverOverview;
	private Map<ServerEntry, ServerConnectionPanel> serverConnectionPanels = new WeakHashMap<>();

	/**
	 * Panel with the selection Buttons
	 */
	private final JPanel buttonPanel = new JPanel();

	/**
	 * Panel with the main buttons at top, and the exit button at bottom
	 */
	private final JPanel mainButtonPanel = new JPanel();

	private final OpenPanel openSinglePlayerPanel;

	public MainMenuPanel(JSettlersFrame settlersFrame) {
		this.settlersFrame = settlersFrame;

		openSinglePlayerPanel = new OpenPanel(MapList.getDefaultList().getFreshMaps().getItems(), settlersFrame::showNewSinglePlayerGameMenu);
		OpenPanel openSaveGamePanel = new OpenPanel(MapList.getDefaultList().getSavedMaps(), this::loadSavegame);
		SettingsMenuPanel settingsPanel = new SettingsMenuPanel(this);

		registerMenu("main-panel-new-single-player-game-button", e -> setCenter("main-panel-new-single-player-game-button", openSinglePlayerPanel));
		registerMenu("start-loadgame", e -> setCenter("start-loadgame", openSaveGamePanel));
		registerMenu("settings-title", e -> {
			setCenter("settings-title", settingsPanel);
			settingsPanel.initializeValues();
		});

		initButtonPanel();
		SwingUtilities.updateComponentTreeUI(this);


		Timer updateServerTimer = new Timer(0, action -> {
				Component mainPanel = getComponent(2);
				if(mainPanel instanceof ServerConnectionPanel) {
					((ServerConnectionPanel)mainPanel).update();
				}
				repaint();
		});
		updateServerTimer.setRepeats(true);
		updateServerTimer.setDelay(250);
		updateServerTimer.start();
	}

	private void initButtonPanel() {
		buttonPanel.setLayout(new GridLayout(0, 1, 20, 20));

		mainButtonPanel.setLayout(new BorderLayout());
		mainButtonPanel.add(buttonPanel, BorderLayout.NORTH);

		initServerList();

		JButton btExit = new JButton(Labels.getString("main-panel-exit-button"));
		btExit.addActionListener(e -> settlersFrame.exit());
		btExit.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_MENU);

		mainButtonPanel.add(btExit, BorderLayout.SOUTH);

		add(mainButtonPanel);
		add(emptyPanel);
		getTitleLabel().setVisible(false);
	}

	private void initServerList() {
		JPanel serverPanel = new JPanel();
		serverOverview = new JList<>(ServerManager.getInstance().createListModel());
		serverOverview.addListSelectionListener(e -> {
			ServerEntry selected = serverOverview.getSelectedValue();
			if(selected == null) return;
			buttonGroup.clearSelection();
			ServerConnectionPanel connPanel = serverConnectionPanels.get(selected);
			if(connPanel == null) {
				connPanel = new ServerListConnectionPanel(selected, this::reset, settlersFrame, openSinglePlayerPanel);
				serverConnectionPanels.put(selected, connPanel);
			}
			setCenter(selected.getAlias(), connPanel);
		});
		serverOverview.setCellRenderer(new ServerEntry.ServerEntryCellRenderer());
		serverOverview.setOpaque(false);
		JScrollPane serverOverviewScroll = new JScrollPane(serverOverview);
		serverOverviewScroll.setPreferredSize(new Dimension(230, 300));

		EditServerEntryPanel addServerPanel = new EditServerEntryPanel(this::reset);
		LANConnectionPanel lanConnectionPanel = new LANConnectionPanel(settlersFrame);

		JPanel serverTopPanel = new JPanel();
		serverTopPanel.setLayout(new BorderLayout());

		JToggleButton addServer = new JToggleButton(Labels.getString("multiplayer-addserver"));
		addServer.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_MENU);
		buttonGroup.add(addServer);
		addServer.addActionListener(e -> setCenter("multiplayer-addserver", addServerPanel));

		JToggleButton connectLan = new JToggleButton(Labels.getString("multiplayer-lanpanel"));
		connectLan.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_MENU);
		buttonGroup.add(connectLan);
		connectLan.addActionListener(e -> setCenter("multiplayer-lanpanel", lanConnectionPanel));

		serverTopPanel.add(addServer, BorderLayout.LINE_START);
		serverTopPanel.add(connectLan, BorderLayout.LINE_END);

		serverPanel.add(serverTopPanel);
		serverPanel.add(serverOverviewScroll);

		mainButtonPanel.add(serverPanel);
	}

	private void registerMenu(String translationKey, ActionListener listener) {
		JToggleButton bt = new JToggleButton(Labels.getString(translationKey));
		bt.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_MENU);
		buttonGroup.add(bt);
		bt.addActionListener(listener);
		buttonPanel.add(bt);
		bt.setPreferredSize(new Dimension(230, 60));
	}

	private void loadSavegame(MapLoader map) {
		SavegameLoader savegameLoader = (SavegameLoader) map;

		if (savegameLoader != null) {
			MapFileHeader mapFileHeader = savegameLoader.getFileHeader();
			PlayerSetting[] playerSettings = mapFileHeader.getPlayerSettings();
			byte playerId = mapFileHeader.getPlayerId();
			JSettlersGame game = new JSettlersGame(savegameLoader, new InitialGameState(playerId, playerSettings, -1));
			IStartingGame startingGame = game.start();
			settlersFrame.showStartingGamePanel(startingGame);
		}
	}

	public void reset() {
		setCenter(emptyPanel);
		getTitleLabel().setVisible(false);
		buttonGroup.clearSelection();
		SwingUtilities.updateComponentTreeUI(this);
		serverOverview.clearSelection();
	}

	private void setCenter(final String titleKey, final JPanel panelToBeSet) {
		getTitleLabel().setText(Labels.getString(titleKey));
		getTitleLabel().setVisible(true);
		setCenter(panelToBeSet);
	}

	private void setCenter(final JPanel panelToBeSet) {
		SwingUtilities.updateComponentTreeUI(panelToBeSet);
		remove(2);
		add(panelToBeSet);
		if(!(panelToBeSet instanceof ServerConnectionPanel)) {
			serverOverview.clearSelection();
		}
		settlersFrame.revalidate();
		settlersFrame.repaint();
	}
}
