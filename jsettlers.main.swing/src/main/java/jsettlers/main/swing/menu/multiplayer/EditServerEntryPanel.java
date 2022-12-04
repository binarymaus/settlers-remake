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
import java.awt.CardLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jsettlers.graphics.localization.Labels;
import jsettlers.main.swing.SimpleDocumentListener;
import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.main.swing.settings.ServerEntry;
import jsettlers.main.swing.settings.ServerManager;
import jsettlers.network.client.EServerType;

public class EditServerEntryPanel extends JPanel {

	private ServerEntry origEntry;
	private ServerEntry edit;

	private JTextField nameField;
	private JComboBox<EServerType> entryType;

	private JTextField addressField;
	private JTextField usernameField;

	private JTextField urlField;

	public EditServerEntryPanel(Runnable exitAction) {
		this(exitAction, exitAction, null);
	}

	public EditServerEntryPanel(Runnable exitAction, Runnable saveAction, ServerEntry editEntry) {
		origEntry = editEntry;
		if(origEntry == null) {
			edit = new ServerEntry();
		} else {
			edit = origEntry.clone();
		}

		JPanel settingsPanel = new JPanel();
		// top options
		JPanel genericOptions = new JPanel();
		// all bottom options
		JPanel typePanel = new JPanel();
		// bottom options variant for jsettlers
		JPanel jsettlersOptions = new JPanel();
		// bottom options variant for http
		JPanel httpOptions = new JPanel();
		// save, reset, cancel buttons
		JPanel saveOptions = new JPanel();

		setLayout(new BorderLayout());

		settingsPanel.setLayout(new BorderLayout());
		genericOptions.setLayout(new GridLayout(0, 2, 10, 10));
		typePanel.setLayout(new CardLayout());
		jsettlersOptions.setLayout(new GridLayout(0, 2, 10, 10));
		httpOptions.setLayout(new GridLayout(0, 2, 10, 10));
		saveOptions.setLayout(new GridLayout(1, 3));

		add(settingsPanel, BorderLayout.PAGE_START);
		settingsPanel.add(genericOptions, BorderLayout.PAGE_START);
		settingsPanel.add(typePanel, BorderLayout.CENTER);
		settingsPanel.add(saveOptions, BorderLayout.PAGE_END);

		typePanel.add(jsettlersOptions, EServerType.JSETTLERS.toString());
		typePanel.add(httpOptions, EServerType.HTTP.toString());

		// name option
		JLabel nameLabel = new JLabel(Labels.getString("multiplayer-server-name"));
		nameLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_SHORT);
		genericOptions.add(nameLabel);

		nameField = new JTextField();
		nameField.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		nameField.getDocument().addDocumentListener((SimpleDocumentListener) () -> edit.setAlias(nameField.getText()));
		genericOptions.add(nameField);

		// type option
		JLabel typeLabel = new JLabel(Labels.getString("multiplayer-server-type"));
		typeLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_SHORT);
		genericOptions.add(typeLabel);

		entryType = new JComboBox<>(EServerType.values());
		entryType.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		entryType.addItemListener(itemEvent -> {
			edit.setType((EServerType) itemEvent.getItem());
			((CardLayout)typePanel.getLayout()).show(typePanel, itemEvent.getItem().toString());
		});
		if(origEntry != null) {
			entryType.setSelectedItem(origEntry.getType());
			entryType.setEnabled(false);
		}
		genericOptions.add(entryType);


		// jsettlers

		// address
		JLabel addressLabel = new JLabel(Labels.getString("multiplayer-server-address"));
		addressLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_SHORT);
		jsettlersOptions.add(addressLabel);

		addressField = new JTextField();
		addressField.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		addressField.getDocument().addDocumentListener((SimpleDocumentListener) () -> edit.setAddress(addressField.getText()));
		jsettlersOptions.add(addressField);

		// username
		JLabel usernameLabel = new JLabel(Labels.getString("multiplayer-server-username"));
		usernameLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_SHORT);
		jsettlersOptions.add(usernameLabel);

		usernameField = new JTextField();
		usernameField.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		usernameField.getDocument().addDocumentListener((SimpleDocumentListener) () -> edit.setUsername(usernameField.getText()));
		jsettlersOptions.add(usernameField);

		// http

		// url
		JLabel urlLabel = new JLabel(Labels.getString("multiplayer-server-url"));
		urlLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_SHORT);
		httpOptions.add(urlLabel);

		urlField = new JTextField();
		urlField.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		urlField.getDocument().addDocumentListener((SimpleDocumentListener) () -> edit.setURL(urlField.getText()));
		httpOptions.add(urlField);

		// save options
		JButton cancel = new JButton();
		if(origEntry != null) {
			cancel.setText(Labels.getString("multiplayer-server-delete"));
		} else {
			cancel.setText(Labels.getString("multiplayer-server-cancel"));
		}
		cancel.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_STONE);
		cancel.addActionListener(e -> {
			exitAction.run();
			if(origEntry != null) {
				ServerManager.getInstance().removeServer(origEntry);
			}
		});
		saveOptions.add(cancel);

		JButton reset = new JButton(Labels.getString("multiplayer-server-reset"));
		reset.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_STONE);
		reset.setVisible(origEntry != null);
		reset.addActionListener(e -> {
			edit = origEntry.clone();
			updateContent();
		});
		saveOptions.add(reset);

		JButton save = new JButton(Labels.getString("multiplayer-server-save"));
		save.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_STONE);
		save.addActionListener(e -> {
			if(!checkValues()) return;

			if(origEntry != null) {
				origEntry.set(edit);
				ServerManager.getInstance().updateFile();
			} else {
				ServerManager.getInstance().addServer(edit);
			}
			saveAction.run();
		});
		saveOptions.add(save);

		updateContent();
	}

	private boolean isValid(String str) {
		return str == null || str.isEmpty();
	}

	private boolean checkValues() {
		if(isValid(edit.getAlias())) return false;
		switch (edit.getType()) {
			case JSETTLERS:
				if(isValid(edit.getAddress())) return false;
				if(isValid(edit.getUsername())) return false;
				break;
			case HTTP:
				if(edit.getURL().isEmpty()) return false;
				break;
			default:
				return false;
		}

		return true;
	}

	private void updateContent() {
		nameField.setText(edit.getAlias());
		entryType.setSelectedIndex(edit.getType().ordinal());

		addressField.setText(edit.getAddress());
		usernameField.setText(edit.getUsername());

		String url = edit.getURL();
		if(url == null) url = ServerEntry.URL_EMPTY;
		urlField.setText(url);
	}
}
