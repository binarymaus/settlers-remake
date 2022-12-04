package jsettlers.logic.objects;

import jsettlers.common.mapobject.EMapObjectType;
import jsettlers.common.mapobject.IMannaBowlObject;
import jsettlers.common.player.ECivilisation;
import jsettlers.logic.buildings.stack.IStackSizeSupplier;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.map.grid.objects.AbstractHexMapObject;

public class MannaBowlMapObject extends AbstractHexMapObject implements IMannaBowlObject {
	private static final long serialVersionUID = -174985264395107962L;

	private final IStackSizeSupplier mannaStack;
	private final ECivilisation civilisation;

	public MannaBowlMapObject(IStackSizeSupplier mannaStack, ECivilisation civilisation) {
		this.mannaStack = mannaStack;
		this.civilisation = civilisation;
	}

	@Override
	public ECivilisation getCivilisation() {
		return civilisation;
	}

	@Override
	public EMapObjectType getObjectType() {
		return EMapObjectType.MANNA_BOWL;
	}

	@Override
	public float getStateProgress() {
		return ((float) mannaStack.getStackSize()) / Constants.STACK_SIZE;
	}

	@Override
	public boolean cutOff() {
		return false;
	}

	@Override
	public boolean canBeCut() {
		return false;
	}

}
