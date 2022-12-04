package jsettlers.mapcreator.data.symmetry;

public class DefaultSymmetries {
	public static final SymmetryConfig DEFAULT = new IdentitySymmetry();
	public static final SymmetryConfig REPEAT4 = new PlainRepeatSymmetry(2, 2);
	public static final SymmetryConfig POINT = new PointSymmetry();
}
