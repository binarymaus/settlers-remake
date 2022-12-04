package jsettlers.common.map;

import java.util.BitSet;

import jsettlers.common.mapobject.IMapObject;
import jsettlers.common.movable.IGraphicsMovable;

public interface IDirectGridProvider {
	IMapObject[] getObjectArray();
	IGraphicsMovable[] getMovableArray();
	BitSet getBorderArray();
	byte[][] getVisibleStatusArray();
	byte[][] getHeightArray();
	boolean isFoWEnabled();
}
