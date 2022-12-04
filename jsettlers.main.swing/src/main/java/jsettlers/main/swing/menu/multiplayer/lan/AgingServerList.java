package jsettlers.main.swing.menu.multiplayer.lan;

import jsettlers.network.NetworkConstants;
import jsettlers.network.server.lan.LanServerAddressBroadcastListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AgingServerList extends DefaultListModel<InetAddress> implements LanServerAddressBroadcastListener.ILanServerAddressListener {

	private final Map<InetAddress, Long> servers = Collections.synchronizedMap(new HashMap<>());

	@Override
	public boolean foundServerAddress(InetAddress address) {
		Object prev = servers.put(address, System.nanoTime());

		if(prev == null) {
			SwingUtilities.invokeLater(() -> addElement(address));
		}
		return false;
	}

	public void update() {
		final long removeTime = System.nanoTime() - 3* NetworkConstants.Server.MAX_BROADCAST_DELAY*1000L*1000L;
		synchronized(servers) {
			Iterator<Map.Entry<InetAddress, Long>> iter = servers.entrySet().iterator();
			while(iter.hasNext()) {
				Map.Entry<InetAddress, Long> entry = iter.next();
				if(entry.getValue() <= removeTime) {
					removeElement(entry.getKey());
					iter.remove();
				}
			}
		}
	}

	public JList<InetAddress> createSwingComponent() {
		JList<InetAddress> serverList = new JList<>(this);
		serverList.setCellRenderer(new ServerListCellRenderer(this));
		return serverList;
	}

	public long getLastReceiveTime(InetAddress address) {
		return servers.get(address);
	}
}
