package jamel;

import java.util.List;

/**
 * A manager for sectorial data.
 */
public class DataManager {

	/**
	 * The list of the agents.
	 */
	final private List<Agent> agents;

	/**
	 * The parent sector.
	 */
	final private Sector sector;

	/**
	 * Creates a new data manager.
	 * 
	 * @param agents
	 *            the list of the agents.
	 * @param sector
	 *            the parent sector.
	 */
	public DataManager(List<Agent> agents, Sector sector) {
		this.agents = agents;
		this.sector = sector;
	}

	/**
	 * Returns the expression for the sum of all specified values for the
	 * specified period.
	 * 
	 * @param dataKey
	 *            the key of the values.
	 * @param period
	 *            the period.
	 * @return the expression for the sum.
	 */
	private Expression getSum(final String dataKey, final String period) {
		final Expression result = new Expression() {

			@Override
			public Double getValue() {
				Double value = 0d;
				for (final Agent agent : DataManager.this.agents) {
					value += agent.getData(dataKey, period);
				}
				return value;
			}

			@Override
			public String toString() {
				return sector.getName() + "(" + dataKey + "," + period + ",sum)";
			}

		};
		return result;
	}

	/**
	 * Returns an expression that provides an access to the specified data.
	 * 
	 * @param args
	 *            the arguments specifying the data to be accessed through the
	 *            expression.
	 * @return an expression.
	 */
	public Expression getDataAccess(final String[] args) {
		final Expression result;
		final String dataKey = args[0];
		final String period = args[1];
		final String operation = args[2];
		switch (operation) {
		case "sum":
			result = getSum(dataKey, period);
			break;
		default:
			throw new RuntimeException("Not yet implemented: " + operation);
		}
		return result;
	}

}
