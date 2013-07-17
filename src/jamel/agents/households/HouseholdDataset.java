package jamel.agents.households;

import jamel.agents.firms.ProductionType;

/**
 * A class for the household data.
 */
public class HouseholdDataset implements HouseholdDatasetInterface {	
		
	/** The consumption budget. */
	private long consumptionBudget;
	
	/** The consumption value. */
	private long consumptionValue;
	
	/** The consumption volume. */
	private int consumptionVolume;
	
	/** The deposits. */
	private long deposits;
	
	/** The dividend. */
	private long dividend;
	
	/** The employment status. */
	private int employmentStatus;
	
	/** The forced savings. */
	private long forcedSavings;
	
	/** The reservation wage. */
	private float reservationWage;
	
	/** The unemployment duration. */
	private double unemploymentDuration;
	
	/** The wage. */
	private long wage;

	/** sector */
	private ProductionType sector;

	/**
	 * @param value - the value to add.
	 */
	public void addDividend(long value) {
		this.dividend+=value;
	}

	/**
	 * @param value - the value to add.
	 */
	public void addToConsumptionValue(long value) {
		this.consumptionValue+=value;
	}

	/**
	 * @param volume - the volume to add.
	 */
	public void addToConsumptionVolume(int volume) {
		this.consumptionVolume+=volume;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.HouseholdDatasetInterface#clear()
	 */
	@Override
	public void clear() {
		consumptionBudget=0;
		consumptionValue=0;
		consumptionVolume=0;
		deposits=0;
		dividend=0;
		employmentStatus=-1;
		forcedSavings=0;
		reservationWage=0;
		unemploymentDuration=0;
		wage=0;		
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.HouseholdDatasetInterface#getConsumptionBudget()
	 */
	@Override
	public long getConsumptionBudget() {
		return consumptionBudget;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.HouseholdDatasetInterface#getConsumptionValue()
	 */
	@Override
	public long getConsumptionValue() {
		return consumptionValue;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.HouseholdDatasetInterface#getDeposits()
	 */
	@Override
	public long getDeposits() {
		return deposits;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.HouseholdDatasetInterface#getDividend()
	 */
	@Override
	public long getDividend() {
		return dividend;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.HouseholdDatasetInterface#getEmploymentStatus()
	 */
	@Override
	public Integer getEmploymentStatus() {
		return employmentStatus;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.HouseholdDatasetInterface#getForcedSavings()
	 */
	@Override
	public long getForcedSavings() {
		return forcedSavings;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.HouseholdDatasetInterface#getIncome()
	 */
	@Override
	public long getIncome() {
		return wage+dividend;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.HouseholdDatasetInterface#getReservationWage()
	 */
	@Override
	public float getReservationWage() {
		return reservationWage;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.HouseholdDatasetInterface#getUnemploymentDuration()
	 */
	@Override
	public double getUnemploymentDuration() {
		return unemploymentDuration;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.HouseholdDatasetInterface#getWage()
	 */
	@Override
	public long getWage() {
		return wage;
	}

	/**
	 * Sets the consumption budget.
	 * @param value - the value to set.
	 */
	public void setConsumptionBudget(long value) {
		this.consumptionBudget=value;
	}

	/**
	 * Sets the deposits.
	 * @param value - the value to set.
	 */
	public void setDeposits(long value) {
		this.deposits=value;
	}

	/**
	 * Sets the employment status.
	 * @param status - the status to set.
	 */
	public void setEmploymentStatus(int status) {
		this.employmentStatus=status;
	}

	/**
	 * Sets the forced savings.
	 * @param value - the value to set.
	 */
	public void setForcedSavings(long value) {
		this.forcedSavings=value;
	}

	/**
	 * Sets the reservation wage.
	 * @param value - the value to set.
	 */
	public void setReservationWage(float value) {
		this.reservationWage=value;
	}

	/**
	 * Sets the duration of unemployment.
	 * @param duration - the duration to set.
	 */
	public void setUnemploymentDuration(float duration) {
		this.unemploymentDuration=duration;
	}

	/**
	 * Sets the wage.
	 * @param value - the value to set.
	 */
	public void setWage(long value) {
		if (value==0)
			throw new RuntimeException("Wage equals 0.");
		this.wage=value;
	}

	/**
	 * Sets the sector.
	 * @param sector  the sector to set.
	 */
	public void setSector(ProductionType sector) {
		this.sector = sector;
	}

	@Override
	public int getConsumptionVolume() {
		return this.consumptionVolume;
	}

	@Override
	public ProductionType getSector() {
		return this.sector;
	}	
	
}