package jsettlers.logic.map.loading;

import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.position.RelativePoint;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.map.loading.data.IMutableMapData;
import jsettlers.logic.map.loading.data.objects.BuildingMapDataObject;
import jsettlers.logic.map.loading.data.objects.MapDataObject;
import jsettlers.logic.map.loading.data.objects.MovableObject;
import jsettlers.logic.map.loading.data.objects.StackMapDataObject;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.movable.EMovableType;
import jsettlers.logic.player.PlayerSetting;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * @author codingberlin
 */
public enum EMapStartResources {
	LOW_GOODS(1),
	MEDIUM_GOODS(2),
	HIGH_GOODS(3);

	public final int value;

	EMapStartResources(int value) {
		this.value = value;
	}

	public static EMapStartResources fromMapValue(int mapValue) {
		for (int i = 0; i < EMapStartResources.values().length; i++) {
			if (EMapStartResources.values()[i].value == mapValue)
				return EMapStartResources.values()[i];
		}

		System.err.println("wrong value for 'EOriginalMapStartResources' " + mapValue + "!");

		return EMapStartResources.HIGH_GOODS;
	}

	public static List<MapDataObject> generateStackObjects(EMapStartResources mapStartResources) {
		List<MapDataObject> goods = new Vector<>();
		switch (mapStartResources) {
		case LOW_GOODS:
			goods.add(new StackMapDataObject(EMaterialType.PLANK, 6));
			goods.add(new StackMapDataObject(EMaterialType.PLANK, 6));
			goods.add(new StackMapDataObject(EMaterialType.STONE, 6));
			goods.add(new StackMapDataObject(EMaterialType.STONE, 6));
			goods.add(new StackMapDataObject(EMaterialType.BLADE, 5));
			goods.add(new StackMapDataObject(EMaterialType.HAMMER, 6));
			goods.add(new StackMapDataObject(EMaterialType.AXE, 3));
			goods.add(new StackMapDataObject(EMaterialType.PICK, 2));
			goods.add(new StackMapDataObject(EMaterialType.SAW, 1));
			break;
		case MEDIUM_GOODS:
			goods.add(new StackMapDataObject(EMaterialType.PLANK, 7));
			goods.add(new StackMapDataObject(EMaterialType.PLANK, 6));
			goods.add(new StackMapDataObject(EMaterialType.PLANK, 6));
			goods.add(new StackMapDataObject(EMaterialType.PLANK, 6));
			goods.add(new StackMapDataObject(EMaterialType.STONE, 7));
			goods.add(new StackMapDataObject(EMaterialType.STONE, 6));
			goods.add(new StackMapDataObject(EMaterialType.STONE, 6));
			goods.add(new StackMapDataObject(EMaterialType.STONE, 6));
			goods.add(new StackMapDataObject(EMaterialType.COAL, 8));
			goods.add(new StackMapDataObject(EMaterialType.COAL, 7));
			goods.add(new StackMapDataObject(EMaterialType.IRONORE, 5));
			goods.add(new StackMapDataObject(EMaterialType.FISH, 3));
			goods.add(new StackMapDataObject(EMaterialType.BREAD, 7));
			goods.add(new StackMapDataObject(EMaterialType.MEAT, 5));
			goods.add(new StackMapDataObject(EMaterialType.BLADE, 5));
			goods.add(new StackMapDataObject(EMaterialType.BLADE, 5));
			goods.add(new StackMapDataObject(EMaterialType.HAMMER, 8));
			goods.add(new StackMapDataObject(EMaterialType.HAMMER, 7));
			goods.add(new StackMapDataObject(EMaterialType.AXE, 6));
			goods.add(new StackMapDataObject(EMaterialType.PICK, 4));
			goods.add(new StackMapDataObject(EMaterialType.SAW, 2));
			goods.add(new StackMapDataObject(EMaterialType.SCYTHE, 1));
			goods.add(new StackMapDataObject(EMaterialType.FISHINGROD, 1));
			break;
		default:
			goods.add(new StackMapDataObject(EMaterialType.PLANK, 8));
			goods.add(new StackMapDataObject(EMaterialType.PLANK, 8));
			goods.add(new StackMapDataObject(EMaterialType.PLANK, 7));
			goods.add(new StackMapDataObject(EMaterialType.PLANK, 7));
			goods.add(new StackMapDataObject(EMaterialType.STONE, 8));
			goods.add(new StackMapDataObject(EMaterialType.STONE, 8));
			goods.add(new StackMapDataObject(EMaterialType.STONE, 8));
			goods.add(new StackMapDataObject(EMaterialType.STONE, 7));
			goods.add(new StackMapDataObject(EMaterialType.COAL, 7));
			goods.add(new StackMapDataObject(EMaterialType.COAL, 7));
			goods.add(new StackMapDataObject(EMaterialType.COAL, 6));
			goods.add(new StackMapDataObject(EMaterialType.COAL, 6));
			goods.add(new StackMapDataObject(EMaterialType.IRONORE, 6));
			goods.add(new StackMapDataObject(EMaterialType.IRONORE, 6));
			goods.add(new StackMapDataObject(EMaterialType.FISH, 7));
			goods.add(new StackMapDataObject(EMaterialType.BREAD, 8));
			goods.add(new StackMapDataObject(EMaterialType.BREAD, 7));
			goods.add(new StackMapDataObject(EMaterialType.MEAT, 8));
			goods.add(new StackMapDataObject(EMaterialType.BLADE, 8));
			goods.add(new StackMapDataObject(EMaterialType.BLADE, 7));
			goods.add(new StackMapDataObject(EMaterialType.HAMMER, 7));
			goods.add(new StackMapDataObject(EMaterialType.HAMMER, 6));
			goods.add(new StackMapDataObject(EMaterialType.HAMMER, 6));
			goods.add(new StackMapDataObject(EMaterialType.HAMMER, 6));
			goods.add(new StackMapDataObject(EMaterialType.AXE, 8));
			goods.add(new StackMapDataObject(EMaterialType.PICK, 5));
			goods.add(new StackMapDataObject(EMaterialType.SAW, 3));
			goods.add(new StackMapDataObject(EMaterialType.SCYTHE, 3));
			goods.add(new StackMapDataObject(EMaterialType.FISHINGROD, 2));
			break;
		}
		return goods;
	}

	public static List<MapDataObject> generateMovableObjects(EMapStartResources mapStartResources, byte playerId) {
		List<MapDataObject> movables = new Vector<>();
		switch (mapStartResources) {
		case LOW_GOODS:
			for (byte i = 0; i < 2; i++)
				movables.add(new MovableObject(EMovableType.MINER, playerId));
			movables.add(new MovableObject(EMovableType.SMITH, playerId));
			movables.add(new MovableObject(EMovableType.BOWMAN_L1, playerId));
			movables.add(new MovableObject(EMovableType.PIKEMAN_L1, playerId));
			for (byte i = 0; i < 6; i++)
				movables.add(new MovableObject(EMovableType.SWORDSMAN_L1, playerId));
			for (byte i = 0; i < 16; i++)
				movables.add(new MovableObject(EMovableType.BEARER, playerId));
			break;
		case MEDIUM_GOODS:
			for (byte i = 0; i < 4; i++)
				movables.add(new MovableObject(EMovableType.MINER, playerId));
			for (byte i = 0; i < 2; i++)
				movables.add(new MovableObject(EMovableType.SMITH, playerId));
			for (byte i = 0; i < 2; i++)
				movables.add(new MovableObject(EMovableType.BOWMAN_L1, playerId));
			for (byte i = 0; i < 2; i++)
				movables.add(new MovableObject(EMovableType.PIKEMAN_L1, playerId));
			for (byte i = 0; i < 10; i++)
				movables.add(new MovableObject(EMovableType.SWORDSMAN_L1, playerId));
			for (byte i = 0; i < 32; i++)
				movables.add(new MovableObject(EMovableType.BEARER, playerId));
			for (byte i = 0; i < 4; i++)
				movables.add(new MovableObject(EMovableType.DONKEY, playerId));
			break;
		default:
			for (byte i = 0; i < 6; i++)
				movables.add(new MovableObject(EMovableType.MINER, playerId));
			for (byte i = 0; i < 3; i++)
				movables.add(new MovableObject(EMovableType.SMITH, playerId));
			for (byte i = 0; i < 4; i++)
				movables.add(new MovableObject(EMovableType.BOWMAN_L1, playerId));
			for (byte i = 0; i < 4; i++)
				movables.add(new MovableObject(EMovableType.PIKEMAN_L1, playerId));
			for (byte i = 0; i < 12; i++)
				movables.add(new MovableObject(EMovableType.SWORDSMAN_L1, playerId));
			for (byte i = 0; i < 50; i++)
				movables.add(new MovableObject(EMovableType.BEARER, playerId));
			for (byte i = 0; i < 6; i++)
				movables.add(new MovableObject(EMovableType.DONKEY, playerId));
			break;
		}
		return movables;
	}

	public void addStartTowerMaterialsAndSettlers(PlayerSetting[] playerSettings, IMutableMapData map) {
		// start resources were already placed during map creation
		if(map.hasStartBuildings()) return;

		for (byte playerId = 0; playerId < playerSettings.length; playerId++) {
			if (!playerSettings[playerId].isAvailable()) continue;

			ShortPoint2D startPoint = map.getStartPoint(playerId);

			// - add the start Tower for this player
			map.setMapObject(startPoint.x, startPoint.y, new BuildingMapDataObject(EBuildingType.TOWER, playerId));

			// - list of all objects that have to be added for this player
			List<MapDataObject> mapObjects = EMapStartResources.generateStackObjects(this);
			mapObjects.addAll(EMapStartResources.generateMovableObjects(this, playerId));

			// - blocking area of the tower
			ECivilisation playerCiv = playerSettings[playerId].getCivilisation();
			List<RelativePoint> towerTiles = Arrays.asList(EBuildingType.TOWER.getVariant(playerCiv).getProtectedTiles());

			RelativePoint relativeMapObjectPoint = new RelativePoint(-3, 3);

			for (MapDataObject currentMapObject : mapObjects) {
				do {
					// - get next point
					relativeMapObjectPoint = nextPointOnSpiral(relativeMapObjectPoint);

					// - don't put things under the tower
					if (towerTiles.contains(relativeMapObjectPoint)) {
						continue;
					}

					// - get absolute position
					int x = relativeMapObjectPoint.calculateX(startPoint.x);
					int y = relativeMapObjectPoint.calculateY(startPoint.y);

					// - is this place free?
					if (map.getMapObject(x, y) == null) {
						// - add Object
						map.setMapObject(x, y, currentMapObject);
						// - break DO: next object...
						break;
					}
				} while (true);
			}
		}
	}

	private RelativePoint nextPointOnSpiral(RelativePoint previousPoint) {
		short previousX = previousPoint.getDx();
		short previousY = previousPoint.getDy();

		short basis = (short) Math.max(Math.abs(previousX), Math.abs(previousY));

		if (previousX == basis && previousY > -basis) {
			return new RelativePoint(previousX, previousY - 1);
		} else if (previousX == -basis && previousY <= basis) {
			return new RelativePoint(previousX, previousY + 1);
		} else if (previousX < basis && previousY == basis) {
			return new RelativePoint(previousX + 1, previousY);
		} else if (previousX > -basis && previousY == -basis) {
			return new RelativePoint(previousX - 1, previousY);
		} else {
			return null;
		}
	}
}