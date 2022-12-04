package jsettlers.common.buildings.loader;

import jsettlers.common.material.EMaterialType;
import jsettlers.common.movable.EDirection;
import org.xml.sax.Attributes;

public class MineElementWrapper {

	private final EDirection dropDirection;
	private final EMaterialType[] foodOrder;
	private final float miningInterval;

	private static final String ATTR_DROP_DIRECTION = "dropDirection";
	private static final String ATTR_FOOD_ORDER = "foodOrder";
	private static final String ATTR_MINING_INTERVAL = "miningInterval";

	public MineElementWrapper() {
		dropDirection = EDirection.SOUTH_WEST;
		foodOrder = new EMaterialType[0];
		miningInterval = 1;
	}

	public MineElementWrapper(Attributes attributes) {
		dropDirection = EDirection.valueOf(attributes.getValue(ATTR_DROP_DIRECTION));
		foodOrder = getMaterialTypeArray(attributes, ATTR_FOOD_ORDER);
		miningInterval = getAttributeAsFloat(attributes, ATTR_MINING_INTERVAL);
	}

	public static EMaterialType[] getMaterialTypeArray(Attributes attributes, String attribute) {
		String foodOrderString = attributes.getValue(attribute);
		if (foodOrderString == null) {
			return null;
		}

		try {
			String[] foodOrderStrings = foodOrderString.split(",");
			EMaterialType[] foodOrder = new EMaterialType[foodOrderStrings.length];
			for (int i = 0; i < foodOrderStrings.length; i++) {
				foodOrder[i] = EMaterialType.valueOf(foodOrderStrings[i]);
			}
			return foodOrder;
		} catch (IllegalArgumentException e) {
			throw new IllegalAccessError("Food order may only contain EMaterialTypes: " + foodOrderString);
		}
	}

	public static float getAttributeAsFloat(Attributes attributes, String attribute) {
		String string = attributes.getValue(attribute);
		if (string == null) {
			return 0f;
		} else {
			try {
				return Float.parseFloat(string);
			} catch (NumberFormatException e) {
				return 0f;
			}
		}
	}

	public EDirection getDropDirection() {
		return dropDirection;
	}

	public EMaterialType[] getFoodOrder() {
		return foodOrder;
	}

	public float getMiningInterval() {
		return miningInterval;
	}
}
