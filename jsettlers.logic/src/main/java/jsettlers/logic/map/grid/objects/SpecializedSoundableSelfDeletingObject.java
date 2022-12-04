package jsettlers.logic.map.grid.objects;

import jsettlers.common.mapobject.EMapObjectType;
import jsettlers.common.mapobject.ISpecializedMapObject;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.objects.SelfDeletingMapObject;
import jsettlers.logic.objects.SoundableSelfDeletingObject;
import jsettlers.logic.player.Player;

class SpecializedSoundableSelfDeletingObject extends SoundableSelfDeletingObject implements ISpecializedMapObject {

	private static final long serialVersionUID = 5929232729004964265L;
	private int sound;
	private int animation;
	private float areaOfEffect;
	private float volume;

	public SpecializedSoundableSelfDeletingObject(ShortPoint2D point, int sound, int animation, float duration, Player player) {
		super(point, EMapObjectType.SPELL_EFFECT, duration, player);
		this.animation = animation;
		this.sound = sound;
		this.areaOfEffect = 100f;
		this.volume = 2.0f;
	}

	public float getVolume() {
		return volume;
	}

	public float getAreaOfEffect() {
		return areaOfEffect;
	}

	public int getSound() {
		return sound;
	}

	public int getAnimation() {
		return animation;
	}
}
