/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/javadoc/index.html>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JAMEL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.agents.firms.managers;

import jamel.Circuit;
import jamel.JamelObject;
import jamel.agents.firms.Firm;
import jamel.agents.firms.Labels;
import jamel.agents.roles.Worker;
import jamel.spheres.monetarySphere.Account;
import jamel.spheres.monetarySphere.Check;
import jamel.util.Blackboard;
import jamel.util.markets.EmploymentContract;
import jamel.util.markets.JobOffer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 * The workforce manager.<br>
 * This manager is in charge of the hiring and firing decisions.
 * TODO supprimer tous les appels à "Circuit.getParameter"
 */
public class WorkforceManager extends JamelObject {

	/**
	 * Returns the average of the specified time series.
	 * @param timeSeries - the time series.
	 * @return the average.
	 */
	private static double getAverage(TimeSeries timeSeries) {
		@SuppressWarnings("unchecked")
		List<TimeSeriesDataItem> data = timeSeries.getItems();
		int count = 0;
		double sum = 0;
		for(TimeSeriesDataItem item:data){
			sum += item.getValue().doubleValue();
			count++;
		}
		return (sum/count);
	}

	/** The current account of the firm. */
	final private Account account;

	/** The number of workers hired in the current period. */
	private Integer effectiveHiring ;

	/** The firm. */
	final private Firm employer; 

	/** The offer of the firm on the labor market. */
	private JobOffer jobOffer ;							

	/** The number of jobs offered at the opening of the labor market. */
	private Integer jobsOffered;								

	/** The payroll. */
	final private LinkedList<EmploymentContract> payroll ;

	/** A time series to record the number of employed targeted. */
	private final TimeSeries targetedWorkforceTimeSeries = new TimeSeries("Jobs offered");

	/** A time series to record the number of vacancies. */
	private final TimeSeries vacanciesTimeSeries = new TimeSeries("Vacancies");

	/** The wage bill targeted. */
	private Long wageBillBudget ;

	/** The black board. */
	protected final Blackboard blackBoard;

	/** The wage proposed on the labor market. */
	protected Double offeredWage ;

	/**
	 * Creates a new workforce manager.
	 * @param theEmployer  the employer of the workforce.
	 * @param theEmployerAccount  the account of the employer.
	 * @param blackboard2  the blackBoard.
	 */
	public WorkforceManager(Firm theEmployer, Account theEmployerAccount, Blackboard blackboard2) {
		final int vacancyRatePeriod = Integer.parseInt(Circuit.getParameter("Firms.vacancies.period")) ;
		this.vacanciesTimeSeries.setMaximumItemAge(vacancyRatePeriod);
		this.targetedWorkforceTimeSeries.setMaximumItemAge(vacancyRatePeriod);
		this.payroll = new LinkedList<EmploymentContract>();
		this.employer = theEmployer;
		this.account = theEmployerAccount;
		this.blackBoard = blackboard2;
	}

	/**
	 * Counts the number of vacancies.
	 */
	private void countVacancies() {
		int vacancies;
		if ( this.jobsOffered>0 )
			vacancies = this.jobsOffered - this.effectiveHiring ;
		else
			vacancies = 0 ;
		this.vacanciesTimeSeries.add(getCurrentPeriod().getMonth(), vacancies);
		this.targetedWorkforceTimeSeries.add(getCurrentPeriod().getMonth(), (Integer)this.blackBoard.get(Labels.WORKFORCE_TARGET));
		this.blackBoard.put(Labels.VACANCIES, vacancies);
	}

	/**
	 * Returns a labor contract duration chosen at random in the interval specified in settings.  
	 * @return an integer that represents the labor contract duration (in months).
	 */
	private int getRandomLabourContractLenght() {
		final int labourContractMax = (Integer) this.blackBoard.get(Labels.labourContractMax);
		final int labourContractMin = (Integer) this.blackBoard.get(Labels.labourContractMin);
		if (labourContractMax<labourContractMin) throw new IllegalArgumentException();
		if (labourContractMax==labourContractMin) return labourContractMin ;
		return labourContractMin+getRandom().nextInt(labourContractMax-labourContractMin) ;
	}

	/**
	 * Fires the specified number of employees.<br>
	 * Uses the "last hired first fired" rule.
	 * @param fired - the number of employees to fire.
	 */
	private void layoff(int fired) {
		if (fired>this.payroll.size()) throw new RuntimeException();
		for (int i=0; i<fired; i++) {
			final EmploymentContract contract = this.payroll.removeLast();
			if (contract.isValid()) {
				contract.breach();			
				contract.getEmployee().notifyLayoff();
			}
		}
	}

	/**
	 * Returns the vacancy rate (vacancies to expected workforce ratio).
	 * @return a float that represents the vacancy rate.
	 */
	protected double getVacanciesRate() {
		double vacancies = getAverage(this.vacanciesTimeSeries);
		double workforceRequirement = getAverage(this.targetedWorkforceTimeSeries);
		if (workforceRequirement==0) return 0;
		return vacancies/workforceRequirement;
	}

	/**
	 * Updates the wage offered.
	 */
	protected void updateWage() {
		if ( this.offeredWage==null ) {
			final Double randomWage = Circuit.getCircuit().getRandomWage() ;
			if ( randomWage!=null ) 
				this.offeredWage = randomWage ;
			else this.offeredWage = Double.parseDouble(Circuit.getParameter("Firms.wage.default")) ;
		}
		else {
			final float wageUpwardFlexibility = Float.parseFloat(Circuit.getParameter("Firms.wage.upFlex"));
			final float wageDownwardFlexibility = Float.parseFloat(Circuit.getParameter("Firms.wage.downFlex"));
			final float normalVacancyRate = Float.parseFloat(Circuit.getParameter("Firms.vacancies.normalRate"));
			final double currentVacancyRate = this.getVacanciesRate() ;
			final float alpha1 = getRandom().nextFloat();
			final float alpha2 = getRandom().nextFloat();
			final double vacancyRatio = currentVacancyRate/normalVacancyRate;
			if (vacancyRatio<1-alpha1*alpha2) {
				this.offeredWage=this.offeredWage*(1f-alpha1*wageDownwardFlexibility) ;
			}
			else if (vacancyRatio>1+alpha1*alpha2) {
				this.offeredWage=this.offeredWage*( 1f+alpha1*wageUpwardFlexibility) ; 
			}
		}
		this.offeredWage = Math.max( this.offeredWage,Integer.parseInt(Circuit.getParameter("Firms.wage.minimum"))) ;
		if ( this.offeredWage==0 ) 
			throw new RuntimeException("The wage is null.");
	}

	/**
	 * Receives a job application.
	 * @param worker - the applicant.
	 * @param offer2 - the related job offer.
	 */
	public void jobApplication(Worker worker, JobOffer offer2) {
		if ( !offer2.equals(this.jobOffer) ) throw new RuntimeException("Bad Offer.") ;
		if ( this.jobsOffered==0 ) throw new RuntimeException("No vacancy.") ;
		this.effectiveHiring ++ ;
		this.jobOffer.subtract(1);
		if (this.jobOffer.getVolume()==0) this.jobOffer = null;
		final EmploymentContract newContract = new EmploymentContract(
				this.employer,
				worker,
				Math.round(this.offeredWage),
				getRandomLabourContractLenght()
				);
		worker.notifyHiring(newContract);
		this.payroll.add(newContract);
	}

	/**
	 * Kills the manager.
	 */
	public void kill() {
		this.jobOffer.clear();
		this.jobOffer = null;
		for (EmploymentContract contract : this.payroll) contract.breach();
		this.payroll.clear();
		this.vacanciesTimeSeries.clear();
		this.targetedWorkforceTimeSeries.clear();
	}

	/**
	 * Fires all the employees.
	 */
	public void layoffAll() {
		layoff(this.payroll.size());
	}

	/**
	 * Creates a new offer on the labor market.
	 */
	public void newJobOffer() {
		if ( this.jobsOffered>0 ) {
			this.effectiveHiring = 0 ;
			this.jobOffer= new JobOffer(
					this.employer,
					this.jobsOffered,
					Math.round(this.offeredWage) 
					) ;
			this.blackBoard.put(Labels.OFFER_OF_JOB, jobOffer);
		}
	}

	/**
	 * Opens the manager.<br>
	 * Called at the beginning of the period to initialize the manager. 
	 */
	public void open() {	
		this.wageBillBudget=null;
		this.effectiveHiring=null;
		this.jobOffer=null;
		this.jobsOffered=null;
	}

	/**
	 * Pays all employees.
	 */
	public void payWorkers() {				
		int effectiveWorkforce = 0;
		long wageBill = 0;
		countVacancies();
		if ( this.payroll.size() == 0 ) {
			// Does nothing.
		}
		else {
			if ( this.account.getAmount() < this.wageBillBudget ) throw new RuntimeException("Not enough money.");
			for ( EmploymentContract job : this.payroll ) {
				final long jobWage = job.getWage();
				final Worker worker = job.getEmployee();
				final Check check = this.account.newCheck( jobWage , worker);
				worker.receiveWage( check );
				wageBill += jobWage;
				effectiveWorkforce  ++;			
			}
			if (effectiveWorkforce!=this.payroll.size()) throw new RuntimeException("Workforce is not consistent with the payroll.");
		}
		this.blackBoard.put(Labels.WORKFORCE,effectiveWorkforce);
		this.blackBoard.put(Labels.WAGEBILL,wageBill);
	}

	/**
	 * Updates the available workforce.
	 */
	public void updateWorkforce() {

		// On met a jour la liste des employés en supprimant les contrats échus.

		Iterator<EmploymentContract> iter = this.payroll.iterator() ;
		while (iter.hasNext()) {								
			EmploymentContract contract = iter.next() ;
			if (!contract.isValid()) iter.remove() ; 
		}

		// On calcule le besoin de main d'oeuvre.

		final int machinery = (Integer)this.blackBoard.get(Labels.MACHINERY);
		final float productionLevel = (Float)this.blackBoard.get(Labels.PRODUCTION_LEVEL);
		final int targetedWorkforce = (int) ((machinery*productionLevel)/100f);
		this.blackBoard.put(Labels.WORKFORCE_TARGET, targetedWorkforce);		

		// Si la main d'oeuvre employée dépasse les besoins, on licencie. 

		final int targetedHiring = targetedWorkforce - this.payroll.size();
		if ( targetedHiring<0 ) {
			this.layoff( -targetedHiring ) ;
			this.jobsOffered = 0;
		}
		else {
			if ( targetedHiring>0 ) {
				this.jobsOffered = targetedHiring;
			}
			else{
				this.jobsOffered = 0;				
			}
		}
		this.blackBoard.put(Labels.JOBS_OFFERED, this.jobsOffered);

		if (targetedHiring>0) 
			updateWage();

		// Computes the future wage bill.

		this.wageBillBudget=0l;
		for (EmploymentContract contract : this.payroll) {
			this.wageBillBudget += contract.getWage();			
		}
		if ( this.jobsOffered>0 ) {
			this.wageBillBudget += Math.round(this.offeredWage)*this.jobsOffered;
		}
		this.blackBoard.put(Labels.WAGEBILL_BUDGET, this.wageBillBudget);
		this.blackBoard.put(Labels.PAYROLL, this.payroll);
	}

}
