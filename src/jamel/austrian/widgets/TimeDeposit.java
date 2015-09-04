/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2010, Pascal Seppecher.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the durations of the GNU General Public License as published by
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

package jamel.austrian.widgets;


import jamel.austrian.roles.Creditor;
import jamel.austrian.roles.Debtor;
import jamel.basic.util.Timer;



/**
 * Represents a time deposit.
 * <p>
 * Encapsulates the reference to the creditor and the debtor, the interest, the start, and the end of the contract.
 * <p>
 * Last update: 16-Aug-2013.
 */
public class TimeDeposit {
	
	/** The timer. */
	protected Timer timer;

	/** The debtor. */
	private final Creditor creditor ;

	/** The creditor. */
	private final Debtor debtor ;
	
	/** The face value of the contract. */
	@SuppressWarnings("unused")
	private final int faceValue ;
	
	/** The interest payment. */
	@SuppressWarnings("unused")
	private final int contractedInterest ;
	
	/** The interest rate. */
	private final float interestRate;
	
	/** The time period in which the deposit was signed. */
	private final int signingPeriod;

	/** The remaining volume of the deposit. */
	private int remainingVolume ;
	
	/** The remaining interest of the deposit. */
	private int remainingInterest ; // the distinction is necessary for the internal calculations of the banks
	
	/** The time period in which the repayments are due. */
	private int end ;
	
	/** Indicates whether the the contract is settled. */
	private boolean isSettled;
	
	/** Indicates whether payments have been deferred. */
	@SuppressWarnings("unused")
	private boolean isExtended;

	/** Indicates whether the bank has defaulted on the deposit. */
	@SuppressWarnings("unused")
	private boolean defaulting;
	
		
	
	/**
	 * Creates a new time deposit.
	 */
	public TimeDeposit(Creditor creditor, Debtor debtor, int volume, int interest, float interestRate, int duration, Timer timer)	{ 
		if (volume<=0) throw new RuntimeException("The volume cannot be zero or negative.");
		this.timer = timer;
		this.creditor = creditor;
		this.debtor = debtor;
		this.interestRate = interestRate;
		this.faceValue = volume;
		this.remainingVolume = volume-interest;
		this.contractedInterest = interest;
		this.remainingInterest = interest;
		this.signingPeriod = timer.getPeriod().intValue();
		this.end = signingPeriod + duration;
		this.isSettled = false;
		this.isExtended = false;
		this.defaulting = false;
	}

	
	/**
	 * Returns the debtor.
	 */
	public Debtor getDebtor() { 
		return debtor ; 
	}

	
	/**
	 * Returns the creditor. 
	 */
	public Creditor getCreditor() { 
		return creditor ; 
	}
	
	
	/**
	 * Subtracts an amount from the volume of the contract.
	 */
	public void redemptionPayment(int amount) {
		if (amount <= 0) throw new IllegalArgumentException("The redemption Payment cannot be zero or negative.");
		if (amount > remainingVolume) throw new IllegalArgumentException("The redemption Payment exceeds the volume of the contract.");
		remainingVolume -= amount;
		if (remainingVolume==0 & remainingInterest==0) isSettled = true;
	}
	
	
	/**
	 * Updates the amount of deferred interest.
	 */
	public void interestPayment(int amount) {
		if (amount <= 0) throw new IllegalArgumentException("The interest payment cannot be zero or negative.");
		if (amount > remainingInterest) throw new IllegalArgumentException("The interest payment exceeds the remaining interest.");
		remainingInterest -= amount;			
		if (remainingVolume==0 & remainingInterest==0) isSettled = true;
	}
	
	
	/**
	 * Defers the payment and updates the contract.
	 */
	public void deferPayment() { 
		if (isSettled) throw new RuntimeException("Contract is already settled.");
		end = timer.getPeriod().intValue()+1;
		isExtended = true;
	}
	
		
	/**
	 * Indicates whether payments of this contract are due.
	 */
	public boolean isDue() { 
		return (timer.getPeriod().intValue() >= end) ; 
	}
	

	/**
	 * Indicates whether the contract has been settled.
	 */
	public boolean isSettled() { 
		return isSettled ; 
	}
	
	
	/**
	 * Defaults on the contract.
	 */
	public void breach() {
		if (!isValid()) throw new RuntimeException("Invalid Contract Exception!") ;
		defaulting = true;
		end = timer.getPeriod().intValue() ;
	}

	
	/**
	 * Returns signing period. 
	 */
	public int getSigningPeriod() { 
		return signingPeriod ; 
	}

	
	/**
	 * Returns the total duration of the time deposit.
	 */
	public int getTerm() { 
		return end-signingPeriod ; 
	}
	
	
	/**
	 * Returns the remaining duration of the time deposit.
	 */
	public int getRemainingDuration() { 
		int remainingTimePeriods = end - timer.getPeriod().intValue();
		if (remainingTimePeriods<0) throw new RuntimeException("Deposit is expired.");
		return remainingTimePeriods; 
	}

	
	/**
	 * Returns the interest rate of the time deposit.
	 */
	public float getInterestRate() { 
		return interestRate ; 
	}
	
	
	/**
	 * Returns the remaining interest to be paid on the deposit.
	 */
	public int getInterest() { 
		return remainingInterest ; 
	}
	
	
	/**
	 * Returns the remaining volume of the deposit.
	 */
	public int getVolume() { 
		return remainingVolume ; 
	}
	
	
	/**
	 * Returns the amount to be received by the depositor.
	 */
	public int getReceivableAmount() { 
		return remainingInterest+remainingVolume; 
	}

	
	/**
	 * Indicates whether the contract is still valid.
	 */
	public boolean isValid() { 
		return (timer.getPeriod().intValue() <= end) ; 
	}
	
}