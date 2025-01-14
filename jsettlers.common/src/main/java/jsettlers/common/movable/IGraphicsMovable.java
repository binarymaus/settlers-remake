/*******************************************************************************
 * Copyright (c) 2015 - 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.common.movable;


import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.position.ILocatable;
import jsettlers.common.selectable.ISelectable;
import jsettlers.common.sound.ISoundable;

/**
 * Defines a Movable actor that can be drawn by jsettlers.graphics
 *
 * @author Andreas Eberle
 */
public interface IGraphicsMovable extends ISelectable, ILocatable, ISoundable, IIDable {

	int getUnitGroup();
	void setUnitGroup(int unitGroup);
    EMovableType getMovableType();

	/**
	 * Get the current action that the movable is doing.
	 *
	 * @return The action
	 * @see #getMoveProgress()
	 */
	EMovableAction getAction();

	EDirection getDirection();

	/**
	 * In general this method returns the progress of doing the action specified by {@link #getAction()}
	 * <p/>
	 * for example:<br>
	 * if the movable is walking: this returns the progress of getting from one grid point to the other.<br>
	 *
	 * @return The value is in the range of [0,1)
	 */
	float getMoveProgress();

	/**
	 * This method returns the material the IMovable is currently carrying.
	 * <p/>
	 * If the movable is just dropping the material ({@link #getAction()} == EAction.DROP) this method has to return the old EMaterialType until {@link #getAction()} changes to another EAction again
	 * (dropping is done)<br>
	 * If the movable is currently taking something, this method already returns the EMaterialType, that the movable want's to take.
	 *
	 * @return The material carried by this movable.
	 */
	EMaterialType getMaterial();

	/**
	 * Used to get health of a movable.
	 *
	 * @return health of a movable
	 */
	float getHealth();

	/**
	 * Used to check if the movable is still alive
	 *
	 * @return true if the movable is still alive, false otherwise.
	 */
	boolean isAlive();

	/**
	 * Returns alternating true and false on every step.
	 *
	 * @return True if the current step is a right step, false if it is a left step.
	 */
	boolean isRightstep();

	boolean hasEffect(EEffectType effect);
}
