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
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.DirectoryMapLister;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.main.swing.menu.openpanel.OpenPanel;
import jsettlers.network.client.IClientConnection;
import jsettlers.network.client.RemoteMapDirectory;

public class RemoteMapEntryPanel extends JPanel {
	private JProgressBar progressBar;

	private JLabel nameLabel = new JLabel("", SwingConstants.CENTER);
	private JLabel author = new JLabel();
	private JTextPane desc = new JTextPane();
	private IClientConnection connection;
	private JToggleButton download;
	private String file;
	private String dir;

	public RemoteMapEntryPanel(IClientConnection connection, JProgressBar progressBar, OpenPanel openSinglePlayerPanel) {
		this.connection = connection;
		this.progressBar = progressBar;

		nameLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_HEADER);
		author.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		download = new JToggleButton(Labels.getString("multiplayer-mapslist-download"));
		download.putClientProperty(ELFStyle.KEY, ELFStyle.TOGGLE_BUTTON_STONE);
		desc.setOpaque(false);

		download.addActionListener(e -> {
			connection.action(IClientConnection.EClientAction.DOWNLOAD_MAP, new Object[] {dir, file,
				(Runnable) () -> {
					DirectoryMapLister.ListedMapFile newMap = new DirectoryMapLister.ListedMapFile(new File("maps", file));
					MapList.getDefaultList().foundMap(newMap);
					openSinglePlayerPanel.setMapLoaders(MapList.getDefaultList().getFreshMaps().getItems());
				}
			});
			progressBar.setVisible(true);
			download.setSelected(true);
		});

		setLayout(new BorderLayout());
		add(nameLabel, BorderLayout.NORTH);
		add(author, BorderLayout.WEST);
		JScrollPane scroll = new JScrollPane(desc);
		scroll.setOpaque(false);
		add(scroll, BorderLayout.PAGE_END);
		desc.setEditable(false);
		add(download, BorderLayout.EAST);
	}

	public void select(String dir, RemoteMapDirectory.RemoteMapEntry entry) {
		nameLabel.setText(entry.name);
		author.setText(entry.author);
		desc.setText(entry.desc);
		file = entry.file;
		this.dir = dir;

		if(entry.file != null) {
			download.setSelected(false);
			download.setEnabled(true);
		} else {
			download.setSelected(true);
			download.setEnabled(false);
		}
	}

	void update() {
		long size = connection.getDownloadSize();
		if(size == -1) {
			progressBar.setVisible(false);
			if(file != null) download.setSelected(false);
			return;
		}
		long progress = connection.getDownloadProgress();
		long progressKB = progress/1024;

		progressBar.setIndeterminate(size==0);
		if(size != 0) {
			progressBar.setValue((int) (progress * 100.f / size));

			long sizeKB = size/1024;

			progressBar.setString(progressKB + "kB / " + sizeKB + "kB");
		} else if(progress != 0) {
			progressBar.setString(progressKB + "kB");
		} else {
			progressBar.setString("");
		}
	}
}
