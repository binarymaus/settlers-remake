package jsettlers.mapcreator.data.symmetry;

import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.utils.Tuple;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PlainRepeatSymmetry extends SymmetryConfig {

	private final int chunkXCount;
	private final int chunkYCount;

	public PlainRepeatSymmetry(int chunkXCount, int chunkYCount) {
		this.chunkXCount = chunkXCount;
		this.chunkYCount = chunkYCount;
	}

	@Override
	public Stream<Tuple<ShortPoint2D, ShortPoint2D>> transform(int width, int height, ShortPoint2D start, ShortPoint2D end, ShortPoint2D symPoint) {

		int chunkX = width/chunkXCount;
		int chunkY = height/chunkYCount;

		int baseX = start.x % chunkX;
		int baseY = start.y % chunkY;

		int dx = end.x - start.x;
		int dy = end.y - start.y;

		return IntStream.range(0, chunkXCount)
				.mapToObj(x -> IntStream.range(0, chunkYCount)
						.mapToObj(y -> {
							int pX = chunkX*x + baseX;
							int pY = chunkY*y + baseY;
							return new Tuple<>(new ShortPoint2D(pX, pY),
									new ShortPoint2D(pX+dx, pY+dy));
						})).flatMap(s -> s);
	}
}
