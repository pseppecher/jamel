package jamel.basic.data;

/**
 * An utility method for creating some standard (mathematical or statistical) expressions.
 */
public class ExpressionFactory {

	/**
	 * Returns a string cleaned from useless parentheses and spaces.
	 * @param dirty the string to be cleaned up.
	 * @return the cleaned up string.
	 */
	private static String cleanUp(final String dirty) {
		final String result;
		if (dirty.contains(" ")) {
			final String str2 = dirty.replace(" ", "");			
			result=cleanUp(str2);
		}
		else if (dirty.startsWith("+")) {
			final String str2=dirty.substring(1, dirty.length());
			result=cleanUp(str2);
		}
		else if (dirty.charAt(0)=='(' && dirty.charAt(dirty.length()-1)==')') {
			int count = 1;
			for (int i=1; i<dirty.length()-1; i++) {
				if (dirty.charAt(i)=='(') {
					count++;
				}
				else if (dirty.charAt(i)==')') {
					count--;
					if (count==0) {
						break;
					}
				}
			}
			if (count==1){
				// Removes the global parentheses.
				final String str2=dirty.substring(1, dirty.length()-1);
				result = cleanUp(str2);
			}
			else {
				// Nothing to remove.
				result=dirty;
			}
		}
		else {
			// Nothing to remove.
			result=dirty;
		}
		return result;
	}
	
	/**
	 * Returns a constant expression.
	 * @param constant the literal description of the constant.
	 * @return a constant expression.
	 */
	private static Expression getConst(final String constant) {
		final Expression result;
		final Double value = Double.valueOf(constant);
		result = new Expression() {

			@Override
			public String toString() {
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
	 * Returns <code>true</code> if parentheses in the specified query are balanced, <code>false</code> otherwise.
	 * @param query the query.
	 * @return <code>true</code> if parentheses in the specified query are balanced, <code>false</code> otherwise.
	 */
	private static boolean isBalanced(String query) {
		int count=0;
		for (int i=0; i<query.length(); i++) {
			if (query.charAt(i)=='(') {
				count++;
			}
			else if (query.charAt(i)==')') {
				count--;
				if (count<0) {
					// Not balanced !
					return false;
				}
			}			
		}
		return count==0;
	}

	/**
	 * Creates and returns the specified expression.
	 * @param query the literal description of the expression to be created.
	 * @param macroDatabase the macro dataset.
	 * @return an expression.
	 */
	public static Expression newExpression(String query, MacroDatabase macroDatabase) {
		final ExpressionFactory expressionFactory = new ExpressionFactory(query, macroDatabase);
		return expressionFactory.newExpression();
	}

	/** The literal description of the expression to be created. */
	private final String literal;

	/** The macroeconomic dataset. */
	private final MacroDatabase macroDatabase;

	/**
	 * Creates a new {@link ExpressionFactory}.
	 * @param query the literal description of the expression to be created.
	 * @param macroDatabase the macro dataset.
	 */
	private ExpressionFactory(final String query, final MacroDatabase macroDatabase) {
		this.literal = query;
		this.macroDatabase = macroDatabase;
	}

	/**
	 * Returns an expression representing the specified addition.
	 * @param addition the literal description of the addition to be created.
	 * @param lim the position of the <code>+<code> character in the literal description.
	 * @return an expression representing the specified addition.
	 */
	private Expression getAddition(final String addition, final int lim) {
		final Expression result;
		final String a=addition.substring(0, lim);
		final String b=addition.substring(lim+1,addition.length());
		final Expression term1 = ExpressionFactory.newExpression(a, macroDatabase);
		final Expression term2 = ExpressionFactory.newExpression(b, macroDatabase);
		result = new Expression() {

			@Override
			public String toString() {
				return addition;
			}

			@Override
			public Double value() {
				final Double result;
				if (term1.value()==null || term2.value()==null) {
					result=null;
				}
				else {
					result = term1.value() + term2.value(); 
				}
				return result;
			}


		};
		return result;
	}

	/**
	 * Returns an expression representing the specified division.
	 * @param division the literal description of the division to be created.
	 * @param lim the position of the <code>:<code> character in the literal description.
	 * @return an expression representing the specified division.
	 */
	private Expression getDivision(final String division, final int lim) {
		final Expression result;
		final String a=division.substring(0, lim);
		final String b=division.substring(lim+1,division.length());
		final Expression dividend = ExpressionFactory.newExpression(a, macroDatabase);
		final Expression divisor = ExpressionFactory.newExpression(b, macroDatabase);
		result = new Expression() {

			@Override
			public String toString() {
				return division;
			}

			@Override
			public Double value() {
				final Double result;
				if (dividend.value()==null || divisor.value()==null) {
					result=null;
				}
				else {
					result = dividend.value()/divisor.value(); 
				}
				return result;
			}


		};
		return result;
	}
	/**
	 * Returns an expression representing the specified multiplication.
	 * @param multiplication the literal description of the multiplication to be created.
	 * @param lim the position of the <code>*<code> character in the literal description.
	 * @return an expression representing the specified multiplication.
	 */
	private Expression getMultiplication(final String multiplication, final int lim) {
		final Expression result;
		final String a=multiplication.substring(0, lim);
		final String b=multiplication.substring(lim+1,multiplication.length());
		final Expression exp1 = ExpressionFactory.newExpression(a, macroDatabase);
		final Expression exp2 = ExpressionFactory.newExpression(b, macroDatabase);
		result = new Expression() {

			@Override
			public String toString() {
				return multiplication;
			}

			@Override
			public Double value() {
				final Double result;
				if (exp1.value()==null || exp2.value()==null) {
					result=null;
				}
				else {
					result = exp1.value()*exp2.value(); 
				}
				return result;
			}


		};
		return result;
	}

	/**
	 * Returns the specified negative expression.
	 * @param negative the literal description of the negative expression to be created.
	 * @return the specified negative expression.
	 */
	private Expression getNegative(final String negative) {
		final Expression result;
		final String a=negative.substring(1,negative.length());
		final Expression positive = ExpressionFactory.newExpression(a, macroDatabase);
		result = new Expression() {

			@Override
			public String toString() {
				return negative;
			}

			@Override
			public Double value() {
				final Double result;
				if (positive.value()==null) {
					result=null;
				}
				else {
					result = -positive.value(); 
				}
				return result;
			}

		};
		return result;
	}

	/**
	 * Returns an expression representing the specified subtraction.
	 * @param subtraction the literal description of the subtraction to be created.
	 * @param lim the position of the <code>-<code> character in the literal description.
	 * @return an expression representing the specified subtraction.
	 */
	private Expression getSubtraction(final String subtraction, final int lim) {
		final Expression result;
		final String a=subtraction.substring(0, lim);
		final String b=subtraction.substring(lim+1,subtraction.length());
		final Expression term1 = ExpressionFactory.newExpression(a, macroDatabase);
		final Expression term2 = ExpressionFactory.newExpression(b, macroDatabase);
		result = new Expression() {

			@Override
			public String toString() {
				return subtraction;
			}

			@Override
			public Double value() {
				final Double result;
				if (term1.value()==null || term2.value()==null) {
					result=null;
				}
				else {
					result = term1.value() - term2.value(); 
				}
				return result;
			}


		};
		return result;
	}
	/**
	 * Parses the literal description of the expression, creates and returns the expression.
	 * @return the expression specified by the literal description.
	 */
	private Expression newExpression() {
		if (!isBalanced(literal)) {
			throw new IllegalArgumentException("Parentheses are not balanced: "+literal);
		}

		final Expression result;

		final String cleaned = cleanUp(literal);

		int count=0;
		int plus=-1;
		int minus=-1;
		int times=-1;
		int obelus=-1;

		for (int i=0; i<cleaned.length(); i++) {

			final char c = cleaned.charAt(i); 

			if (c=='(') {
				count++;
			}
			else if (c==')') {
				count--;
			}

			else {

				// Is this char an operator ?

				if (count==0) {
					// We are outside parentheses.
					if (c=='+') {
						plus=i;
						break;
					}
					else if (c=='-' && i!=0) {
						final char previous=cleaned.charAt(i-1);
						if (previous!='*' && previous!=':') {
							minus=i;
						}
					}
					else if (c=='*') {
						times=i;
					}
					else if (c==':') {
						obelus=i;
					}
				}
			}
		}

		if (plus!=-1) {
			result = getAddition(cleaned,plus);
		}
		else if (minus!=-1) {
			result = getSubtraction(cleaned,minus);
		}
		else if (times!=-1) {
			result=getMultiplication(cleaned,times);
		}
		else if (obelus!=-1) {
			result = getDivision(cleaned,obelus);
		}
		else if (cleaned.startsWith("-")) {
			result=getNegative(cleaned);
		}
		else if (cleaned.contains("(") && cleaned.endsWith(")")) {
			result=macroDatabase.getFunction(cleaned);
		}
		else {
			result=getConst(cleaned);
		}
		return result;
	}

}

// ***
