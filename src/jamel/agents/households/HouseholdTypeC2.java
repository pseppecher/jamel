package jamel.agents.households;

import jamel.Circuit;
import jamel.agents.roles.Employer;
import jamel.util.markets.JobOffer;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * Un ménage qui résiste à changer de secteur pour trouver un emploi. 
 */
public class HouseholdTypeC2 extends HouseholdTypeB {
	
	/** 
	 * The employer comparator.<p>
	 * To compare employers according to the wage they offer on the labor market.
	 */
	public class Comp implements Comparator<Employer> {

		/**
		 * Comparator for employers.
		 */
		@Override
		public int compare(Employer e1, Employer e2) {
			final JobOffer offer1 = e1.getJobOffer();
			final JobOffer offer2 = e2.getJobOffer();
			if ((offer1 == null) & (offer2 == null)) return 0;
			if (offer1 == null) return 1;
			if (offer2 == null) return -1;
			float p1 =1;
			float p2 =1;
			if (!e1.getProduction().equals(sector)) p1=mobility;
			if (!e2.getProduction().equals(sector)) p2=mobility;
			return (new Float(offer2.getWage()*p2).compareTo(offer1.getWage()*p1));
		}
		
	}
	
	/** The mobility on the labor market. 
	 * O : the household is not mobile.
	 * 1 : the household is perfectly mobile.
	 */
	private float mobility;
	
	/** The comparator. */
	private final Comp comparator = new Comp();
		
	/**
	 * Creates a new household.
	 * @param aName  the name.
	 * @param parameters  a string that contains some parameters.
	 */
	public HouseholdTypeC2(String aName, String parameters) {
		super(aName);
	}

	/**
	 * Updates the parameters of the household
	 * using the values recorded in the parameter map.
	 */
	protected void updateParameters() {
		super.updateParameters();
		this.mobility = Float.parseFloat(Circuit.getParameter("Households.mobility"));
	}

	/**
	 * Checks if the given offer is acceptable. 
	 * @param jobOffer  the offer to check.
	 * @return <code>true</code> if the wage of the given offer is equal or higher than the reservation wage.
	 */
	@Override
	protected boolean isAcceptable(JobOffer jobOffer) {
		float p=1;
		if (!jobOffer.getEmployer().getProduction().equals(sector)) p=mobility;
		return jobOffer.getWage()*p>=this.reservationWage;
	}
	
	/**
	 * Updates the list of employers, mixing
	 */
	@Override
	protected void updateEmployersList() {
		final int mobSize = (int) (maxEmployers*0.5f);
		final TreeSet<Employer> newEmployers = new TreeSet<Employer>(comparator);
		newEmployers.addAll(Circuit.getEmployerCollection(mobSize,null));
		newEmployers.addAll(Circuit.getEmployerCollection(maxEmployers-mobSize,this.sector));
		this.employers.clear();
		this.employers.addAll(newEmployers);
	}


}
