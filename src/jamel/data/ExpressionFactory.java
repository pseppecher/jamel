package jamel.data;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.jfree.data.xy.VectorDataItem;

import jamel.Jamel;
import jamel.util.ArgChecks;
import jamel.util.JamelObject;
import jamel.util.Sector;
import jamel.util.Simulation;

/**
 * The expression factory.
 */
public class ExpressionFactory extends JamelObject {

	/**
	 * Returns a query cleaned from useless parentheses and spaces.
	 * 
	 * @param query
	 *            the query to be cleaned up.
	 * @return the cleaned up string.
	 */
	private static String cleanUp(final String query) {
		final String result;
		if (query.startsWith("+")) {
			final String str2 = query.substring(1, query.length());
			result = cleanUp(str2);
		} else if (query.charAt(0) == '(' && query.charAt(query.length() - 1) == ')') {
			int count = 1;
			for (int i = 1; i < query.length() - 1; i++) {
				if (query.charAt(i) == '(') {
					count++;
				} else if (query.charAt(i) == ')') {
					count--;
					if (count == 0) {
						break;
					}
				}
			}
			if (count == 1) {
				// Removes the global parentheses.
				final String str2 = query.substring(1, query.length() - 1);
				result = cleanUp(str2);
			} else {
				// Nothing to remove.
				result = query;
			}
		} else {
			// Nothing to remove.
			result = query;
		}
		return result.trim();
	}

	/**
	 * Returns the specified addition.
	 * 
	 * @param arg1
	 *            the augend.
	 * @param arg2
	 *            the addend.
	 * @return the specified addition.
	 */
	private static Expression getAddition(final Expression arg1, final Expression arg2) {
		ArgChecks.nullNotPermitted(arg1, "arg1");
		ArgChecks.nullNotPermitted(arg2, "arg2");
		final Expression result = new Expression() {

			@Override
			public Double getValue() {

				final Double v1 = arg1.getValue();
				final Double v2 = arg2.getValue();
				return (v1 == null || v2 == null) ? null : v1 + v2;

			}

			@Override
			public String toString() {
				return "(" + arg1.toString() + " + " + arg2.toString() + ")";
			}

		};
		return result;
	}

	/**
	 * Returns the specified division.
	 * 
	 * @param arg1
	 *            the dividend.
	 * @param arg2
	 *            the divisor.
	 * @return the specified division.
	 */
	private static Expression getDivision(final Expression arg1, final Expression arg2) {
		ArgChecks.nullNotPermitted(arg1, "arg1");
		ArgChecks.nullNotPermitted(arg2, "arg2");
		final Expression result = new Expression() {

			@Override
			public Double getValue() {

				final Double v1 = arg1.getValue();
				final Double v2 = arg2.getValue();
				return (v1 == null || v2 == null || v2 == 0) ? null : v1 / v2;

			}

			@Override
			public String toString() {
				return arg1.toString() + " / " + arg2.toString();
			}

		};
		return result;
	}

	/**
	 * Returns the modulo.
	 * 
	 * @param arg1
	 *            the dividend.
	 * @param arg2
	 *            the divisor.
	 * @return the specified modulo operation.
	 */
	private static Expression getModulo(final Expression arg1, final Expression arg2) {
		ArgChecks.nullNotPermitted(arg1, "arg1");
		ArgChecks.nullNotPermitted(arg2, "arg2");
		final Expression result = new Expression() {

			@Override
			public Double getValue() {

				final Double v1 = arg1.getValue();
				final Double v2 = arg2.getValue();
				return (v1 == null || v2 == null || v2 == 0) ? null : v1 % v2;

			}

			@Override
			public String toString() {
				return arg1.toString() + " % " + arg2.toString();
			}

		};
		return result;
	}

	/**
	 * Returns the specified multiplication.
	 * 
	 * @param arg1
	 *            the first factor.
	 * @param arg2
	 *            the second factor.
	 * @return the specified multiplication.
	 */
	private static Expression getMultiplication(final Expression arg1, final Expression arg2) {
		ArgChecks.nullNotPermitted(arg1, "arg1");
		ArgChecks.nullNotPermitted(arg2, "arg2");
		final Expression result = new Expression() {

			@Override
			public Double getValue() {

				final Double v1 = arg1.getValue();
				final Double v2 = arg2.getValue();
				return (v1 == null || v2 == null) ? null : v1 * v2;

			}

			@Override
			public String toString() {
				return arg1.toString() + " * " + arg2.toString();
			}

		};
		return result;
	}

	/**
	 * Returns a new "null" expression.
	 * 
	 * @return a new "null" expression.
	 */
	private static Expression getNull() {
		return new Expression() {

			@Override
			public Double getValue() {
				return null;
			}

			@Override
			public String toString() {
				return "null";
			}

		};
	}

	/**
	 * Returns an expression that represents the specified numeric constant.
	 * 
	 * @param d
	 *            the numeric constant.
	 * @return an expression that represents the specified numeric constant.
	 */
	private static Expression getNumeric(final double d) {

		final Expression result = new Expression() {

			@Override
			public Double getValue() {
				return d;
			}

			@Override
			public String toString() {
				return "" + d;
			}

		};
		return result;
	}

	/**
	 * Returns the opposite of the specified expression.
	 * 
	 * @param arg
	 *            the specified expression.
	 * @return the opposite of the specified expression.
	 */
	private static Expression getOpposite(final Expression arg) {
		ArgChecks.nullNotPermitted(arg, "arg");
		final Expression result = new Expression() {

			@Override
			public Double getValue() {

				final Double v = arg.getValue();
				return (v == null) ? null : -v;

			}

			@Override
			public String toString() {
				return "- " + arg.toString();
			}

		};
		return result;
	}

	/**
	 * Returns the specified subtraction.
	 * 
	 * @param arg1
	 *            the minuend.
	 * @param arg2
	 *            the subtrahend.
	 * @return the specified subtraction.
	 */
	private static Expression getSubtraction(final Expression arg1, final Expression arg2) {
		ArgChecks.nullNotPermitted(arg1, "arg1");
		ArgChecks.nullNotPermitted(arg2, "arg2");
		final Expression result = new Expression() {

			@Override
			public Double getValue() {

				final Double v1 = arg1.getValue();
				final Double v2 = arg2.getValue();
				return (v1 == null || v2 == null) ? null : v1 - v2;

			}

			@Override
			public String toString() {
				return "(" + arg1.toString() + " - " + arg2.toString() + ")";
			}

		};
		return result;
	}

	/**
	 * Compares the value of the first expression against the value of the
	 * second expression. The result is 1 if and only if the arguments are not
	 * null and the double values are the same.
	 * 
	 * @param arg1
	 *            the first expression
	 * @param arg2
	 *            the second expression
	 * @return <code>1</code> if the values of the expressions are the same;
	 *         <code>0</code> otherwise.
	 */
	private static Expression getTestEqual(Expression arg1, Expression arg2) {
		ArgChecks.nullNotPermitted(arg1, "arg1");
		ArgChecks.nullNotPermitted(arg2, "arg2");
		final Expression result = new Expression() {

			@Override
			public Double getValue() {

				final Double v1 = arg1.getValue();
				final Double v2 = arg2.getValue();
				return (v1 == null || v2 == null) ? null : (v1.equals(v2)) ? 1. : 0.;

			}

			@Override
			public String toString() {
				return "isEqual(" + arg1.toString() + ", " + arg2.toString() + ")";
			}

		};
		return result;
	}

	/**
	 * Returns <code>true</code> if parentheses in the specified query are
	 * balanced, <code>false</code> otherwise.
	 * 
	 * @param query
	 *            the query.
	 * @return <code>true</code> if parentheses in the specified query are
	 *         balanced, <code>false</code> otherwise.
	 */
	private static boolean isBalanced(String query) {
		int count = 0;
		for (int i = 0; i < query.length(); i++) {
			if (query.charAt(i) == '(') {
				count++;
			} else if (query.charAt(i) == ')') {
				count--;
				if (count < 0) {
					// Not balanced !
					return false;
				}
			}
		}
		return count == 0;
	}

	/**
	 * Splits this string in two substrings around the first comma.
	 * Commas within parentheses are ignored.
	 *
	 * @param input
	 *            the string to be split.
	 * @return the array of strings computed by splitting the given string
	 */
	private static String[] split2(final String input) {
		final String[] result;
		Integer position = null;
		int count = 0;
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c == '(') {
				count++;
			} else if (c == ')') {
				count--;
				if (count < 0) {
					throw new RuntimeException("Parentheses not balanced: " + input);
				}
			} else if (count == 0) {
				// We are outside parentheses.
				// Is this char an comma ?
				if (c == ',') {
					position = i;
					break;
				}
			}
		}
		if (count != 0) {
			throw new RuntimeException("Parentheses not balanced: " + input);
		}
		if (position != null) {
			result = new String[2];
			result[0] = input.substring(0, position);
			result[1] = input.substring(position + 1);
		} else {
			result = new String[1];
			result[0] = input;
		}
		return result;
	}

	/**
	 * Returns a new constant expression.
	 * 
	 * @param arg1
	 *            the number value to be returned by the expression.
	 * @return a new constant expression.
	 */
	public static Expression getConstant(Number arg1) {
		ArgChecks.nullNotPermitted(arg1, "arg1");
		final Expression result;
		if (arg1 == null) {
			result = getNull();
		} else {
			result = getNumeric(arg1.doubleValue());
		}
		return result;
	}

	/**
	 * Splits the given input String around commas.
	 * Commas within parenthesis are ignored.
	 *
	 * @param input
	 *            the string to be split
	 * @return the array of strings computed by splitting the given string
	 */
	public static String[] split(final String input) {
		final ArrayList<String> list = new ArrayList<>();
		String string = input;
		while (true) {
			final String[] output = split2(string);
			list.add(output[0]);
			if (output.length == 1) {
				break;
			}
			string = output[1];
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Creates a new Expression factory for the specified simulation.
	 * 
	 * @param simulation
	 *            the parent simulation.
	 */
	public ExpressionFactory(Simulation simulation) {
		super(simulation);
	}

	/**
	 * Returns the specified expression.
	 * 
	 * @param query
	 *            a string that describes the expression to be returned.
	 * @return the specified expression.
	 */
	public Expression getExpression(final String query) {

		try {

			final Expression result;

			if (!isBalanced(query)) {
				throw new RuntimeException("Not balanced: " + query);
				// Comment traiter cet incident ?
				// Il n'est pas dû à Jamel mais au scénario, il faut informer
				// clairement l'utilisateur de l'endroit où il s'est planté.
			}

			final String key = cleanUp(query.replaceAll("(\\p{javaSpaceChar}|\\r|\\n|\\t)", ""));

			Character operator = null;
			Integer position = null;
			int count = 0;

			for (int i = 0; i < key.length(); i++) {

				final char c = key.charAt(i);

				if (c == '(') {
					count++;
				} else if (c == ')') {
					count--;
				}

				else if (count == 0 && i > 0) {

					// We are outside parentheses.
					// Is this char an operator ?

					if (c == '+') {
						operator = c;
						position = i;
						break;
					} else if (c == '-') {
						final char previous = key.charAt(i - 1);
						if (previous != '*' && previous != '/') {
							operator = c;
							position = i;
							break;
						}
					} else if (c == '*' || c == '/' || c == '%') {
						operator = c;
						position = i;
					}
				}
			}

			if (position != null) {
				if (operator == null) {
					throw new RuntimeException("Operator is null");
				}
				final Expression arg1 = getExpression(key.substring(0, position));
				final Expression arg2 = getExpression(key.substring(position + 1));

				switch (operator) {
				case '+':
					result = getAddition(arg1, arg2);
					break;
				case '-':
					result = getSubtraction(arg1, arg2);
					break;
				case '*':
					result = getMultiplication(arg1, arg2);
					break;
				case '/':
					result = getDivision(arg1, arg2);
					break;
				case '%':
					result = getModulo(arg1, arg2);
					break;
				default:
					throw new RuntimeException("Unexpected operator: " + operator);
				}

			}

			else if (key.startsWith("-")) {
				result = getOpposite(getExpression(key.substring(1)));
			}

			else if (Pattern.matches("\\d.*", key)) {
				result = getNumeric(Double.parseDouble(key));
			}

			else if (Pattern.matches("isEqual[\\(].*[\\)]", key)) {
				final String argString = key.substring(8, key.length() - 1);
				final String[] args = split(argString);
				if (args.length != 2) {
					throw new RuntimeException("Bad number of parameters: " + key);
				}
				result = getTestEqual(getExpression(args[0]), getExpression(args[1]));
			}

			else if (Pattern.matches("val[\\(].*[\\)]", key)) {
				final String argString = key.substring(4, key.length() - 1);
				final String[] split = argString.split(",", 2);
				final Sector sector = this.getSimulation().getSector(split[0].split("\\.")[0]);
				if (sector == null) {
					throw new RuntimeException("Sector not found: " + split[0]);
				}
				final String[] args = split[1].split(",");
				if (split[0].split("\\.").length == 2) {
					// on demande une valeur sur un agent particulier.
					result = sector.getIndividualDataAccess(split[0].split("\\.")[1], args);
				} else {
					// on demande une opération d'agrégation sur l'ensemble
					// des agents (somme des données par exemple)
					result = sector.getDataAccess(args);
				}
			}

			else if (Pattern.matches(".*[\\.].*", key)) {
				// Récupération d'un paramètre
				final String[] split = key.split("\\.", 2);
				final Sector sector = this.getSimulation().getSector(split[0]);
				if (sector == null) {
					throw new RuntimeException("Sector not found: \"" + split[0] + "\" in string: \"" + key + "\"");
				}
				result = ExpressionFactory.getConstant(sector.getParameters().getDoubleValue(split[1]));
			}

			else if (key.equals("t")) {
				result = this.getSimulation().getTime();
			}

			else if (key.equals("speed")) {
				result = this.getSimulation().getSpeed();
			}

			else if (key.equals("totalMemory")) {
				result = this.getSimulation().getTotalMemory();
			}

			else if (key.equals("freeMemory")) {
				result = this.getSimulation().getFreeMemory();
			}

			else if (key.equals("duration")) {
				result = this.getSimulation().getDuration();
			}

			else {
				throw new RuntimeException("Not yet implemented: \'" + key + "\'");
			}
			return result;
		} catch (Exception e) {
			Jamel.println("Bad query: '" + query + "'");
			throw new RuntimeException("Bad query: " + query, e);
		}
	}

	/**
	 * Creates and returns a new vector series.
	 * 
	 * @param x
	 *            the definition of the x values.
	 * @param y
	 *            the definition of the y values.
	 * @param deltaX
	 *            the definition of the x vector values.
	 * @param deltaY
	 *            the definition of the y vector values.
	 * @param conditions
	 *            some conditions.
	 * @return a new vector series.
	 */
	public DynamicSeries getVectorSeries(String x, String y, String deltaX, String deltaY, String conditions) {
		final VectorDynamicXYSeries newSeries;

		final Expression xExp = this.getExpression(x);
		final Expression yExp = this.getExpression(y);
		final Expression deltaXExp = this.getExpression(deltaX);
		final Expression deltaYExp = this.getExpression(deltaY);
		final Expression[] conditionsExp = parseConditions(conditions);

		String seriesKey = xExp.toString() + ", " + yExp.toString() + ", " + deltaXExp.toString() + ", "
				+ deltaYExp.toString();

		newSeries = new VectorDynamicXYSeries(seriesKey) {

			@Override
			public void update(boolean refereshCharts) {
				boolean update = true;
				for (int i = 0; i < conditionsExp.length; i++) {
					if (conditionsExp[i].getValue() == null || conditionsExp[i].getValue() != 1) {
						update = false;
						break;
					}
				}
				if (update) {
					try {
						final Double xValue = xExp.getValue();
						final Double yValue = yExp.getValue();
						if (xValue != null && yValue!=null) {
							final Double xDeltaValue = deltaXExp.getValue();
							final Double yDeltaValue = deltaYExp.getValue();
							final VectorDataItem item = new VectorDataItem(xValue, yValue, xDeltaValue, yDeltaValue);
							this.add(item, false);
						}
					} catch (Exception e) {
						throw new RuntimeException(
								"Something went wrong while updating the series: " + this.getDescription(), e);
					}
				}
				if (refereshCharts) {
					this.setNotify(true);
					this.setNotify(false);
				}
			}

		};

		return newSeries;
	}

	/**
	 * Returns the specified series.
	 * 
	 * @param x
	 *            the definition of x data.
	 * @param y
	 *            the definition of y data.
	 * @param conditions
	 *            the conditions.
	 * @return the specified series.
	 */
	public StandardDynamicXYSeries getXYSeries(String x, String y, String conditions) {
		StandardDynamicXYSeries newSeries = null;
		try {
			final Expression xExp = this.getExpression(x);
			final Expression yExp = this.getExpression(y);
			if (conditions == null) {
				newSeries = new StandardDynamicXYSeries(xExp, yExp);
			} else {
				newSeries = new StandardDynamicXYSeries(xExp, yExp, this.parseConditions(conditions));
			}
		} catch (final Exception e) {
			final String message = "Something went wrong with the series: " + x + ", " + y + ", " + conditions;
			Jamel.println(message);
			throw new RuntimeException(message, e);
		}
		return newSeries;
	}

	/**
	 * Parses the given string and returns an array of expressions.
	 * 
	 * @param conditions
	 *            a string that contains the description of the conditions.
	 * @return an array of expressions.
	 */
	public Expression[] parseConditions(String conditions) {
		final String[] strings;
		final Expression[] result;
		if (conditions == null || conditions.isEmpty()) {
			result = new Expression[0];
		} else {
			strings = ExpressionFactory.split(conditions);
			result = new Expression[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = this.getExpression(strings[i]);
			}
		}
		return result;
	}

}
