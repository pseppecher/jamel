package jamel.jamel.firms.util;

import jamel.jamel.widgets.JobContract;
import jamel.jamel.widgets.LaborPower;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A collection of job contracts.
 * <p>
 * "The workforce is the labour pool in employment. It is generally used to
 * describe those working for a single company or industry (...) The term
 * generally excludes the employers or management, and can imply those involved
 * in manual labour."
 * 
 * (ref: <a href="https://en.wikipedia.org/wiki/Workforce">Wikipedia</a>)
 */
public class Workforce extends LinkedList<JobContract> {

	/**
	 * Removes all invalid job contracts.
	 */
	public void cleanUp() {
		final Iterator<JobContract> iter = this.iterator();
		while (iter.hasNext()) {
			JobContract contract = iter.next();
			if (!contract.isValid())
				iter.remove();
		}
	}

	/**
	 * Returns an array containing all of the labor powers of the workforce.
	 * 
	 * @return an array of labor powers.
	 */
	public LaborPower[] getLaborPowers() {
		final LaborPower[] manPower = new LaborPower[this.size()];
		int index = 0;
		for (JobContract contract : this) {
			manPower[index] = contract.getLaborPower();
			index++;
		}
		return manPower;
	}

	/**
	 * Returns the payroll.
	 * <p>
	 * "In accounting, payroll refers to the amount paid to employees for
	 * services they provided during a certain period of time." (<a
	 * href="https://en.wikipedia.org/wiki/Payroll">Wikipedia</a>)
	 * <p>
	 * The payroll is calculated each time the <code>getPayroll()</code> method
	 * is called, by summing the wages of each contract in the list.
	 * 
	 * @return the payroll.
	 */
	public long getPayroll() {
		long payroll = 0;
		for (JobContract contract : this) {
			if (!contract.isValid()) {
				throw new RuntimeException("Invalid job contract.");
			}
			payroll += contract.getWage();
		}
		return payroll;
	}

	/**
	 * Lays off the specified number of employees.<br>
	 * Uses the "last hired first fired" rule.
	 * 
	 * @param fired
	 *            the number of employees to fire.
	 */
	public void layoff(int fired) {
		if (fired > this.size()) {
			throw new IllegalArgumentException(
					"The number of workers to be fired exceeds the workforce size.");
		}
		final int target = this.size() - fired;
		while (this.size() > target) {
			final JobContract contract = this.removeLast();
			contract.breach();
		}
	}

	/**
	 * Lays off all the employees.
	 */
	public void layoff() {
		for (JobContract contract : this) {
			contract.breach();
		}
		this.clear();
	}

}

// ***
