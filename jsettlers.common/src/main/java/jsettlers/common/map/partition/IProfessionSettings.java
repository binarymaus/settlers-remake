package jsettlers.common.map.partition;

import jsettlers.common.movable.EMovableType;

public interface IProfessionSettings {

	ISingleProfessionLimit getSettings(EMovableType movableType);
}
