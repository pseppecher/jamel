package jamel.agents.households;

import jamel.Circuit;
import jamel.agents.roles.Employer;

import java.util.TreeSet;

/**
 * Un ménage qui résiste à changer de secteur pour trouver un emploi. 
 */
public class HouseholdTypeC extends HouseholdTypeB {
	
	/** The mobility on the labor market. 
	 * O : the household is not mobile.
	 * 1 : the household is perfectly mobile.
	 */
	protected float mobility;
	
	/**
	 * Creates a new household.
	 * @param aName  the name.
	 */
	public HouseholdTypeC(String aName) {
		super(aName);
		this.mobility = 0.5f; // TODO should be a parameter.
	}

	
	/**
	 * Updates the list of employers, mixing
	 */
	@Override
	protected void updateEmployersList() {
		final int mobSize = (int) (maxEmployers*mobility);
		final TreeSet<Employer> newEmployers = new TreeSet<Employer>(EMPLOYER_COMPARATOR);
		newEmployers.addAll(Circuit.getEmployerCollection(mobSize,null));
		newEmployers.addAll(Circuit.getEmployerCollection(maxEmployers-mobSize,this.sector));
		this.employers.clear();
		this.employers.addAll(newEmployers);
	}
	
}
