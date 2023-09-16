package jsettlers.mapcreator.main.window.newmap;

import jsettlers.logic.map.loading.newmap.MapFileHeader;
import jsettlers.mapcreator.data.MapData;

public interface InitialMapProvider {
	MapData getMapData(MapFileHeader header);
}
