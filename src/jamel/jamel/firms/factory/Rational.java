package jamel.jamel.firms.factory;

import jamel.Jamel;

/**
 * Represents one rational number.
 * <p>
 * From <code>RationalNumber.java</code> by Lewis/Loftus.
 */
public class Rational {

	/**
	 * Computes and returns the greatest common divisor of the two positive
	 * parameters. Uses Euclid's algorithm.
	 * 
	 * @param val1
	 *            the first parameter.
	 * @param val2
	 *            the second parameter.
	 * @return the greatest common divisor of the two positive parameters
	 */
	private static int gcd(final int val1, final int val2) {
		int int1=val1;
		int int2=val2;
		while (int1 != int2) {
			if (int1 > int2)
				int1 = int1 - int2;
			else
				int2 = int2 - int1;
		}
		return int1;
	}

	/**
	 * Reduces a fraction by dividing both the numerator and the denominator by
	 * their greatest common divisor.
	 * 
	 * @param numerator
	 *            the numerator of the fraction to be reduced.
	 * @param denominator
	 *            the denominator of the fraction to be reduced.
	 * @return an array of int, the first value representing the numerator, the
	 *         second value representing the denominator.
	 */
	private static int[] reduce(final int numerator, final int denominator) {
		final int[] result = new int[2];
		if (numerator != 0) {
			int common = gcd(Math.abs(numerator), denominator);
			result[0] = numerator / common;
			result[1] = denominator / common;
		} else {
			result[0] = numerator;
			result[1] = denominator;
		}
		return result;
	}

	/**
	 * For debugging purpose.
	 * 
	 * @param args
	 *            not used.
	 */
	public static void main(String[] args) {
		Rational q1 = new Rational(1, 1);
		Rational q2 = new Rational(2, 2);
		Jamel.println(q1 + "=" + q2);
		Rational q3 = new Rational(1, 2);
		Rational q4 = new Rational(2, 5);
		Jamel.println(q4 + "=" + q4.doubleValue());
		Jamel.println(q3.add(q4).toString());
		Jamel.println(q3.multiply(q4).toString());
		Jamel.println(q3.divide(q4).toString());
		Jamel.println(q3.subtract(q4).toString());
		Jamel.println(""+new Rational(4, 2).equals(3));
	}

	/** The denominator */
	private final int denominator;

	/** The numerator */
	private final int numerator;

	/**
	 * Creates a new <code>Rational</code> with an integer value.
	 * 
	 * @param i
	 *            the integer value.
	 */
	public Rational(int i) {
		this.numerator = i;
		this.denominator = 1;
	}

	/**
	 * Creates a new rational number.
	 * 
	 * @param numerator1
	 *            the numerator.
	 * @param denominator1
	 *            the denominator.
	 */
	public Rational(int numerator1, int denominator1) {
		if (denominator1 == 0) {
			throw new IllegalArgumentException(
					"The denominator cannot be zero.");
		}
		final int numerator2;
		final int denominator2;
		
		// Make the numerator "store" the sign
		if (denominator1 < 0) {
			numerator2 = numerator1 * -1;
			denominator2 = denominator1 * -1;
		}
		else {
			numerator2 = numerator1;
			denominator2 = denominator1;			
		}
		final int[] reduced = reduce(numerator2, denominator2);
		this.numerator = reduced[0];
		this.denominator = reduced[1];
	}

	/**
	 * Returns a <code>Rational</code> whose value is
	 * <code>(this + augend)</code>.
	 * 
	 * @param augend
	 *            value to be added to this <code>Rational</code>.
	 * @return <code>this + augend</code>
	 */
	public Rational add(Rational augend) {
		int commonDenominator = denominator * augend.getDenominator();
		int numerator1 = numerator * augend.getDenominator();
		int numerator2 = augend.getNumerator() * denominator;
		int sum = numerator1 + numerator2;
		return new Rational(sum, commonDenominator);
	}

	/**
	 * Returns a <code>Rational</code> whose value is
	 * <code>(this / divisor)</code>.
	 * 
	 * @param divisor
	 *            value by which this <code>Rational</code> is to be divided.
	 * @return <code>this/divisor</code>
	 */
	public Rational divide(Rational divisor) {
		return multiply(divisor.reciprocal());
	}

	/**
	 * Converts this <code>Rational</code> to a <code>double</code>.
	 * 
	 * @return this <code>Rational</code> converted to a double.
	 */
	public double doubleValue() {
		return ((double) numerator) / denominator;
	}

	/**
	 * Compares this <code>Rational</code> with the specified <code>int</code>
	 * for equality.
	 * 
	 * @param i
	 *            <code>int</code> to which this <code>Rational</code> is to be
	 *            compared.
	 * @return <code>true</code> if <code>(this==i)</code>.
	 */
	public boolean equals(int i) {
		return (this.numerator == i && this.denominator == 1);
	}

	/**
	 * Compares this <code>Rational</code> with the specified
	 * <code>Object</code> for equality.
	 * 
	 * @param x
	 *            <code>Object</code> to which this <code>Rational</code> is to
	 *            be compared.
	 * @return <code>true</code> if and only if the specified
	 *         <code>Object</code> is a <code>Rational</code> whose value is
	 *         equal to this <code>Rational</code>'s.
	 */
	@Override
	public boolean equals(Object x) {
		final boolean result;
		if (!(x instanceof Rational)) {
			result = false;
		} else {
			Rational q = (Rational) x;
			result = (numerator == q.getNumerator() && denominator == q
					.getDenominator());
		}
		return result;
	}

	/**
	 * Returns the denominator of this rational number.
	 * 
	 * @return the denominator of this rational number.
	 */
	public int getDenominator() {
		return denominator;
	}

	/**
	 * Returns the numerator of this rational number.
	 * 
	 * @return the numerator of this rational number.
	 */
	public int getNumerator() {
		return numerator;
	}

	@Override
	public int hashCode() {
		return numerator + 11 * denominator;
	}

	/**
	 * Returns a <code>Rational</code> whose value is
	 * <code>(this x multiplicand)</code>.
	 * 
	 * @param multiplicand
	 *            value to be multiplied by this <code>rational</code>.
	 * @return <code>this * multiplicand</code>.
	 */
	public Rational multiply(Rational multiplicand) {
		int numer = numerator * multiplicand.getNumerator();
		int denom = denominator * multiplicand.getDenominator();
		return new Rational(numer, denom);
	}

	/**
	 * Returns a <code>Rational</code> whose value is <code>(-this)</code>.
	 * 
	 * @return <code>(-this)</code>
	 */
	public Rational negate() {
		return new Rational(-this.numerator, this.numerator);
	}

	/**
	 * Returns the reciprocal of this <code>Rational</code>.
	 * 
	 * @return the reciprocal.
	 */
	public Rational reciprocal() {
		return new Rational(denominator, numerator);
	}

	/**
	 * Returns a <code>Rational</code> whose value is
	 * <code>(this - subtrahend)</code>.
	 * 
	 * @param subtrahend
	 *            value to be added to this <code>Rational</code>.
	 * @return <code>this - subtrahend</code>
	 */
	public Rational subtract(Rational subtrahend) {
		int commonDenominator = this.denominator * subtrahend.getDenominator();
		int numerator1 = this.numerator * subtrahend.getDenominator();
		int numerator2 = subtrahend.getNumerator() * this.denominator;
		int difference = numerator1 - numerator2;
		return new Rational(difference, commonDenominator);
	}

	@Override
	public String toString() {
		final String result;
		if (numerator == 0) {
			result = "0";
		} else {
			if (denominator == 1) {
				result = numerator + "";
			} else {
				result = numerator + "/" + denominator;
			}
		}
		return result;
	}

}

// ***
