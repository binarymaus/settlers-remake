package jsettlers.logic.movable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jsettlers.algorithms.fogofwar.FogOfWar;
import jsettlers.algorithms.simplebehaviortree.Root;
import jsettlers.common.movable.EMovableType;
import jsettlers.logic.SerializationUtils;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.movable.interfaces.ILogicMovable;
import jsettlers.logic.timer.RescheduleTimer;

public final class MovableManager {

	static final HashMap<Integer, ILogicMovable> movablesByID = new HashMap<>();
	static final ConcurrentLinkedQueue<ILogicMovable> allMovables  = new ConcurrentLinkedQueue<>();
	static       int                                  nextID       = Integer.MIN_VALUE;
	static byte fowTeam = -1;

	private static final Map<EMovableType, Root<Movable>> BEHAVIOUR_TREES = new EnumMap<>(EMovableType.class);

	public static void initFow(byte fow) {
		fowTeam = fow;
		for(ILogicMovable lm : allMovables) {
			if(lm instanceof Movable) {
				Movable mv = (Movable) lm;
				if(MatchConstants.ENABLE_ALL_PLAYER_FOG_OF_WAR || lm.getPlayer().getTeamId() == fowTeam) {
					FogOfWar.instance.refThread.nextTasks.offer(mv);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void readStaticState(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		nextID = ois.readInt();
		allMovables.clear();
		fowTeam = -1;
		allMovables.addAll((Collection<? extends ILogicMovable>) ois.readObject());
		movablesByID.putAll(SerializationUtils.readHashMap(ois));
	}

	public static void writeStaticState(ObjectOutputStream oos) throws IOException {
		oos.writeInt(nextID);
		oos.writeObject(allMovables);
		SerializationUtils.writeHashMap(oos, movablesByID);
	}

	/**
	 * Used for networking to identify movables over the network.
	 *
	 * @param id
	 * 		id to be looked for
	 * @return returns the movable with the given ID<br>
	 * or null if the id can not be found
	 */
	public static ILogicMovable getMovableByID(int id) {
		return movablesByID.get(id);
	}

	public static Queue<ILogicMovable> getAllMovables() {
		return allMovables;
	}

	public static void resetState() {
		allMovables.clear();
		movablesByID.clear();
		nextID = Integer.MIN_VALUE;
		fowTeam = -1;
	}

	static int requestId(Movable movable, Movable replace) {
		int id;

		if(replace != null) {
			id = replace.getID();
		} else {
			id = nextID++;
		}
		return id;
	}

	static void add(Movable movable) {

		movablesByID.put(movable.getID(), movable);
		allMovables.offer(movable);


		if((fowTeam != -1 && MatchConstants.ENABLE_ALL_PLAYER_FOG_OF_WAR) || fowTeam == movable.player.getTeamId()) {
			FogOfWar.instance.refThread.nextTasks.offer(movable);
		}

		RescheduleTimer.add(movable, Constants.MOVABLE_INTERRUPT_PERIOD);
	}

	static void remove(Movable movable) {
		movablesByID.remove(movable.getID());
		allMovables.remove(movable);
	}

	public static <T extends Movable> void registerBehaviour(EMovableType type, Root<T> tree) {
		if(BEHAVIOUR_TREES.containsKey(type)) {
			throw new Error(type + " already has a behaviour tree!");
		}

		BEHAVIOUR_TREES.put(type, (Root<Movable>)tree);
	}

	static Root<Movable> getBehaviourFor(EMovableType type) {
		Root<Movable> behaviour = BEHAVIOUR_TREES.get(type);

		if(behaviour == null) {
			throw new Error(type + " is missing a behaviour tree!");
		}

		return behaviour;
	}
}
