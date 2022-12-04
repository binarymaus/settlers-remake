package jsettlers.graphics.map.draw.settlerimages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MovableParser {

	private final Map<String, MovableTemplate> templates = new HashMap<>();

	private final OpenResourceFunction openResource;

	public MovableParser(OpenResourceFunction openResource) {
		this.openResource = openResource;
	}

	public interface OpenResourceFunction {
		InputStream openResource(String name) throws IOException;
	}

	private void invokeTemplate(String line, Consumer<String> lineCons) {
		int argBegin = line.indexOf('(');
		String name = line.substring(0, argBegin);
		MovableTemplate template = templates.get(name);

		if (template == null) {
			System.err.println("template " + name + " is undefined!");
			return;
		} else if (template == lineCons) {
			System.err.println("template " + name + " tried to invoke itself!");
			return;
		}

		template.invoke(line.substring(argBegin), lineCons);
	}

	public void parseFile(String resource, Consumer<String> external) throws IOException {
		parseFile(openResource.openResource(resource), external);
	}

	private void parseFile(InputStream input, Consumer<String> external) throws IOException {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
			Consumer<String> lineConsumer = external;

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) continue;
				if (line.startsWith("#")) continue;

				if (line.startsWith("}")) {
					if (lineConsumer == external) {
						System.err.println("} is out of place!");
					}
					lineConsumer = external;
					continue;
				}

				if (line.startsWith("!")) {
					int argBegin = line.indexOf(' ');
					String name = line.substring(1, argBegin);

					MovableTemplate newTemplate = new MovableTemplate(line.substring(argBegin));
					lineConsumer = newTemplate;
					templates.put(name, newTemplate);
				} else if (line.contains("(")) {
					invokeTemplate(line, lineConsumer);
				} else {
					lineConsumer.accept(line);
				}
			}
		}
	}
}
