package jsettlers.logic.movable.military;

import jsettlers.algorithms.path.Path;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.Root;
import jsettlers.common.buildings.OccupierPlace;
import jsettlers.common.movable.EDirection;
import jsettlers.common.movable.EEffectType;
import jsettlers.common.movable.EMovableAction;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.IPlayer;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.buildings.military.occupying.IOccupyableBuilding;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.movable.MovableManager;
import jsettlers.logic.movable.interfaces.IAttackable;
import jsettlers.logic.movable.interfaces.IAttackableHumanMovable;
import jsettlers.logic.movable.other.AttackableHumanMovable;
import jsettlers.logic.movable.Movable;
import jsettlers.logic.movable.interfaces.AbstractMovableGrid;
import jsettlers.logic.player.Player;

import static jsettlers.algorithms.simplebehaviortree.BehaviorTreeHelper.*;

public class MountainKingMovable extends AttackableHumanMovable {

	private static final float INFANTRY_ATTACK_DURATION = 1;

	private static final long serialVersionUID = 667104393129440108L;

	protected IAttackable enemy;


	private IOccupyableBuilding building;
	private ShortPoint2D inTowerAttackPosition;
	protected boolean defending;

	protected ShortPoint2D currentTarget = null;
	protected ShortPoint2D goToTarget = null;

	protected int patrolStep = -1;
	public ShortPoint2D[] patrolPoints = null;

	public boolean enemyNearby;
	private IAttackable toCloseEnemy;
	protected ShortPoint2D startPoint;


	public MountainKingMovable(AbstractMovableGrid grid, EMovableType movableType, ShortPoint2D position, Player player, Movable movable) {
		super(grid, movableType, position, player, movable);

		enemyNearby = true; // might not actually be true
	}

	static {
		Root<MountainKingMovable> behaviour = new Root<>(createSoldierBehaviour());

		MovableManager.registerBehaviour(EMovableType.MOUNTAIN_KING, behaviour);
	}

	private static Node<MountainKingMovable> createSoldierBehaviour() {
		return guardSelector(
				handleFrozenEffect(),
				guard(mov -> mov.nextTarget != null,
						action(mov -> {
							mov.abortGoTo();

							switch(mov.nextMoveToType) {
								default:
								case DEFAULT:
									mov.currentTarget = mov.nextTarget;
									break;
								case FORCED:
									mov.goToTarget = mov.nextTarget;
									break;
								case PATROL:
									mov.patrolPoints = new ShortPoint2D[] {mov.position, mov.nextTarget};
									mov.patrolStep = 0;
									break;
							}

							mov.nextTarget = null;
						})
				),
				guard(mov -> mov.goToTarget != null,
						sequence(
								ignoreFailure(goToPos(mov -> mov.goToTarget)),
								action(mov -> {
									mov.enterFerry();
									mov.goToTarget = null;
								})
						)
				),
				// attack enemy
				guard(mov -> mov.enemyNearby,
						selector(
								sequence(
										// handle potential enemy
										findEnemy(),
										ignoreFailure(
												selector(
														// attack him
														attackEnemy(),

														condition(mov -> !mov.enemy.isAlive()), // enemy might die even if the attack fails

														// or roughly chase enemy
														goInDirectionIfAllowedAndFreeNode(mov -> EDirection.getApproxDirection(mov.position, mov.enemy.getPosition())),
														// or go to his position
														resetAfter(mov -> {
																	mov.startPoint = null;
																},
																sequence(
																		action(mov -> {
																			mov.startPoint = mov.position;
																		}),
																		goToPos(mov -> mov.enemy.getPosition(), mov -> {
																			// hit him
																			if(mov.isEnemyAttackable()) return false;
																			// update behaviour (adjust target)
																			if(mov.startPoint.getOnGridDistTo(mov.position) > 2) return false;
																			return true;
																		})
																)
														)
												)
										)
								),
								sequence(
										// handle nearby enemies (bowman only)
										findTooCloseEnemy(),
										// run in opposite direction
										ignoreFailure(goInDirectionIfAllowedAndFreeNode(mov -> EDirection.getApproxDirection(mov.toCloseEnemy.getPosition(), mov.position)))
								),
								sequence(
										// no enemy in sight
										action(mov -> {
											mov.enemyNearby = false;
										})
								)
						)
				),
				guard(mov -> mov.currentTarget != null,
						sequence(
								ignoreFailure(goToPos(mov -> mov.currentTarget)),
								action(mov -> {
									mov.currentTarget = null;
								})
						)
				),
				guard(mov -> mov.patrolStep != -1,
						sequence(
								ignoreFailure(goToPos(mov -> mov.patrolPoints[mov.patrolStep])),
								action(mov -> {
									mov.patrolStep = (mov.patrolStep+1) % mov.patrolPoints.length;
								})
						)
				)
		);
	}




	static Node<MountainKingMovable> findTooCloseEnemy() {
		return sequence(
				condition(mov -> mov.getMinSearchDistance() > 0),
				condition(mov -> {
					mov.toCloseEnemy = mov.grid.getEnemyInSearchArea(
							mov.getAttackPosition(), mov, (short) 0, mov.getMinSearchDistance(), !mov.defending);
					return mov.toCloseEnemy != null;
				})
		);
	}

	protected static Node<MountainKingMovable> findEnemy() {
		return condition(mov -> {
			mov.enemy = mov.grid.getEnemyInSearchArea(mov.getAttackPosition(), mov, mov.getMinSearchDistance(), mov.getMaxSearchDistance(), !mov.defending);
			return mov.enemy != null;
		});
	}

	protected static Node<MountainKingMovable> attackEnemy() {
		return sequence(
				condition(MountainKingMovable::isEnemyValid),
				condition(MountainKingMovable::isEnemyAttackable),
				action(mov -> {mov.setDirection(EDirection.getApproxDirection(mov.position, mov.enemy.getPosition()));}),
				action(MountainKingMovable::startAttack),
				playAction(EMovableAction.ACTION1, MountainKingMovable::getAttackDuration),
				condition(MountainKingMovable::isEnemyValid),
				action(MountainKingMovable::hitEnemy)

		);
	}

	protected boolean isEnemyValid() {
		return enemy != null && enemy.isAlive();
	}

	@Override
	protected boolean isBusy() {
		return super.isBusy();
	}

	protected void startAttack() {

	}

	protected void abortGoTo() {
		currentTarget = null;
		goToTarget = null;
		patrolStep = -1;
		patrolPoints = null;
	}

	protected ShortPoint2D getAttackPosition() {
		return !defending && isBowman() ? inTowerAttackPosition : position;
	}

	private boolean isBowman() {
		return getMovableType().isBowman();
	}

	protected boolean isEnemyAttackable() {
		if(!isEnemyValid()) {
			return false;
		}

		int distance = position.getOnGridDistTo(enemy.getPosition());

		return distance <= getMaxAttackDistance();
	}

	@Override
	public void receiveHit(float hitStrength, ShortPoint2D attackerPos, IPlayer attackingPlayer) {
		super.receiveHit(hitStrength, attackerPos, attackingPlayer);
		enemyNearby = true;
	}

	@Override
	public void informAboutAttackable(IAttackable other) {
		enemyNearby = true;
	}

	@Override
	public void findWayAroundObstacle() {
		if (building == null && startPoint != null) {
			EDirection direction = EDirection.getDirection(position, path.getNextPos());

			EDirection rightDir = direction.getNeighbor(-1);
			ShortPoint2D rightPos = rightDir.getNextHexPoint(position);
			EDirection leftDir = direction.getNeighbor(1);
			ShortPoint2D leftPos = leftDir.getNextHexPoint(position);

			ShortPoint2D freePosition = getRandomFreePosition(rightPos, leftPos);

			if (freePosition != null) {
				path = new Path(freePosition);

			} else {
				EDirection twoRightDir = direction.getNeighbor(-2);
				ShortPoint2D twoRightPos = twoRightDir.getNextHexPoint(position);
				EDirection twoLeftDir = direction.getNeighbor(2);
				ShortPoint2D twoLeftPos = twoLeftDir.getNextHexPoint(position);

				freePosition = getRandomFreePosition(twoRightPos, twoLeftPos);

				if (freePosition != null) {
					path = new Path(freePosition);
				}
			}
		} else {
			super.findWayAroundObstacle();
		}
	}

	private ShortPoint2D getRandomFreePosition(ShortPoint2D pos1, ShortPoint2D pos2) {
		boolean pos1Free = grid.isFreePosition(pos1.x, pos1.y);
		boolean pos2Free = grid.isFreePosition(pos2.x, pos2.y);

		if (pos1Free && pos2Free) {
			return MatchConstants.random().nextBoolean() ? pos1 : pos2;
		} else if (pos1Free) {
			return pos1;
		} else if (pos2Free) {
			return pos2;
		} else {
			return null;
		}
	}

	@Override
	protected void decoupleMovable() {
		super.decoupleMovable();
	}

	protected float getCombatStrength() {
		boolean alliedGround = player.hasSameTeam(grid.getPlayerAt(position));

		float strengthMod = 1;
		if(alliedGround && hasEffect(EEffectType.DEFEATISM)) strengthMod *= EEffectType.DEFEATISM_DAMAGE_FACTOR;
		if(!alliedGround && hasEffect(EEffectType.INCREASED_MORALE)) strengthMod *= EEffectType.INCREASED_MORALE_DAMAGE_FACTOR;
		if(hasEffect(EEffectType.MOTIVATE_SWORDSMAN)) strengthMod *= EEffectType.MOTIVATE_SWORDSMAN_DAMAGE_FACTOR;

		return player.getCombatStrengthInformation().getCombatStrength(isOnOwnGround()) * strengthMod;
	}

	public ShortPoint2D getCurrentTarget() {
		return path != null ? path.getTargetPosition() : null;
	}

	private short getMaxAttackDistance() {
		if(defending) return Constants.TOWER_DEFEND_ATTACK_RADIUS;

		if(getMovableType().isPikeman() && !enemy.isTower()) return Constants.PIKEMAN_ATTACK_RADIUS;

		return Constants.DEFAULT_ATTACK_RADIUS;
	}

	protected short getAttackDuration() {
		short duration = (short)(INFANTRY_ATTACK_DURATION*1000);
		if(hasEffect(EEffectType.MOTIVATE_SWORDSMAN))  duration *= EEffectType.MOTIVATE_SWORDSMAN_ANIMATION_FACTOR;

		return duration;
	}

	protected short getMinSearchDistance() {
		return 0;
	}

	protected short getMaxSearchDistance() {
		if(defending) return getMaxAttackDistance();

		return Constants.SOLDIER_SEARCH_RADIUS;
	}

	protected void hitEnemy() {
		enemy.receiveHit(getMovableType().getStrength() * getCombatStrength(), position, player);
		// decrease the enemy's health
	}
}
