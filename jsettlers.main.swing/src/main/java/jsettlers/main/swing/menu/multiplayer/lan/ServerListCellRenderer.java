package jsettlers.main.swing.menu.multiplayer.lan;


import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.network.NetworkConstants;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.net.InetAddress;

public class ServerListCellRenderer implements ListCellRenderer<InetAddress> {

	private final AgingServerList serverList;

	private final JPanel content = new JPanel();
	private final JLabel label = new JLabel();


	private final Color SELECTION_BACKGROUND = UIManager.getColor("ServerListCellRenderer.backgroundSelected");
	private final Color BACKGROUND_EVEN = UIManager.getColor("ServerListCellRenderer.backgroundColorEven");
	private final Color BACKGROUND_ODD = UIManager.getColor("ServerListCellRenderer.backgroundColorOdd");

	private final Color FONT_COLOR = UIManager.getColor("ServerListCellRenderer.foregroundColor");

	public ServerListCellRenderer(AgingServerList serverList) {
		this.serverList = serverList;

		content.putClientProperty(ELFStyle.KEY, ELFStyle.PANEL_DRAW_BG_CUSTOM);
		label.setForeground(FONT_COLOR);
		content.setLayout(new BorderLayout());
		content.add(label, BorderLayout.CENTER);
		SwingUtilities.updateComponentTreeUI(content);
	}

	@Override
	public Component getListCellRendererComponent(JList list,
												  InetAddress value,
												  int index,
												  boolean isSelected,
												  boolean cellHasFocus) {

		if(isSelected) {
			content.setBackground(SELECTION_BACKGROUND);
		} else if(index % 2 == 0) {
			content.setBackground(BACKGROUND_EVEN);
		} else {
			content.setBackground(BACKGROUND_ODD);
		}

		String hostAddr = value.getHostAddress();
		String hostname = value.getCanonicalHostName();

		String ipString = hostAddr;
		if(!hostAddr.equals(hostname)) {
			ipString = hostAddr + "(" + hostname + ")";
		}

		String timeString = "";
		long since = (System.nanoTime() - serverList.getLastReceiveTime(value))/(1000L*1000L*1000L);
		if(since > NetworkConstants.Server.MAX_BROADCAST_DELAY/1000) {
			timeString = " / (" + since + "s ago)";
		}

		label.setText(ipString + timeString);
		return content;
	}
}