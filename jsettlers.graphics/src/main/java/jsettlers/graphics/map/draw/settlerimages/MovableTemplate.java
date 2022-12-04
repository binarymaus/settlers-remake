package jsettlers.graphics.map.draw.settlerimages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MovableTemplate implements Consumer<String> {

	private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\s*(-?\\d+)\\s*([+\\-*/%])\\s*(-?\\d+)\\s*");
	private final List<String> arguments = new ArrayList<>();
	private final List<String> lines = new ArrayList<>();

	MovableTemplate(String declaration) {
		int lastSpace = 0;
		while (declaration.charAt(lastSpace + 1) != '{') {
			int nextSpace = declaration.indexOf(' ', lastSpace + 1);
			arguments.add(declaration.substring(lastSpace + 1, nextSpace));
			lastSpace = nextSpace;
		}
	}


	@Override
	public void accept(String line) {
		lines.add(line);
	}

	private String applyArguments(HashMap<String, String> variables, String template) {
		String out = template;

		for (Map.Entry<String, String> variable : variables.entrySet()) {
			out = matchVariable("$" + variable.getKey())
					.matcher(out)
					.replaceAll(variable.getValue());
		}

		return out;
	}

	private Pattern matchVariable(String name) {
		return Pattern.compile(name, Pattern.LITERAL);
	}

	private void applyCalculation(String operation, HashMap<String, String> variables) {
		String[] parts = operation.split("=", 2);
		String target = parts[0].trim();

		variables.put(target, evaluate(parts[1]));
	}

	public void invoke(String line, Consumer<String> lineCons) {
		HashMap<String, String> variables = new HashMap<>(arguments.size());

		int lastComma = 0;
		int index = 0;
		while (line.charAt(lastComma) != ')') {
			int nextComma = line.indexOf(',', lastComma + 1);
			if (nextComma == -1) nextComma = line.indexOf(')');

			String arg = arguments.get(index);
			variables.put(arg, Matcher.quoteReplacement(line.substring(lastComma + 1, nextComma)));
			index++;

			lastComma = nextComma;
		}

		for (String templateLine : lines) {
			String apply = applyArguments(variables, templateLine);

			if (apply.startsWith("%")) {
				applyCalculation(apply.substring(1), variables);
			} else {
				lineCons.accept(apply);
			}
		}
	}

	private static String evaluate(String expression) {
		int first;
		int second;
		String operation;
		int out;

		Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
		if(!matcher.matches()) {
			System.err.println("Illegal expression: " + expression);
			return "";
		}

		try {
			first = Integer.parseInt(matcher.group(1));
			operation = matcher.group(2).trim();
			second = Integer.parseInt(matcher.group(3));

			switch (operation) {
				case "+":
					out = first + second;
					break;
				case "-":
					out = first - second;
					break;
				case "*":
					out = first * second;
					break;
				case "/":
					out = first / second;
					break;
				case "%":
					out = first % second;
					break;
				default:
					System.err.println("Illegal operation in \"" + expression + "\"");
					return "";
			}
			return Integer.toString(out);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		}
		return "";
	}
}
