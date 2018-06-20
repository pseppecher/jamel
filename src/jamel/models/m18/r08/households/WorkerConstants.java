package jamel.models.m18.r08.households;

import jamel.util.Parameters;

/**
 * Represents a set of constants.
 */
class WorkerConstants {

	/**
	 * The supervision period.
	 */
	final float supervision;

	/**
	 * The name of the bank sector.
	 */
	final public String bankSectorName;

	/**
	 * The savings target ratio.
	 */
	final public String consumptionGoodsQuality;

	/**
	 * The wage flexibility.
	 */
	final public float flexibility;

	/**
	 * The wage resistance parameter.
	 */
	final public int resistance;

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
	 * The name of the supplier sector.
	 */
	final public String supplierSectorName;

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
	public WorkerConstants(Parameters parameters) {
		this.supervision = parameters.getInt("supervision");
		this.supplySearch = parameters.getInt("goodMarket.search");
		this.savingsPropensityToConsumeExcess = parameters.getFloat("goodMarket.savingsPropensityToConsumeExcess");
		this.savingsPropensityToSave = parameters.getFloat("goodMarket.savingPropensity");
		this.savingsRatioTarget = parameters.getFloat("goodMarket.savingsRatioTarget");
		this.flexibility = parameters.getFloat("laborMarket.flexibility");
		this.resistance = parameters.getInt("laborMarket.resistance");
		this.consumptionGoodsQuality = parameters.getString("goodMarket.quality");
		this.supplierSectorName = parameters.getString("goodMarket.suppliers");
		this.bankSectorName = parameters.getString("bankSector");
	}

}