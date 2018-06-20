package jamel.models.m18.r08.households;

import jamel.util.Parameters;

/**
 * Represents a set of constants.
 */
class ShareholderConstants {

	/**
	 * The supervision period.
	 */
	final float supervision;

	/**
	 * The savings target ratio.
	 */
	final public String consumptionGoodsQuality;

	/**
	 * The propensity to consume excess savings.
	 */
	final public float savingsPropensityToConsumeExcess;

	/**
	 * The propensity to save?
	 */
	final public float savingsPropensityToSave;

	/**
	 * The savings target ratio.
	 */
	final public float savingsRatioTarget;

	/**
	 * The number of suppliers to be selected in the consumption phase.
	 */
	final public int supplySearch;

	/**
	 * Creates a new set of constants.
	 * 
	 * @param parameters
	 *            the parameters.
	 */
	public ShareholderConstants(Parameters parameters) {
		this.supervision = parameters.getInt("supervision");
		this.supplySearch = parameters.getInt("goodMarket.search");
		this.savingsPropensityToConsumeExcess = parameters.getFloat("goodMarket.savingsPropensityToConsumeExcess");
		this.savingsPropensityToSave = parameters.getFloat("goodMarket.savingPropensity");
		this.savingsRatioTarget = parameters.getFloat("goodMarket.savingsRatioTarget");
		this.consumptionGoodsQuality = parameters.getString("goodMarket.quality");
	}

}