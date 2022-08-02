package jsettlers.logic.map.loading.data;

import jsettlers.logic.map.loading.data.objects.MapDataObject;

public interface IMutableMapData extends IMapData {
	void setMapObject(int x, int y, MapDataObject obj);
}
