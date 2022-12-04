package jsettlers.common.landscape;

public enum EPartitionType {
	LAND_PARTITION(1),
	SEA_PARTITION(-1),
	BLOCKED(0);

	public final int signum;

	EPartitionType(int signum) {
		this.signum = signum;
	}

	public static EPartitionType fromInt(int i) {
		if(i < 0) {
			return SEA_PARTITION;
		} else if(i > 0) {
			return LAND_PARTITION;
		}
		return BLOCKED;
	}
}
