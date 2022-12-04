package jsettlers.mapcreator.data.symmetry;

import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.utils.Tuple;

import java.util.stream.Stream;

public class PointSymmetry extends SymmetryConfig {

	@Override
	public Stream<Tuple<ShortPoint2D, ShortPoint2D>> transform(int width, int height, ShortPoint2D start, ShortPoint2D end, ShortPoint2D symPoint) {
		ShortPoint2D mirrorPoint = new ShortPoint2D(width/2, height/2);

		int dX = end.x - start.x;
		int dY = end.y - start.y;

		int mirrorX = start.x - mirrorPoint.x;
		int mirrorY = start.y - mirrorPoint.y;


		return Stream.of(1, -1)
				.map(t -> {
					ShortPoint2D base = new ShortPoint2D(mirrorPoint.x + t*mirrorX, mirrorPoint.y + t*mirrorY);
					ShortPoint2D newEnd = new ShortPoint2D(base.x + t*dX, base.y + t*dY);
					return new Tuple<>(base, newEnd);
				});
	}
}
