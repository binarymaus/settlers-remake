/*******************************************************************************
 * Copyright (c) 2015
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
package jsettlers.network.server.lan;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import jsettlers.network.NetworkConstants;
import jsettlers.network.infrastructure.log.Logger;

/**
 * This thread broadcasts a small package over the network to inform LAN members of the server address
 * 
 * @author Andreas Eberle
 * 
 */
public final class LanServerBroadcastThread {

	private final DatagramSocket socket;
	private final Timer broadcastTimer;
	private final TimerTask broadcastTask;
	private final Logger logger;
	private boolean active;

	public LanServerBroadcastThread(Logger logger) throws SocketException {
		this.logger = logger;
		socket = new DatagramSocket();
		active = false;

		broadcastTask = new TimerTask() {
			@Override
			public void run() {
				broadcast();
			}
		};

		broadcastTimer = new Timer("LanServerBroadcastThread", true);
	}

	private void broadcast() {
		byte[] data = NetworkConstants.Server.BROADCAST_MESSAGE.getBytes();

		try {
			broadcast(NetworkConstants.Server.BROADCAST_PORT, socket, data);
		} catch (IOException e) {
			logger.error(e);
			logger.warn("Stopped server broadcasting due to exception.");
			shutdown();
		}
	}

	private void broadcast(int udpPort, DatagramSocket socket, byte[] data) throws IOException {
		InetAddress dst6 = InetAddress.getByAddress(NetworkConstants.Server.MULTICAST_IP6);
		InetAddress dst4 = InetAddress.getByAddress(new byte[]{-1, -1, -1, -1});

		socket.send(new DatagramPacket(data, data.length, dst6, udpPort));
		socket.send(new DatagramPacket(data, data.length, dst4, udpPort));
	}

	public void start() {
		assert !active;

		active = true;
		broadcastTimer.schedule(broadcastTask, 0, NetworkConstants.Server.BROADCAST_DELAY);
	}

	public void shutdown() {
		if(!active) return;
		broadcastTimer.cancel();
		socket.close();
		active = false;
	}

	public boolean isAlive() {
		return active;
	}
}
