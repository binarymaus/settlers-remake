package go.graphics;

public class BackgroundDrawHandle extends GLResourceIndex {

	public final BufferHandle vertices;
	public TextureHandle texture;

	public BackgroundDrawHandle(GLDrawContext dc, int id, TextureHandle texture, BufferHandle vertices) {
		super(dc, id);
		this.vertices = vertices;
		this.texture = texture;
	}

	public int[] regions;
	public int regionCount;

	public int getVertexArrayId() {
		return id;
	}
}
