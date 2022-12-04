package go.graphics.sound;

public interface SoundHandle {
	void start();
	void pause();
	void stop();
	void dismiss();

	void setVolume(float volume);

	/**
	 *
	 * @return duration in ms
	 */
	int getPlaybackDuration();
}
