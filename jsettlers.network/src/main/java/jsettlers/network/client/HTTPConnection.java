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
package jsettlers.network.client;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import java.util.function.Consumer;
import jsettlers.network.infrastructure.log.Logger;

public class HTTPConnection implements IClientConnection, Runnable {

	public static final int TIMEOUT = 3000; // 3 sec
	private static final long MIN_FETCH_DELAY = 10000; // 10 sec

	private static final Gson GSON = new Gson();

	public String url;
	private Logger log;
	private HttpURLConnection lastConnection;
	private int lastStatus = 0;

	private final Map<String, RemoteMapDirectory> mapDirectoryMap = new HashMap<>();

	private ArrayBlockingQueue<Action> actions = new ArrayBlockingQueue<>(10);

	private long downloadSize = -1;
	private long downloadProgress = -1;
	private final Object PROGRESS_SYNC = new Object();

	public HTTPConnection(String url, Logger logger) {
		log = logger;
		this.url = url;
		new Thread(this, "http-connect-thread").start();
	}

	@Override
	public long getDownloadProgress() {
		synchronized (PROGRESS_SYNC) {
			return downloadProgress;
		}
	}

	@Override
	public long getDownloadSize() {
		synchronized (PROGRESS_SYNC) {
			return downloadSize;
		}
	}

	@Override
	public void action(EClientAction action, Object argument) {
		actions.offer(new Action(action, argument));
	}

	@Override
	public void run() {
		try {
			readIndex("/");
		} catch (Throwable e) {
			log.error(e);
		}

		while(true) {
			Action nextAction;
			try {
				nextAction = actions.take();
			} catch (InterruptedException e) {
				log.error(e);
				nextAction = new Action(EClientAction.CLOSE, null);
			}

			try {
				switch (nextAction.action) {
					case GET_MAPS_DIR:
						readIndex(nextAction.argument.toString());
						break;
					case DOWNLOAD_MAP: {
						Object[] args = (Object[]) nextAction.argument;
						download((String)args[0], (String)args[1], (Runnable)args[2]);
						}
						break;
					case FIND_MAP: {
						Object[] args = (Object[]) nextAction.argument;
						findMap((String)args[0], (Consumer<String>)args[1]);
						}
						break;
					case CLOSE:
						if(lastConnection != null) lastConnection.disconnect();
						return;
				}
			} catch (Throwable e) {
				log.error(e);
			}
		}
	}

	@Override
	public RemoteMapDirectory getMaps(String directory) {
		synchronized (mapDirectoryMap) {
			return mapDirectoryMap.get(directory);
		}
	}

	private void findMap(String arg, Consumer<String> arg1) throws IOException {
		try {
			openResource("/find/" + arg, "GET", 0L);
		} catch (Throwable ex) {
			ex.printStackTrace();
			arg1.accept(null);
			return;
		}

		if(lastConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
			arg1.accept(null);
			return;
		}

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(lastConnection.getInputStream()))) {
			String line = reader.readLine();
			arg1.accept(line);
		}
	}

	@Override
	public String findMap(String id) {
		Semaphore finishLock = new Semaphore(1);
		AtomicReference<String> path = new AtomicReference<>(null);

		finishLock.acquireUninterruptibly();
		action(EClientAction.FIND_MAP, new Object[] {id,
			(Consumer<String>) str -> {
			path.set(str);
			finishLock.release();
		}});

		finishLock.acquireUninterruptibly();

		return path.get();
	}

	private static final int BUFFER_SIZE = 256*1024;

	private void download(String dir, String map, Runnable finish) throws IOException {
		openResource(dir + map, "GET", 0L);
		File str = new File("maps", map);

		synchronized (PROGRESS_SYNC) {
			downloadSize = lastConnection.getHeaderFieldInt("Content-Length", 0);
			downloadProgress = 0;
		}

		byte[] buffer = new byte[BUFFER_SIZE];
		try(InputStream from = lastConnection.getInputStream();
			OutputStream out = new FileOutputStream(str)) {
			int read;
			while ((read = from.read(buffer)) != -1) {
				out.write(buffer, 0, read);
				synchronized (PROGRESS_SYNC) {
					downloadProgress += read;
				}
			}
		}

		finish.run();

		finishProgress();
	}

	private void readIndex(String directory) throws IOException {
		RemoteMapDirectory cached = mapDirectoryMap.get(directory);
		long lastFetch = (cached==null) ? 0 : cached.date;
		long currentDate = new Date().getTime();

		if(lastFetch+MIN_FETCH_DELAY>currentDate) return;

		openResource(directory + "index.json", "GET", lastFetch);
		if(cached != null && lastConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
			cached.date = currentDate;
			return;
		}
		RemoteMapDirectory index;
		try(Reader reader = new InputStreamReader(lastConnection.getInputStream())) {
			index = GSON.fromJson(reader, RemoteMapDirectory.class);
		} finally {
			finishProgress();
		}
		index.date = currentDate;

		synchronized (mapDirectoryMap) {
			mapDirectoryMap.put(directory, index);
		}
	}

	private void openResource(String res, String method, long lastFetch) throws IOException {
		log.info(method + " " + res);
		synchronized (PROGRESS_SYNC) {
			// downloading but no progress yet
			downloadSize = 0;
			downloadProgress = 0;
		}
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url + res).openConnection();
			if(lastFetch == 0) connection.setIfModifiedSince(lastFetch);
			connection.setConnectTimeout(TIMEOUT);
			connection.setReadTimeout(TIMEOUT);
			connection.setRequestMethod(method);
			connection.connect();
			if(lastConnection != null) lastConnection.disconnect();
			lastConnection = connection;
			lastStatus = connection.getResponseCode();
		} catch(Throwable e) {
			lastStatus = -1;
			if(lastConnection != null) {
				lastConnection.disconnect();
				lastConnection = null;
			}
			finishProgress();
			throw new IOException(e);
		}
	}

	private void finishProgress() {
		synchronized (PROGRESS_SYNC) {
			downloadSize = -1;
			downloadProgress = -1;
		}
	}

	@Override
	public boolean hasConnectionFailed() {
		return lastStatus == -1;
	}

	@Override
	public boolean isConnected() {
		return lastConnection != null;
	}
}
