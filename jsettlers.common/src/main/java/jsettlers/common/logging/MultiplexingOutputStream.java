package jsettlers.common.logging;

import java.io.IOException;
import java.io.OutputStream;

public class MultiplexingOutputStream extends OutputStream {
	private final OutputStream[] outputStreams;

	public MultiplexingOutputStream(OutputStream... outputStreams) {
		this.outputStreams = outputStreams;
	}

	@Override
	public void write(int b) throws IOException {
		for(OutputStream out : outputStreams) {
			out.write(b);
		}
	}
}
