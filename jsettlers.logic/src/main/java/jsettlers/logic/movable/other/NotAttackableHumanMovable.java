package jsettlers.logic.movable.other;

import jsettlers.algorithms.simplebehaviortree.Root;
import jsettlers.common.action.EMoveToType;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.IPlayer;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.movable.Movable;
import jsettlers.logic.movable.interfaces.AbstractMovableGrid;
import jsettlers.logic.movable.interfaces.IAttackable;
import jsettlers.logic.movable.interfaces.IAttackableHumanMovable;
import jsettlers.logic.movable.interfaces.IFerryMovable;
import jsettlers.logic.movable.interfaces.IHealerMovable;
import jsettlers.logic.movable.interfaces.INotAttackableHumanMovable;
import jsettlers.logic.player.Player;

public class NotAttackableHumanMovable extends Movable implements INotAttackableHumanMovable {

	private static final long serialVersionUID = 6890695823402563L;
	protected EMoveToType nextMoveToType;
	protected ShortPoint2D nextTarget = null;
	protected boolean goingToHealer = false;

	// the following data only for ship passengers
	protected IFerryMovable ferryToEnter = null;
	public boolean isStopped = false;

	public NotAttackableHumanMovable(AbstractMovableGrid grid, EMovableType movableType, ShortPoint2D position, Player player, Movable movable) {
		super(grid, movableType, position, player, movable);
	}

	@Override
	public void moveTo(ShortPoint2D targetPosition, EMoveToType moveToType) {
		if(!playerControlled) return;

		nextTarget = targetPosition;
		nextMoveToType = moveToType;
		goingToHealer = false;
		isStopped = false;
	}

	@Override
	public void receiveHit(float hitStrength, ShortPoint2D attackerPos, IPlayer attackingPlayer) {

	}

	@Override
	public final boolean isAttackable() {
		return false;
	}

	@Override
	public boolean isTower() {
		return false;
	}

	@Override
	public void stopOrStartWorking(boolean stop) {
		if(!playerControlled) return;

		nextTarget = position;
		nextMoveToType = stop? EMoveToType.FORCED : EMoveToType.DEFAULT;
		goingToHealer = false;
	}

	protected void enterFerry() {
		ferryToEnter = null;
	}

	@Override
	public ShortPoint2D getFoWPosition() {
		if(isOnFerry()) return null;

		return position;
	}

	@Override
	public void informAboutAttackable(IAttackable attackable) {
		
	}
}
