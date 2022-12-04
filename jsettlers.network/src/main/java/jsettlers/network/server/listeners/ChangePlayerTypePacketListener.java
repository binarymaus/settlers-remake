package jsettlers.network.server.listeners;

import java.io.IOException;
import jsettlers.network.NetworkConstants;
import jsettlers.network.common.packets.ByteTuplePacket;
import jsettlers.network.infrastructure.channel.GenericDeserializer;
import jsettlers.network.infrastructure.channel.listeners.PacketChannelListener;
import jsettlers.network.server.IServerManager;
import jsettlers.network.server.match.Player;

public class ChangePlayerTypePacketListener extends PacketChannelListener<ByteTuplePacket> {

	private final IServerManager serverManager;
	private final Player player;

	public ChangePlayerTypePacketListener(IServerManager serverManager, Player player) {
		super(NetworkConstants.ENetworkKey.CHANGE_PLAYER_TYPE, new GenericDeserializer<>(ByteTuplePacket.class));
		this.serverManager = serverManager;
		this.player = player;
	}

	@Override
	protected void receivePacket(NetworkConstants.ENetworkKey key, ByteTuplePacket packet) throws IOException {
		serverManager.setPlayerTypeForSlot(player, packet.getValueA(), packet.getValueB());
	}
}
