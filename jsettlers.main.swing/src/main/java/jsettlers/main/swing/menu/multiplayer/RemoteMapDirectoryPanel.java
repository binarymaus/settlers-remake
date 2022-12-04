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
package jsettlers.main.swing.menu.multiplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import jsettlers.graphics.localization.Labels;
import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.main.swing.menu.openpanel.OpenPanel;
import jsettlers.network.client.IClientConnection;
import jsettlers.network.client.RemoteMapDirectory;

public class RemoteMapDirectoryPanel extends JPanel {

	private IClientConnection connection;
	private String currentDir = "/";
	private String enterDir = "/";
	private RemoteMapDirectory directory = null;
	private final JList<Object> directoryList;
	private final JProgressBar progressBar;
	private final RemoteMapEntryPanel selectedPanel;
	private static final String RETURN = "..";
	private static final String REFRESH = Labels.getString("multiplayer-mapslist-refresh");

	public RemoteMapDirectoryPanel(IClientConnection connection, OpenPanel openSinglePlayerPanel) {
		this.connection = connection;

		progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
		progressBar.putClientProperty(ELFStyle.KEY, ELFStyle.PROGRESSBAR_SLIDER);
		selectedPanel = new RemoteMapEntryPanel(connection, progressBar, openSinglePlayerPanel);
		progressBar.setVisible(false);
		progressBar.setStringPainted(true);
		selectedPanel.setVisible(false);

		directoryList = new JList<>(new Object[0]);
		directoryList.setCellRenderer(new RemoteMapDirectoryCellRenderer());
		directoryList.setOpaque(false);
		directoryList.addListSelectionListener(e -> {
			Object selected = directoryList.getSelectedValue();
			if(selected == null) return;

			if(selected == RETURN) {
				String newDir = currentDir.substring(0, currentDir.lastIndexOf('/'));
				select(newDir.substring(0, newDir.lastIndexOf('/')+1));
				selectedPanel.setVisible(false);
			} else if(selected == REFRESH) {
				select(currentDir);
			} else {
				if(selected instanceof String) {
					select(currentDir + selected + "/");
					selectedPanel.setVisible(false);
				} else {
					selectedPanel.select(currentDir, (RemoteMapDirectory.RemoteMapEntry)selected);
					selectedPanel.setVisible(true);
					return; // don't clear selection
				}
			}
			directoryList.clearSelection();
		});

		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
		setLayout(new BorderLayout());
		content.add(progressBar, BorderLayout.NORTH);
		content.add(directoryList, BorderLayout.CENTER);
		content.add(selectedPanel, BorderLayout.SOUTH);
		add(content, BorderLayout.CENTER);

		SwingUtilities.updateComponentTreeUI(this);
	}

	private void select(String directory) {
		enterDir = directory;
		connection.action(IClientConnection.EClientAction.GET_MAPS_DIR, directory);
	}

	void update() {
		selectedPanel.update();

		if(enterDir == null) return;

		RemoteMapDirectory directory = connection.getMaps(enterDir);
		if(directory != null && this.directory != directory) {
			this.directory = directory;
			currentDir = enterDir;
			enterDir = null;
			directoryList.setModel(new RemoteMapDirectoryModel(directory));
			progressBar.setIndeterminate(false);
			progressBar.setVisible(false);
		} else {
			progressBar.setIndeterminate(true);
			progressBar.setVisible(true);
		}
	}

	private class RemoteMapDirectoryModel extends AbstractListModel<Object> {

		private RemoteMapDirectoryModel(RemoteMapDirectory directory) {
			this.directory = directory;
		}

		private RemoteMapDirectory directory;

		@Override
		public int getSize() {
			if(directory == null) return 0;
			int size = 1;
			if(!currentDir.equals("/")) size++;
			return directory.directories.size() + directory.maps.size() + size;
		}

		@Override
		public Object getElementAt(int i) {
			if(directory == null) return "";
			if(i == 0) return REFRESH;
			i--;
			if(!currentDir.equals("/")) {
				if (i == 0) return RETURN;
				i--;
			}

			if(i < directory.directories.size()) return directory.directories.get(i);
			i -= directory.directories.size();

			return directory.maps.get(i);
		}
	}

	private class RemoteMapDirectoryCellRenderer implements javax.swing.ListCellRenderer<Object> {
		private JLabel simpleStringLabel;

		public RemoteMapDirectoryCellRenderer() {
			simpleStringLabel = new JLabel();
			simpleStringLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_LONG);
			simpleStringLabel.updateUI();
		}

		@Override
		public Component getListCellRendererComponent(JList<?> jList, Object s, int i, boolean isSelected, boolean b1) {
			if(i % 2 == 0) {
				simpleStringLabel.setForeground(Color.WHITE);
			} else {
				simpleStringLabel.setForeground(Color.LIGHT_GRAY);
			}
			if(s instanceof RemoteMapDirectory.RemoteMapEntry) {
				RemoteMapDirectory.RemoteMapEntry obj = (RemoteMapDirectory.RemoteMapEntry) s;
				if(isSelected) simpleStringLabel.setForeground(Color.CYAN);
				simpleStringLabel.setText(Labels.getString("multiplayer-mapslist-entry", obj.name, obj.author));
			} else {
				simpleStringLabel.setText(s.toString());
			}
			return simpleStringLabel;
		}
	}
}
