package jamel.agents.firms.typeA;

import jamel.Circuit;
import jamel.agents.firms.Firm;
import jamel.agents.firms.Labels;
import jamel.agents.firms.managers.WorkforceManager;
import jamel.spheres.monetarySphere.Account;
import jamel.util.Blackboard;

/**
 * Un workforce manager sensible au niveau de la production
 */
public class WorkforceManagerA1 extends WorkforceManager {

	private static final Float normalLevel = 80f;

	/**
	 * Creates a new workforce manager.
	 * @param theEmployer the employer
	 * @param theEmployerAccount the account of the employer
	 * @param blackboard2 the black board
	 */
	public WorkforceManagerA1(Firm theEmployer, Account theEmployerAccount, Blackboard blackboard2) {
		super(theEmployer, theEmployerAccount, blackboard2);
	}
	
	/**
	 * Updates the wage offered.
	 */
	@Override
	protected void updateWage() {
		if ( this.offeredWage==null ) {
			final Double randomWage = Circuit.getCircuit().getRandomWage() ;
			if ( randomWage!=null ) 
				this.offeredWage = randomWage ;
			else this.offeredWage = defaultWage ;
		}
		else {
			//final float wageUpwardFlexibility = (Float) this.blackBoard.get(Labels.WAGE_UP_FLEX);DELETE
			//final float wageDownwardFlexibility = (Float) this.blackBoard.get(Labels.WAGE_DOWN_FLEX);DELETE
			final float wageUpwardFlexibility = Float.parseFloat(Circuit.getParameter("Firms.wageUpFlex"));
			final float wageDownwardFlexibility = Float.parseFloat(Circuit.getParameter("Firms.wageDownFlex"));
			final Float productionLevel = (Float)this.blackBoard.get(Labels.PRODUCTION_LEVEL);
			final Float npl;
			if (productionLevel<normalLevel) {
				npl = 0.5f*productionLevel/normalLevel;
			}
			else {
				npl = 0.5f*(1f+(productionLevel-normalLevel)/(100f-normalLevel));			
			}
			final double currentVacancyRate = this.getVacanciesRate() ;
			final float alpha1 = getRandom().nextFloat();
			final float alpha2 = getRandom().nextFloat();
			final float alpha3 = getRandom().nextFloat();
			final double vacancyRatio = currentVacancyRate/normalVacancyRate;
			if ((vacancyRatio<1-alpha1*alpha2)&&(alpha1*alpha3<1-npl)) {
				this.offeredWage=this.offeredWage*(1f-alpha1*wageDownwardFlexibility) ;
			}
			else if ((vacancyRatio>1+alpha1*alpha2)&&(alpha1*alpha3<npl)) {
				this.offeredWage=this.offeredWage*(1f+alpha1*wageUpwardFlexibility) ; 
			}
		}
		this.offeredWage = Math.max( this.offeredWage,Integer.parseInt(Circuit.getParameter("minimumWage"))) ;		
		if ( this.offeredWage==0 ) 
			throw new RuntimeException("The wage is null.");
	}	

}
