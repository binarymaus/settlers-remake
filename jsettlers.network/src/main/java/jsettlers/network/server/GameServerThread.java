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
package jsettlers.network.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import jsettlers.network.NetworkConstants;
import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.infrastructure.channel.socket.ISocketFactory;
import jsettlers.network.infrastructure.log.Logger;
import jsettlers.network.infrastructure.log.LoggerManager;
import jsettlers.network.server.db.IDBFacade;
import jsettlers.network.server.db.inMemory.InMemoryDB;
import jsettlers.network.server.lan.LanServerAddressBroadcastListener;
import jsettlers.network.server.lan.LanServerBroadcastThread;
import jsettlers.network.server.lan.SingleLanServerAddressListener;

/**
 * 
 * @author Andreas Eberle
 * 
 */
public final class GameServerThread extends Thread {

	private final ServerSocket serverSocket;
	private final ServerManager manager;
	private final LanServerBroadcastThread lanBroadcastThread;
	private final Logger logger;

	private long counter = 0;
	private boolean canceled = false;

	public GameServerThread(boolean lan) throws IOException {
		this(lan, LoggerManager.ROOT_LOGGER);
	}

	public GameServerThread(boolean lan, Logger logger) throws IOException {
		super("GameServer");
		this.logger = logger;
		this.serverSocket = new ServerSocket(NetworkConstants.Server.SERVER_PORT);
		this.manager = new ServerManager(new InMemoryDB());

		this.setDaemon(true);

		if (lan) {
			lanBroadcastThread = new LanServerBroadcastThread(logger);
		} else {
			lanBroadcastThread = null;
		}
	}

	@Override
	public void run() {
		logger.log("Server up and running!\n");
		System.out.println("Server up and running!");
		while (!canceled) {
			try {
				Socket clientSocket = serverSocket.accept();

				Channel clientChannel = new Channel(logger, ISocketFactory.DEFAULT_FACTORY.generateSocket(clientSocket));
				manager.identifyNewChannel(clientChannel);
				clientChannel.start();

				logger.log("accepted new client (" + ++counter + "): " + clientSocket);
			} catch (SocketException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * NOTE: THIS METHOD IS BLOCKING for the given time
	 * 
	 * @param waitMS
	 *            block at maximum the given number of milliseconds to find the address
	 * 
	 * @return
	 */
	public static InetAddress retrieveLanServerAddress(int waitMS) {
		SingleLanServerAddressListener listener = new SingleLanServerAddressListener();
		LanServerAddressBroadcastListener serverAddressReceiver = new LanServerAddressBroadcastListener(listener);
		try {
			serverAddressReceiver.start();

			try {
				serverAddressReceiver.join(waitMS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			InetAddress address = listener.getAddress();
			if (address != null) {
				System.out.println("found server!");
				return address;
			} else {
				return null;
			}
		} finally {
			serverAddressReceiver.shutdown();
		}
	}

	@Override
	public synchronized void start() {
		super.start();
		manager.start();
		if(lanBroadcastThread != null) {
			lanBroadcastThread.start();
		}
	}

	public synchronized void shutdown() {
		canceled = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
		}

		if(lanBroadcastThread != null) {
			lanBroadcastThread.shutdown();
		}

		manager.shutdown();
	}

	public boolean isLanBroadcasterAlive() {
		return lanBroadcastThread != null && lanBroadcastThread.isAlive();
	}

	public IDBFacade getDatabase() {
		return manager.getDatabase();
	}
}
