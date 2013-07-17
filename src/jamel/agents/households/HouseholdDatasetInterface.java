package jamel.agents.households;

import jamel.agents.firms.ProductionType;

/**
 *
 */
public interface HouseholdDatasetInterface {

	/**
	 * 
	 */
	public abstract void clear();

	/**
	 * @return the consumption budget.
	 */
	public abstract long getConsumptionBudget();

	/**
	 * @return the consumption value.
	 */
	public abstract long getConsumptionValue();

	/**
	 * @return the deposits.
	 */
	public abstract long getDeposits();

	/**
	 * @return the dividend.
	 */
	public abstract long getDividend();

	/**
	 * @return the employment status.
	 */
	public abstract Integer getEmploymentStatus();

	/**
	 * @return the forced savings.
	 */
	public abstract long getForcedSavings();

	/**
	 * @return the income.
	 */
	public abstract long getIncome();

	/**
	 * @return the reservation wage.
	 */
	public abstract float getReservationWage();

	/**
	 * @return the unemployment duration.
	 */
	public abstract double getUnemploymentDuration();

	/**
	 * @return the wage.
	 */
	public abstract long getWage();

	/**
	 * @return the volume consumed.
	 */
	public abstract int getConsumptionVolume();

	/**
	 * @return the sector where the household was employed the last time.
	 */
	public ProductionType getSector();

}