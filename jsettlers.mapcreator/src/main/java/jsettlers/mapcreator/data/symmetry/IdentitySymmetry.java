package jsettlers.mapcreator.data.symmetry;

import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.utils.Tuple;

import java.util.stream.Stream;

public class IdentitySymmetry extends SymmetryConfig {
	@Override
	public Stream<Tuple<ShortPoint2D, ShortPoint2D>> transform(int width, int height, ShortPoint2D start, ShortPoint2D end, ShortPoint2D symPoint) {
		return Stream.of(new Tuple<>(start, end));
	}
}
