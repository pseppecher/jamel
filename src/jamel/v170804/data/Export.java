package jamel.v170804.data;

import java.util.LinkedList;
import java.util.List;

import jamel.Jamel;
import jamel.util.Expression;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Simulation;

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
	 * @param param
	 *            the description of the data to be observed.
	 * @param simulation
	 *            the parent simulation.
	 */
	public Export(final Parameters param, final Simulation simulation) {
		super(simulation);
		final String[] keys = param.splitTextContent(";");
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
		// TODO implement me !
		for (final Expression expression : this.expressions) {
			Jamel.println(expression.toString(), expression.getValue() + ";");
		}
	}

}
