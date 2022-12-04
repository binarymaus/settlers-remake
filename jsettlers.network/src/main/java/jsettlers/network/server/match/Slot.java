package jsettlers.network.server.match;

public class Slot {
	private byte type;
	private byte team;
	private byte civilisation;
	private byte position;

	public Slot(byte type, byte team, byte civilisation, byte position) {
		this.type = type;
		this.team = team;
		this.civilisation = civilisation;
		this.position = position;
	}

	public byte getType() {
		return type;
	}

	public byte getTeam() {
		return team;
	}

	public byte getCivilisation() {
		return civilisation;
	}

	public byte getPosition() {
		return position;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public void setTeam(byte team) {
		this.team = team;
	}

	public void setCivilisation(byte civilisation) {
		this.civilisation = civilisation;
	}

	public void setPosition(byte position) {
		this.position = position;
	}
}
