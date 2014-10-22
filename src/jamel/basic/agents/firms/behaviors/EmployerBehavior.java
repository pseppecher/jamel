package jamel.basic.agents.firms.behaviors;

import jamel.util.Circuit;

/**
 * Encapsulates the employer behavior of the firm.
 */
public class EmployerBehavior {

	/** The wage. */
	private double wage;

	/**
	 * Creates a new behavior.
	 * @param wageInitialValue the initial value of the wage.
	 */
	public EmployerBehavior(long wageInitialValue) {
		this.wage = wageInitialValue;
	}

	/**
	 * Returns the wage.
	 * @return the wage.
	 */
	public double getWage() {
		return this.wage;
	}

	/**
	 * Updates the wage.
	 * @param vacancyRate the current job vacancy rate.
	 * @param normalVacancyRate the normal job vacancy rate.
	 * @param wageFlexDown the downward wage flexibility.
	 * @param wageFlexUp the upward wage flexibility.
	 * @param minimumWage the minimum wage.
	 */
	public void update(double vacancyRate, float normalVacancyRate, float wageFlexDown, float wageFlexUp, long minimumWage) {
		final float alpha1 = Circuit.getRandom().nextFloat();
		final float alpha2 = Circuit.getRandom().nextFloat();
		final double vacancyRatio = vacancyRate/normalVacancyRate;
		final double newWage;
		if (vacancyRatio<1-alpha1*alpha2) {
			newWage=this.wage*(1f-alpha1*wageFlexDown);
		}
		else if (vacancyRatio>1+alpha1*alpha2) {
			newWage=this.wage*( 1f+alpha1*wageFlexUp);
		}
		else {
			newWage=this.wage;
		}
		this.wage = Math.max(newWage, minimumWage);
	}

}

//***
