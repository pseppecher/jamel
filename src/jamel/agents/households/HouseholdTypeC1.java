package jamel.agents.households;

import jamel.util.markets.EmploymentContract;

/**
 * Un ménage dont la mobilité sectorielle augmente avec la durée du chômage.
 */
public class HouseholdTypeC1 extends HouseholdTypeC {
	
	/** The speed at which the mobility of the household increases. */
	private static final float mSpeed = 0.03f;
	
	/**
	 * Creates a new household.
	 * @param aName  the name.
	 */
	public HouseholdTypeC1(String aName) {
		super(aName);
		this.mobility=1f;
	}

	
	/**
	 * Updates the list of employers.
	 */
	@Override
	protected void updateEmployersList() {
		super.updateEmployersList();
		if (this.mobility<1)
			this.mobility+=mSpeed;
	}
	
	/**
	 * Receives notification of his hiring.
	 * @param contract  the employment contract.
	 */
	@Override
	public void notifyHiring(EmploymentContract contract) {
		super.notifyHiring(contract);
		this.mobility=0;
	}	
	
}
