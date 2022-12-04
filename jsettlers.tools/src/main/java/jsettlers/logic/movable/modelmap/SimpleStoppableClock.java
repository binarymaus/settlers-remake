package jsettlers.logic.movable.modelmap;

public class SimpleStoppableClock {

	public SimpleStoppableClock() {
		setSpeed(1);
	}

	private float lastStopTime;
	private long lastStopTimeIndex;
	private float speed;
	private boolean stopped = false;

	public long getCurrentTime() {
		float realSpeed = stopped ? 0 : speed;

		return lastStopTimeIndex+(long)((System.nanoTime()-lastStopTime)*realSpeed/1000);
	}

	private void resetTime() {
		lastStopTimeIndex = getCurrentTime();
		lastStopTime = System.nanoTime();
	}

	public void setSpeed(float speed) {
		resetTime();
		this.speed = speed;
	}

	public void mulSpeed(float multiplier) {
		setSpeed(speed * multiplier);
	}

	public void toggleStopped() {
		setStopped(!stopped);
	}

	public void setStopped(boolean stop) {
		resetTime();
		stopped = stop;
	}
}
