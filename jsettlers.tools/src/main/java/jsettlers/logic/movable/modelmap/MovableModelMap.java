package jsettlers.logic.movable.modelmap;

import java.util.HashMap;
import java.util.Map;
import jsettlers.common.CommonConstants;
import jsettlers.common.landscape.ELandscapeType;
import jsettlers.common.map.EDebugColorModes;
import jsettlers.common.map.IGraphicsBackgroundListener;
import jsettlers.common.map.IGraphicsGrid;
import jsettlers.common.map.partition.IPartitionData;
import jsettlers.common.mapobject.IMapObject;
import jsettlers.common.movable.IGraphicsMovable;
import jsettlers.common.player.IPlayer;

public class MovableModelMap implements IGraphicsGrid {

	private final short width;
	private final short height;

	private final Map<Integer, IGraphicsMovable> movables = new HashMap<>();

	public MovableModelMap(short width, short height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public short getWidth() {
		return width;
	}

	@Override
	public short getHeight() {
		return height;
	}

	@Override
	public IGraphicsMovable getMovableAt(int x, int y) {
		return movables.get(x+y*width);
	}

	public void setMovableAt(int x, int y, IGraphicsMovable movable) {
		movables.put(x+y*width, movable);
	}

	@Override
	public IMapObject getVisibleMapObjectsAt(int x, int y) {
		return null;
	}

	@Override
	public byte getVisibleHeightAt(int x, int y) {
		return 0;
	}

	@Override
	public ELandscapeType getVisibleLandscapeTypeAt(int x, int y) {
		return ELandscapeType.GRASS;
	}

	@Override
	public int getDebugColorAt(int x, int y, EDebugColorModes debugColorMode) {
		return 0;
	}

	@Override
	public boolean isBorder(int x, int y) {
		return false;
	}

	@Override
	public IPlayer getPlayerAt(int x, int y) {
		return null;
	}

	@Override
	public byte getVisibleStatus(int x, int y) {
		return CommonConstants.FOG_OF_WAR_VISIBLE;
	}

	@Override
	public void setBackgroundListener(IGraphicsBackgroundListener backgroundListener) {

	}

	@Override
	public IPartitionData getPartitionData(int x, int y) {
		return null;
	}

	@Override
	public boolean isBuilding(int x, int y) {
		return false;
	}
}
