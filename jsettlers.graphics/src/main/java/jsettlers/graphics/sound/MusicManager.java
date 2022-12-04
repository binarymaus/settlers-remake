package jsettlers.graphics.sound;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import go.graphics.sound.SoundHandle;
import jsettlers.common.CommonConstants;
import jsettlers.common.player.ECivilisation;
import go.graphics.sound.SoundPlayer;

/**
 *
 * music management of different s3 versions, provides CIVILISATION specific music management
 *
 * TODO: validate music files and integrate exception handling
 *
 * @author MarviMarv
 */
public final class MusicManager implements Runnable {

	private final static String[][] ULTIMATE_EDITION_MUSIC_SET = { { "02", "03", "12" }, { "06", "07", "14" }, { "04", "05", "13" }, { "08", "09", "10" }, { "11" } };
	private final static String[][] HISTORY_EDITION_MUSIC_SET = { { "02", "03", "04" }, { "05", "06", "07" }, { "08", "09", "10" }, { "13", "14", "15" }, { "11", "12" } };

	private static File lookupPath = null;

	public static void setLookupPath(final File lookupPath) {
		MusicManager.lookupPath = lookupPath;
	}

	private Thread musicThread = null;
	private final SoundPlayer soundPlayer;
	private final ECivilisation civilisation;
	private boolean paused = true;
	private SoundHandle activeTrack = null;
	private float volume;

	private final Semaphore waitMutex = new Semaphore(1);

	public MusicManager(SoundPlayer soundPlayer, ECivilisation civilisation) {
		this.soundPlayer = soundPlayer;
		this.civilisation = civilisation;

		volume = CommonConstants.MUSIC_VOLUME.get();
	}

	public boolean isRunning() {
		return !paused;
	}

	public void startMusic() {
		if(!isRunning()) {
			paused = false;
			musicThread = new Thread(this);
			musicThread.setName("MusicThread");
			musicThread.setDaemon(true);
			musicThread.start();
		}
	}

	public void stopMusic() {
		if (isRunning()) {
			if(activeTrack != null) activeTrack.pause();
			paused = true;

			waitMutex.release();

			try {
				musicThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void setMusicVolume(float volume, boolean relative) {
		if(relative) {
			this.volume += volume;
		} else {
			this.volume = volume;
		}

		if (isRunning() && activeTrack != null) {
			activeTrack.setVolume(this.volume);
		}
	}

	private List<File> assembleMusicSet(final ECivilisation civilisation, final boolean playAll) {
		// there are no music files available
		if(lookupPath == null || !lookupPath.exists()) return List.of();


		List<File> list = new ArrayList<>();
		// just take all mp3 and ogg files
		if(playAll) {
			list.addAll(Arrays.asList(lookupPath.listFiles((file, name) -> name.endsWith(".mp3") || name.endsWith(".ogg") || name.endsWith(".wav"))));
		} else if(lookupPath.getName().equals("Theme")) { // history edition
			for(String fileName : HISTORY_EDITION_MUSIC_SET[civilisation.ordinal]) {
				File file = new File(lookupPath, "Track" + fileName + ".mp3");
				if(file.exists()) list.add(file);
			}
		} else {
			// ultimate edition
			for(String fileName : ULTIMATE_EDITION_MUSIC_SET[civilisation.ordinal]) {
				File file = new File(lookupPath, "Track" + fileName + ".ogg");
				if(file.exists()) list.add(file);
			}
		}

		// this music folder is custom made
		if(list.isEmpty()) {
			list.addAll(Arrays.asList(lookupPath.listFiles((file, name) -> name.matches(civilisation.name() + ".\\d*.(mp3|ogg|wav)"))));
		}

		// shuffle list so we don't get bored :)
		Collections.shuffle(list);

		return list;
	}

	@Override
	public void run() {
		int trackIndex = 0;
		List<SoundHandle> musicSet = new ArrayList<>();
		for(File musicFile : assembleMusicSet(civilisation, CommonConstants.PLAYALL_MUSIC.get())) {
			musicSet.add(soundPlayer.openSound(musicFile));
		}

		// nothing to do here
		if(musicSet.isEmpty()) return;

		while (!paused) {
			activeTrack = musicSet.get(trackIndex);

			activeTrack.setVolume(volume);
			activeTrack.start();

			// wait until the track is completed or we are killed
			try {
				waitMutex.tryAcquire();
				waitMutex.tryAcquire(activeTrack.getPlaybackDuration(), TimeUnit.MILLISECONDS);
				waitMutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			activeTrack = null;

			trackIndex++;


			if (trackIndex == musicSet.size()) {
				trackIndex = 0;
			}
		}

		// cleanup music files
		for(SoundHandle handle : musicSet) {
			handle.dismiss();
		}
	}
}
