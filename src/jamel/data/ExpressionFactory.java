package jamel.data;

import java.security.InvalidParameterException;
import java.util.regex.Pattern;

import jamel.util.JamelObject;
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
		/*if (query.contains(" ")) {
			final String str2 = query.replace(" ", "");
			result = cleanUp(str2);
		} else*/ if (query.startsWith("+")) {
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
		if (arg1 == null || arg2 == null) {
			throw new InvalidParameterException("Null");
		}
		final Expression result = new Expression() {

			@Override
			public Double getValue() {

				final Double value;
				if (arg1.getValue() == null || arg2.getValue() == null) {
					value = null;
				} else {
					value = arg1.getValue() + arg2.getValue();
				}
				return value;

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
		if (arg1 == null || arg2 == null) {
			throw new InvalidParameterException("Null");
		}
		final Expression result = new Expression() {

			@Override
			public Double getValue() {
				final Double value;
				if (arg1.getValue() == null || arg2.getValue() == null || arg2.getValue() == 0) {
					value = null;
				} else {
					value = arg1.getValue() / arg2.getValue();
				}
				return value;
			}

			@Override
			public String toString() {
				return arg1.toString() + " / " + arg2.toString();
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
		if (arg1 == null || arg2 == null) {
			throw new IllegalArgumentException("Null.");
		}
		final Expression result = new Expression() {

			@Override
			public Double getValue() {
				final Double value;
				if (arg1.getValue() == null || arg2.getValue() == null) {
					value = null;
				} else {
					value = arg1.getValue() * arg2.getValue();
				}
				return value;
			}

			@Override
			public String toString() {
				return arg1.toString() + " * " + arg2.toString();
			}

		};
		return result;
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
	 * @param arg1
	 *            the specified expression.
	 * @return the opposite of the specified expression.
	 */
	private static Expression getOpposite(final Expression arg1) {
		if (arg1 == null) {
			throw new InvalidParameterException("Null");
		}
		final Expression result = new Expression() {

			@Override
			public Double getValue() {
				return -arg1.getValue();
			}

			@Override
			public String toString() {
				return "- " + arg1.toString();
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
		if (arg1 == null || arg2 == null) {
			throw new InvalidParameterException("Null");
		}
		final Expression result = new Expression() {

			@Override
			public Double getValue() {

				final Double value;
				if (arg1.getValue() == null || arg2.getValue() == null) {
					value = null;
				} else {
					value = arg1.getValue() - arg2.getValue();
				}
				return value;

			}

			@Override
			public String toString() {
				return "(" + arg1.toString() + " - " + arg2.toString() + ")";
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

		// Jamel.println("getExpression", "\'" + query + "\'");

		final Expression result;

		if (!isBalanced(query)) {
			throw new RuntimeException("Not balanced: " + query);
			// TODO Comment traiter cet incident ?
			// Il n'est pas dû à Jamel mais au scénario, il faut informer
			// clairement l'utilisateur de l'endroit où il s'est planté.
		}

		final String cleaned = cleanUp(query.replaceAll("(\\p{javaSpaceChar}|\\r|\\n)", ""));

		Character operator = null;
		Integer position = null;
		int count = 0;

		for (int i = 0; i < cleaned.length(); i++) {

			final char c = cleaned.charAt(i);

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
					final char previous = cleaned.charAt(i - 1);
					if (previous != '*' && previous != '/') {
						operator = c;
						position = i;
						break;
					}
				} else if (c == '*' || c == '/') {
					operator = c;
					position = i;
				}
			}
		}

		if (position != null) {
			if (operator == null) {
				throw new RuntimeException("Operator is null");
			}
			final Expression arg1 = getExpression(cleaned.substring(0, position));
			final Expression arg2 = getExpression(cleaned.substring(position + 1));

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
			default:
				throw new RuntimeException("Unexpected operator: " + operator);
			}

		} else {
			if (cleaned.startsWith("-")) {
				result = getOpposite(getExpression(cleaned.substring(1)));
			} else if (Pattern.matches("\\d.*", cleaned)) {
				result = getNumeric(Double.parseDouble(cleaned));
			} else {
				result = this.getSimulation().getDataAccess(cleaned);
			}
		}

		return result;

	}

}
