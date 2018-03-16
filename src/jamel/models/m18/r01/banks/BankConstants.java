package jamel.models.m18.r01.banks;

import jamel.util.Parameters;

/**
 * A class to parse and store the constant parameters of the bank.
 */
class BankConstants {

	/**
	 * The capital target ratio.
	 */
	final float capitalTargetRatio;

	/**
	 * The penalty premium.
	 */
	final float penaltyPremium;

	/**
	 * The supervision period.
	 */
	final float supervision;

	/**
	 * The reaction coefficient of the taylor rule.
	 */
	final float taylorCoef;

	/**
	 * The inflation target.
	 */
	final float taylorTarget;

	/**
	 * Creates a new set of parameters by parsing the specified
	 * {@code Parameters}.
	 * 
	 * @param params
	 *            the parameters to be parsed.
	 */
	BankConstants(Parameters params) {
		this.supervision = params.getInt("supervision");
		this.capitalTargetRatio = params.getFloat("capitalTargetRatio");
		this.penaltyPremium = params.getFloat("penaltyPremium");
		this.taylorCoef = params.getFloat("taylor.coef");
		this.taylorTarget = params.getFloat("taylor.target");
	}
	
}