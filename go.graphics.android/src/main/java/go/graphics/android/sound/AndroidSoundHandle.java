package go.graphics.android.sound;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import java.io.File;

import go.graphics.sound.SoundHandle;
import java.io.IOException;

public class AndroidSoundHandle implements SoundHandle {

	private MediaPlayer player;
	private final File source;
	private final int length;
	private float volume = 1;

	public AndroidSoundHandle(File source) {
		this.source = source;

		MediaMetadataRetriever mdr  = new MediaMetadataRetriever();
		mdr.setDataSource(source.getPath());
		String lengthString = mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		mdr.release();

		// something went wrong
		if(lengthString == null) {
			length = -1;
			return;
		}

		length = Integer.parseInt(lengthString);
	}

	private void create() {
		try {
			if(player != null) return;

			player = new MediaPlayer();
			player.setDataSource(source.toString());
			player.prepare();
		} catch (IOException e) {
			e.printStackTrace();
			player = null;
		}
	}

	private void release() {
		if(player == null) return;

		player.release();
		player = null;
	}

	@Override
	public void start() {
		create();
		player.start();
		setVolume(volume);
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public void stop() {
		player.stop();
		release();
	}

	@Override
	public void dismiss() {
		release();
	}

	@Override
	public void setVolume(float volume) {
		this.volume = volume;

		if(player != null) {
			player.setVolume(volume, volume);
		}
	}

	@Override
	public int getPlaybackDuration() {
		return length;
	}
}
