package jamel;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Exports the data observed into an output file.
 */
public class Export extends JamelObject {

	/**
	 * The data to be observed.
	 */
	final private List<Expression> expressions = new LinkedList<>();

	/**
	 * Creates a new Export object.
	 * 
	 * @param elem
	 *            the description of the data to be observed.
	 * @param simulation
	 *            the parent simulation.
	 */
	public Export(final Element elem, final Simulation simulation) {
		super(simulation);
		final String[] keys = elem.getTextContent().split(";");
		for (final String key : keys) {
			if (!key.trim().isEmpty()) {
				this.expressions.add(this.getSimulation().getExpression(key.trim()));
			}
		}
	}

	/**
	 * Exports the data observed into the output file.
	 */
	public void run() {
		for (final Expression expression : this.expressions) {
			Jamel.println(expression.toString(), expression.getValue() + ";");
		}
	}

}
