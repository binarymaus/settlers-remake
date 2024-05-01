package jsettlers.logic.movable.civilian;

import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.Root;
import jsettlers.common.map.shapes.MapCircle;
import jsettlers.common.map.shapes.MapCircleIterator;
import jsettlers.common.movable.EDirection;
import jsettlers.common.movable.EEffectType;
import jsettlers.common.movable.EMovableAction;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.IPlayer;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.utils.coordinates.CoordinateStream;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.movable.Movable;
import jsettlers.logic.movable.MovableManager;
import jsettlers.logic.movable.interfaces.AbstractMovableGrid;
import jsettlers.logic.movable.interfaces.IAttackableHumanMovable;
import jsettlers.logic.movable.interfaces.IHealerMovable;
import jsettlers.logic.movable.interfaces.ILogicMovable;
import jsettlers.logic.movable.military.MageMovable;
import jsettlers.logic.movable.other.AttackableHumanMovable;
import jsettlers.logic.player.Player;

import static jsettlers.algorithms.simplebehaviortree.BehaviorTreeHelper.*;

import java.util.Comparator;
import java.util.List;

public class HealerMovable extends BuildingWorkerMovable implements IHealerMovable {

	private static final long serialVersionUID = 1L;

	private IAttackableHumanMovable patient = null;

	private IAttackableHumanMovable nextPatient = null;

	public HealerMovable(AbstractMovableGrid grid, ShortPoint2D position, Player player, Movable replace) {
		super(grid, EMovableType.HEALER, position, player, replace);
	}

	static {
		MovableManager.registerBehaviour(EMovableType.HEALER, new Root<>(createHealerBehaviour()));
	}

	private static Node<HealerMovable> createHealerBehaviour() {
		return defaultWorkCycle(
				sequence(
					waitFor(
						sequence(
							isAllowedToWork()
							//condition(HealerMovable::canHeal)
						)
					),
					show(),
					ignoreFailure(
						sequence(
							action(mov -> {
								mov
								.sort(mov.spellRegion())
								.map(mov::getMovableAt)
								.filter(lm -> lm!=null&&lm.isAlive()&&lm.getMovableType().isSoldier())
								.filter(lm -> lm.getPlayer().getTeamId() == mov.getPlayer().getTeamId())
								.filter(lm -> ((AttackableHumanMovable)lm).needsTreatment())
								.forEach(movable -> {
									var attackable = (AttackableHumanMovable)movable;
									mov.playHealAnimation(attackable.getPosition());
									attackable.healPercentage(20f);
								});
							})
						)
					),
					hide(),
					sleep(2000)
				)
		);
	}

	private void playHealAnimation(ShortPoint2D position) {
		grid.playHealAnimation(position, 78, 115, 1, player);
	}

	private CoordinateStream sort(CoordinateStream stream) {

		List<ShortPoint2D> points = stream.toList();
		points.sort(Comparator.comparingInt(pt -> pt.getOnGridDistTo(position)));
		return CoordinateStream.fromList(points);
	}

	private ILogicMovable getMovableAt(int x, int y) {
		if(!grid.isInBounds(x, y)) {
			return null;
		}
		return grid.getMovableAt(x, y);
	}

	private CoordinateStream spellRegion() {
		return new MapCircle(building.getWorkAreaCenter(), building.getBuildingVariant().getWorkRadius()).stream();
	}


	private boolean canHeal(ShortPoint2D pos, boolean leave) {
		ILogicMovable movable = grid.getMovableAt(pos.x, pos.y);
		if (movable instanceof IAttackableHumanMovable &&
				((IAttackableHumanMovable) movable).needsTreatment() &&
				movable.getPlayer().getTeamId() == player.getTeamId()) {
			nextPatient = (IAttackableHumanMovable) movable;
			return true;
		} else {
			nextPatient = null;
			if (movable != null && leave) movable.leavePosition();
			return false;
		}
	}

	private boolean canHeal() {
		ShortPoint2D healSpot = getHealSpot();

		// TODO use a more reasonable way of searching for potential patients

		if(patient != null && patient.getPosition().getOnGridDistTo(healSpot) <= 5 &&
				canHeal(patient.getPosition(), false)) return true;

		if(canHeal(healSpot, true)) return true;

		for(EDirection dir : EDirection.VALUES) {
			if(canHeal(dir.getNextHexPoint(healSpot), false)) return true;
		}

		return false;
	}

	private void callWounded() {
		// check if patient is still interested
		if (patient != null && !patient.isGoingToTreatment()) {
			patient = null;
		}

		if (patient != null) return;

		IAttackableHumanMovable bestPatient = null;
		float patientHealth = Float.MAX_VALUE;
		MapCircleIterator iter = new MapCircleIterator(new MapCircle(building.getWorkAreaCenter(), building.getBuildingVariant().getWorkRadius()));

		int width = grid.getWidth();
		int height = grid.getHeight();
		while (iter.hasNext()) {
			ShortPoint2D next = iter.next();
			if (next.x > 0 && next.x < width && next.y > 0 && next.y < height) {
				ILogicMovable potentialPatient = grid.getMovableAt(next.x, next.y);
				if (potentialPatient instanceof IAttackableHumanMovable) {
					IAttackableHumanMovable realPotentialPatient = (IAttackableHumanMovable) potentialPatient;


					if(potentialPatient.getPlayer() == player &&
							!realPotentialPatient.isGoingToTreatment() &&
							realPotentialPatient.needsTreatment()) {
						float newHealth = potentialPatient.getHealth();
						if (newHealth < patientHealth) {
							bestPatient = (IAttackableHumanMovable) potentialPatient;
							patientHealth = newHealth;
						}
					}
				}
			}
		}

		if(bestPatient != null) bestPatient.pingWounded(this);
	}

	private boolean heal() {
		if(nextPatient != null) {
			nextPatient.heal();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public ShortPoint2D getHealSpot() {
		if(building == null) return null;
		return building.getBuildingVariant().getHealSpot().calculatePoint(building.getPosition());
	}
}
