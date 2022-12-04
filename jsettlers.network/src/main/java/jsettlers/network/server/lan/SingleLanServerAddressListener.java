package jsettlers.network.server.lan;

import java.net.InetAddress;

public final class SingleLanServerAddressListener implements LanServerAddressBroadcastListener.ILanServerAddressListener {

	public SingleLanServerAddressListener() {
		address = null;
	}

	private InetAddress address;

	@Override
	public boolean foundServerAddress(InetAddress address) {
		this.address = address;
		return true;
	}

	public InetAddress getAddress() {
		return address;
	}
}
