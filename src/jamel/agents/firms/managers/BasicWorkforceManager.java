/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2014, Pascal Seppecher and contributors.
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
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.]
 * [JAMEL uses JFreeChart, copyright by Object Refinery Limited and Contributors. 
 * JFreeChart is distributed under the terms of the GNU Lesser General Public Licence (LGPL). 
 * See <http://www.jfree.org>.]
 */

package jamel.agents.firms.managers;

import jamel.Circuit;
import jamel.JamelObject;
import jamel.agents.firms.Labels;
import jamel.agents.firms.util.Mediator;
import jamel.agents.roles.Employer;
import jamel.agents.roles.Worker;
import jamel.spheres.monetarySphere.Account;
import jamel.spheres.monetarySphere.Check;
import jamel.util.markets.EmploymentContract;
import jamel.util.markets.JobOffer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 * The basic workforce manager.<br>
 * This manager is in charge of the hiring and firing decisions.
 */
public class BasicWorkforceManager extends JamelObject implements WorkforceManager {

	@SuppressWarnings("javadoc")
	protected static final String PARAM_LABOR_CONTRACT_MAX = "Firms.laborContract.max";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_LABOR_CONTRACT_MIN = "Firms.laborContract.min";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_VAC_TARGET = "Firms.vacancies.normalRate";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_VACANCY_SPAN = "Firms.vacancies.period";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_WAGE_DEFAULT = "Firms.wage.default";
	
	@SuppressWarnings("javadoc")
	protected static final String PARAM_WAGE_FLEX_DOWN = "Firms.wage.downFlex";
	
	@SuppressWarnings("javadoc")
	protected static final String PARAM_WAGE_FLEX_UP = "Firms.wage.upFlex";

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

	/**
	 * Returns a labor contract duration chosen at random in the interval specified in settings.  
	 * @return an integer that represents the labor contract duration (in months).
	 */
	private static int getRandomLabourContractLenght() {
		final int labourContractMax = Integer.parseInt(Circuit.getParameter(PARAM_LABOR_CONTRACT_MAX));
		final int labourContractMin = Integer.parseInt(Circuit.getParameter(PARAM_LABOR_CONTRACT_MIN));
		if (labourContractMax<labourContractMin) throw new IllegalArgumentException();
		if (labourContractMax==labourContractMin) return labourContractMin ;
		return labourContractMin+getRandom().nextInt(labourContractMax-labourContractMin) ;
	}

	/** The number of workers hired in the current period. */
	private Integer effectiveHiring ; 

	/** effectiveWorkforce */
	private Integer effectiveWorkforce = null;							

	/** The firm. */
	private Employer employer;								

	/** The offer of the firm on the labor market. */
	private JobOffer jobOffer ;

	/** The number of jobs offered at the opening of the labor market. */
	private Integer jobsOffered;

	/** The payroll. */
	final private LinkedList<EmploymentContract> payroll ;

	/** The targeted workforce */
	private Integer targetedWorkforce = null;

	/** A time series to record the number of employed targeted. */
	private final TimeSeries targetedWorkforceTimeSeries = new TimeSeries("Jobs offered");

	/** The vacancies */
	private Integer vacancies = null;

	/** A time series to record the number of vacancies. */
	private final TimeSeries vacanciesTimeSeries = new TimeSeries("Vacancies");

	/** wageBill */
	private Long wageBill = null;

	/** The wage bill targeted. */
	private Long wageBillBudget ;

	/** The mediator */
	protected Mediator mediator;

	/** The wage proposed on the labor market. */
	protected Double offeredWage ;

	/**
	 * Creates a new workforce manager.
	 * @param mediator  the mediator.
	 */
	public BasicWorkforceManager(Mediator mediator) {
		final int vacancyRatePeriod = Integer.parseInt(Circuit.getParameter(PARAM_VACANCY_SPAN)) ;
		this.vacanciesTimeSeries.setMaximumItemAge(vacancyRatePeriod);
		this.targetedWorkforceTimeSeries.setMaximumItemAge(vacancyRatePeriod);
		this.payroll = new LinkedList<EmploymentContract>();
		this.mediator = mediator;
	}

	/**
	 * Counts the number of vacancies.
	 */
	private void countVacancies() {
		if ( this.jobsOffered>0 )
			this.vacancies = this.jobsOffered - this.effectiveHiring ;
		else
			this.vacancies  = 0 ;
		this.vacanciesTimeSeries.add(getCurrentPeriod().getMonth(), this.vacancies);
		this.targetedWorkforceTimeSeries.add(getCurrentPeriod().getMonth(), this.targetedWorkforce);
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
			final Double randomWage = (Double) Circuit.getResource(Circuit.SELECT_A_WAGE);			
			if ( randomWage!=null ) 
				this.offeredWage = randomWage ;
			else this.offeredWage = Double.parseDouble(Circuit.getParameter(PARAM_WAGE_DEFAULT)) ;
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

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.WorkforceManager#get(java.lang.String)
	 */
	@Override
	public Object get(String key) {
		Object result = null;
		if (key.equals(Labels.PAYROLL)) {
			result = this.payroll;
		}
		else if (key.equals(Labels.WORKFORCE_TARGET)) {
			result=this.targetedWorkforce;
		}
		else if (key.equals(Labels.VACANCIES)) {
			result=this.vacancies;
		}
		else if (key.equals(Labels.WORKFORCE)) {
			result=this.effectiveWorkforce;
		}
		else if (key.equals(Labels.WAGEBILL)) {
			result=this.wageBill;
		}
		else if (key.equals(Labels.WAGE)) {
			result=this.offeredWage;
		}
		else if (key.equals(Labels.JOBS_OFFERED)) {
			result=this.jobsOffered;
		}
		else if (key.equals(Labels.WAGEBILL_BUDGET)) {
			result=this.wageBillBudget;
		}
		else if (key.equals(Labels.OFFER_OF_JOB)) {
			result=this.jobOffer;
		}
		else if (key.equals(Labels.OPENING)) {
			this.open();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.WorkforceManager#jobApplication(jamel.agents.roles.Worker, jamel.util.markets.JobOffer)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.WorkforceManager#kill()
	 */
	@Override
	public void kill() {
		this.jobOffer.clear();
		this.jobOffer = null;
		for (EmploymentContract contract : this.payroll) contract.breach();
		this.payroll.clear();
		this.vacanciesTimeSeries.clear();
		this.targetedWorkforceTimeSeries.clear();
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.WorkforceManager#layoffAll()
	 */
	@Override
	public void layoffAll() {
		layoff(this.payroll.size());
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.WorkforceManager#newJobOffer()
	 */
	@Override
	public void newJobOffer() {
		if ( this.jobsOffered>0 ) {
			this.effectiveHiring = 0 ;
			this.jobOffer= new JobOffer(
					this.employer,
					this.jobsOffered,
					Math.round(this.offeredWage) 
					) ;
		}
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.WorkforceManager#open()
	 */
	@Override
	public void open() {	
		this.wageBillBudget=null;
		this.effectiveHiring=null;
		this.jobOffer=null;
		this.jobsOffered=null;
		if (this.employer==null) {
			this.employer=(Employer)this.mediator.get(Labels.FIRM);
		}
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.WorkforceManager#payWorkers()
	 */
	@Override
	public void payWorkers() {				
		this.effectiveWorkforce  = 0;
		this.wageBill  = 0l;
		countVacancies();
		if ( this.payroll.size() == 0 ) {
			// Does nothing.
		}
		else {
			Account account = (Account) this.mediator.get(Labels.ACCOUNT);
			if ( account.getAmount() < this.wageBillBudget ) throw new RuntimeException("Not enough money.");
			for ( EmploymentContract job : this.payroll ) {
				final long jobWage = job.getWage();
				final Worker worker = job.getEmployee();
				final Check check = account.newCheck( jobWage , worker);
				worker.receiveWage( check );
				wageBill += jobWage;
				effectiveWorkforce  ++;			
			}
			if (effectiveWorkforce!=this.payroll.size()) throw new RuntimeException("Workforce is not consistent with the payroll.");
		}
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.WorkforceManager#updateWorkforce()
	 */
	@Override
	public void updateWorkforce() {

		// On met a jour la liste des employés en supprimant les contrats échus.

		Iterator<EmploymentContract> iter = this.payroll.iterator() ;
		while (iter.hasNext()) {								
			EmploymentContract contract = iter.next() ;
			if (!contract.isValid()) iter.remove() ; 
		}

		// On calcule le besoin de main d'oeuvre.

		final int machinery = (Integer)this.mediator.get(Labels.MACHINERY);
		final float productionLevel = (Float)this.mediator.get(Labels.PRODUCTION_LEVEL);
		this.targetedWorkforce  = (int) ((machinery*productionLevel)/100f);
		//this.blackBoard.put(Labels.WORKFORCE_TARGET, targetedWorkforce);		

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
	}

}
