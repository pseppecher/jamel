package jamel.basic.data;

import jamel.basic.util.InitializationException;

/**
 * An utility method for creating some standard (mathematical or statistical)
 * expressions.
 */
public class ExpressionFactory {

	/**
	 * Returns a string cleaned from useless parentheses and spaces.
	 * 
	 * @param dirty
	 *            the string to be cleaned up.
	 * @return the cleaned up string.
	 */
	private static String cleanUp(final String dirty) {
		final String result;
		if (dirty.contains(" ")) {
			final String str2 = dirty.replace(" ", "");
			result = cleanUp(str2);
		} else if (dirty.startsWith("+")) {
			final String str2 = dirty.substring(1, dirty.length());
			result = cleanUp(str2);
		} else if (dirty.charAt(0) == '('
				&& dirty.charAt(dirty.length() - 1) == ')') {
			int count = 1;
			for (int i = 1; i < dirty.length() - 1; i++) {
				if (dirty.charAt(i) == '(') {
					count++;
				} else if (dirty.charAt(i) == ')') {
					count--;
					if (count == 0) {
						break;
					}
				}
			}
			if (count == 1) {
				// Removes the global parentheses.
				final String str2 = dirty.substring(1, dirty.length() - 1);
				result = cleanUp(str2);
			} else {
				// Nothing to remove.
				result = dirty;
			}
		} else {
			// Nothing to remove.
			result = dirty;
		}
		return result;
	}

	/**
	 * Returns a constant expression.
	 * 
	 * @param constant
	 *            the literal description of the constant.
	 * @return a constant expression.
	 */
	private static Expression getConst(final String constant) {
		final Expression result;
		final Double value = Double.valueOf(constant);
		result = new Expression() {

			@Override
			public String getQuery() {
				return value.toString();
			}

			@Override
			public Double value() {
				return value;
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
	 * Formats the specified string for more readability.
	 * @param string the string to be formated.
	 * @return the formated string. 
	 */
	public static String format(final String string) {
		String result = string.replace(",", ", ");
		result = string.replace("=", " = ");
		result = string.replace("+", " + ");
		result = string.replace("*", " * ");
		result = string.replace(":", " : ");
		result = string.replace(" t-", " t#");
		result = string.replace("-", " - ");
		result = string.replace(" t#", " t-");
		result = string.replace("   ", " ");
		result = string.replace("  ", " ");
		return result;
	}

	/**
	 * Creates and returns the specified expression.
	 * 
	 * @param query
	 *            the literal description of the expression to be created.
	 * @param macroDatabase
	 *            the macro database.
	 * @return an expression.
	 * @throws InitializationException if something goes wrong.
	 */
	public static Expression newExpression(String query,
			MacroDatabase macroDatabase) throws InitializationException {
		final ExpressionFactory expressionFactory = new ExpressionFactory(
				query, macroDatabase);
		return expressionFactory.newExpression();
	}

	/**
	 * Returns an {@link Expression} the value of which is null.
	 * @param query the query.
	 * @return an {@link Expression} the value of which is null.
	 */
	public static Expression newNullExpression(final String query) {
		return new Expression(){

			@Override
			public String getQuery() {
				return query;
			}
			
			@Override
			public Double value() {
				return null;
			}};
			
	}

	/** The literal description of the expression to be created. */
	private final String literal;
	
	/** The macro-economic dataset. */
	private final MacroDatabase macroDatabase;

	/**
	 * Creates a new {@link ExpressionFactory}.
	 * 
	 * @param query
	 *            the literal description of the expression to be created.
	 * @param macroDatabase
	 *            the macro dataset.
	 */
	private ExpressionFactory(final String query,
			final MacroDatabase macroDatabase) {
		this.literal = query;
		this.macroDatabase = macroDatabase;
	}

	/**
	 * Returns an expression representing the specified addition.
	 * 
	 * @param query
	 *            the literal description of the addition to be created.
	 * @param lim
	 *            the position of the
	 *            <code>+</code> character in the literal description.
	 * @return an expression representing the specified addition.
	 * @throws InitializationException if something goes wrong.
	 */
	private Expression getAddition(final String query, final int lim) throws InitializationException {
		final Expression result;
		final String a = query.substring(0, lim);
		final String b = query.substring(lim + 1, query.length());
		final Expression term1 = ExpressionFactory.newExpression(a,
				macroDatabase);
		final Expression term2 = ExpressionFactory.newExpression(b,
				macroDatabase);
		result = new Expression() {
			
			final private String formated = format(query);

			@Override
			public String getQuery() {
				return formated;
			}

			@Override
			public Double value() {
				final Double value;
				if (term1.value() == null || term2.value() == null) {
					value = null;
				} else {
					value = term1.value() + term2.value();
				}
				return value;
			}

		};
		return result;
	}

	/**
	 * Returns an expression representing the specified division.
	 * 
	 * @param query
	 *            the literal description of the division to be created.
	 * @param lim
	 *            the position of the
	 *            <code>:</code> character in the literal description.
	 * @return an expression representing the specified division.
	 * @throws InitializationException if something goes wrong.
	 */
	private Expression getDivision(final String query, final int lim) throws InitializationException {
		final Expression result;
		final String a = query.substring(0, lim);
		final String b = query.substring(lim + 1, query.length());
		final Expression dividend = ExpressionFactory.newExpression(a,
				macroDatabase);
		final Expression divisor = ExpressionFactory.newExpression(b,
				macroDatabase);
		result = new Expression() {

			final private String formated = format(query);

			@Override
			public String getQuery() {
				return formated;
			}

			@Override
			public Double value() {
				final Double value;
				if (dividend.value() == null || divisor.value() == null) {
					value = null;
				} else {
					value = dividend.value() / divisor.value();
				}
				return value;
			}

		};
		return result;
	}

	/**
	 * Returns an expression representing the specified multiplication.
	 * 
	 * @param query
	 *            the literal description of the multiplication to be created.
	 * @param lim
	 *            the position of the
	 *            <code>*</code> character in the literal description.
	 * @return an expression representing the specified multiplication.
	 * @throws InitializationException if something goes wrong.
	 */
	private Expression getMultiplication(final String query,
			final int lim) throws InitializationException {
		final Expression result;
		final String a = query.substring(0, lim);
		final String b = query.substring(lim + 1,
				query.length());
		final Expression exp1 = ExpressionFactory.newExpression(a,
				macroDatabase);
		final Expression exp2 = ExpressionFactory.newExpression(b,
				macroDatabase);
		result = new Expression() {

			final private String formated = format(query);

			@Override
			public String getQuery() {
				return formated;
			}

			@Override
			public Double value() {
				final Double value;
				if (exp1.value() == null || exp2.value() == null) {
					value = null;
				} else {
					value = exp1.value() * exp2.value();
				}
				return value;
			}

		};
		return result;
	}

	/**
	 * Returns the specified negative expression.
	 * 
	 * @param query
	 *            the literal description of the negative expression to be
	 *            created.
	 * @return the specified negative expression.
	 * @throws InitializationException if something goes wrong.
	 */
	private Expression getNegative(final String query) throws InitializationException {
		final Expression result;
		final String a = query.substring(1, query.length());
		final Expression positive = ExpressionFactory.newExpression(a,
				macroDatabase);
		result = new Expression() {

			final private String formated = format(query);

			@Override
			public String getQuery() {
				return formated;
			}

			@Override
			public Double value() {
				final Double value;
				if (positive.value() == null) {
					value = null;
				} else {
					value = -positive.value();
				}
				return value;
			}

		};
		return result;
	}

	/**
	 * Returns an expression representing the specified subtraction.
	 * 
	 * @param query
	 *            the literal description of the subtraction to be created.
	 * @param lim
	 *            the position of the
	 *            <code>-</code> character in the literal description.
	 * @return an expression representing the specified subtraction.
	 * @throws InitializationException if something goes wrong.
	 */
	private Expression getSubtraction(final String query, final int lim) throws InitializationException {
		final Expression result;
		final String a = query.substring(0, lim);
		final String b = query.substring(lim + 1, query.length());
		final Expression term1 = ExpressionFactory.newExpression(a,
				macroDatabase);
		final Expression term2 = ExpressionFactory.newExpression(b,
				macroDatabase);
		result = new Expression() {

			final private String formated = format(query);

			@Override
			public String getQuery() {
				return formated;
			}

			@Override
			public Double value() {
				final Double value;
				if (term1.value() == null || term2.value() == null) {
					value = null;
				} else {
					value = term1.value() - term2.value();
				}
				return value;
			}

		};
		return result;
	}

	/**
	 * Parses the literal description of the expression, creates and returns the
	 * expression.
	 * 
	 * @return the expression specified by the literal description.
	 * @throws InitializationException if something goes wrong.
	 */
	private Expression newExpression() throws InitializationException {
		if (!isBalanced(literal)) {
			throw new IllegalArgumentException("Parentheses are not balanced: "
					+ literal);
		}

		final Expression result;

		final String cleaned = cleanUp(literal);

		int count = 0;
		int plus = -1;
		int minus = -1;
		int times = -1;
		int obelus = -1;

		for (int i = 0; i < cleaned.length(); i++) {

			final char c = cleaned.charAt(i);

			if (c == '(') {
				count++;
			} else if (c == ')') {
				count--;
			}

			else {

				// Is this char an operator ?

				if (count == 0) {
					// We are outside parentheses.
					if (c == '+') {
						plus = i;
						break;
					} else if (c == '-' && i != 0) {
						final char previous = cleaned.charAt(i - 1);
						if (previous != '*' && previous != ':') {
							minus = i;
						}
					} else if (c == '*') {
						times = i;
					} else if (c == ':') {
						obelus = i;
					}
				}
			}
		}

		if (plus != -1) {
			result = getAddition(cleaned, plus);
		} else if (minus != -1) {
			result = getSubtraction(cleaned, minus);
		} else if (times != -1) {
			result = getMultiplication(cleaned, times);
		} else if (obelus != -1) {
			result = getDivision(cleaned, obelus);
		} else if (cleaned.startsWith("-")) {
			result = getNegative(cleaned);
		} else if (cleaned.contains("(") && cleaned.endsWith(")")) {
			result = macroDatabase.getFunction(cleaned);
		} else {
			result = getConst(cleaned);
		}
		return result;
	}

}

// ***
