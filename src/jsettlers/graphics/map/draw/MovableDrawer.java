package jsettlers.graphics.map.draw;

import go.graphics.Color;
import jsettlers.common.movable.IMovable;
import jsettlers.common.position.ISPosition2D;
import jsettlers.graphics.image.Image;
import jsettlers.graphics.map.MapDrawContext;
import jsettlers.graphics.map.draw.settlerimages.SettlerImageMap;
import jsettlers.graphics.sequence.Sequence;

/**
 * This is the movable drawer that draws movable objects (settlers).
 * <p>
 * It uses the settler image map to get the right image. TODO: cleanup.
 * 
 * @author michael
 */
public class MovableDrawer {

	private SettlerImageMap imageMap = SettlerImageMap.getInstance();

	/**
	 * Draws a movable
	 * 
	 * @param context
	 *            The context to draw at.
	 * @param movable
	 *            The movable.
	 */
	public void draw(MapDrawContext context, IMovable movable) {
		Image image = this.imageMap.getImageForSettler(movable);
		drawImage(context, movable, image);
	}

	private void drawImage(MapDrawContext context, IMovable movable, Image image) {
		ISPosition2D pos = movable.getPos();
		byte fogstatus = context.getVisibleStatus(pos.getX(), pos.getY());
		if (fogstatus == 0) {
			return; // break
		}
		
		Color color = context.getPlayerColor(movable.getPlayer());
		float shade = MapObjectDrawer.getColor(fogstatus);
		
		// draw settler
		image.draw(context.getGl(), color, shade);

		if (movable.isSelected()) {
			context.getGl().glTranslatef(0, 0, 0.2f);
			drawSelectionMark(context, movable.getHealth());
		}
	}

	private static void drawSelectionMark(MapDrawContext context, float health) {
		Image image =
		        ImageProvider.getInstance().getSettlerSequence(4, 7)
		                .getImageSafe(0);
		image.drawAt(context.getGl(), 0, 20);

		Sequence<? extends Image> sequence =
		        ImageProvider.getInstance().getSettlerSequence(4, 6);
		int healthId =
		        Math.min((int) ((1 - health) * sequence.length()), sequence
		                .length() - 1);
		Image healthImage = sequence.getImageSafe(healthId);
		healthImage.drawAt(context.getGl(), 0, 38);
	}
}
