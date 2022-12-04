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
import java.util.concurrent.atomic.AtomicBoolean;

import jsettlers.network.NetworkConstants;

/**
 * Listens for a broadcasted address of a LAN game server.
 * 
 * @author Andreas Eberle
 * 
 */
public final class LanServerAddressBroadcastListener extends Thread {

	private final ILanServerAddressListener listener;

	private final AtomicBoolean canceled = new AtomicBoolean(false);
	private DatagramSocket socket;

	public LanServerAddressBroadcastListener(ILanServerAddressListener listener) {
		super("LanServerAddressListener");
		this.listener = listener;
		super.setDaemon(true);
	}

	@Override
	public void run() {
		try {
			socket = new DatagramSocket(NetworkConstants.Server.BROADCAST_PORT);

			boolean cancelListener = false;
			while (!canceled.get() && !cancelListener) {
				try {
					DatagramPacket packet = new DatagramPacket(new byte[NetworkConstants.Server.BROADCAST_BUFFER_LENGTH],
							NetworkConstants.Server.BROADCAST_BUFFER_LENGTH);

					socket.receive(packet);

					String receivedMessage = new String(packet.getData(), packet.getOffset(), packet.getLength());

					if (NetworkConstants.Server.BROADCAST_MESSAGE.equals(receivedMessage)) {

						if (listener != null) {
							cancelListener = listener.foundServerAddress(packet.getAddress());
						}
					}
				} catch (SocketException e) {
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		} finally {
			socket.close();
		}
	}

	public interface ILanServerAddressListener {

		/**
		 *
		 * @param address the last found address
		 * @return if the listener broadcast thread should be shut down
		 */
		boolean foundServerAddress(InetAddress address);
	}

	public void shutdown() {
		canceled.set(true);
		socket.close();
	}
}
