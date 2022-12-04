package jsettlers.network.client;

public class NullConnection implements IClientConnection {
	@Override
	public boolean hasConnectionFailed() {
		return true;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public RemoteMapDirectory getMaps(String directory) {
		return null;
	}

	@Override
	public long getDownloadProgress() {
		return 0;
	}

	@Override
	public long getDownloadSize() {
		return 0;
	}

	@Override
	public void action(EClientAction action, Object argument) {

	}

	@Override
	public String findMap(String id) {
		return null;
	}
}
