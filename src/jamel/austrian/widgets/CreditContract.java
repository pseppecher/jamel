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
 * Represents a credit contract.
 * <p>
 * Encapsulates the reference to the creditor and the debtor, the interest and the end of the contract.
 * <p>
 * Last update: 8-Feb-2013.
 */
public class CreditContract  {

	/** The timer. */
	protected Timer timer;
	
	/** The debtor. */
	private final Creditor creditor;

	/** The creditor. */
	private final Debtor debtor;
	
	/** The face value of the contract. */
	private int faceValue;
	
	/** The original face value of the contract. */
	@SuppressWarnings("unused")
	private final int originalFaceValue;
	
	/** The interest rate (monthly). */
	private final float interest;
	
	/** The number of time periods between the signing of the contract and the first installment. */
	private int signingPeriod;
	
	/** The period in which the first payment is due. */
	private int start;

	/** The period in which the last payment is due. */
	private int end;

	/** The remaining volume. */
	private int volume;
	
	/** The amount of interest which has not been paid in time. */
	private int deferredInterest;
	
	@SuppressWarnings("unused")
	private int lastDeferment;
	
	private boolean isSettled;
	
	@SuppressWarnings("unused")
	private boolean isExtended;
	
	@SuppressWarnings("unused")
	private boolean isDeferred;

	private boolean isCreditLine;
	
	/**
	 * Creates a new employment contract.
	 */
	public CreditContract(Creditor creditor, Debtor debtor, int volume, float interest, int duration, boolean isCreditLine, Timer timer)	{ 
		if (volume<=0) throw new RuntimeException("The volume cannot be zero or negative.");
		this.timer = timer;
		this.creditor = creditor;
		this.debtor = debtor;
		this.isCreditLine = isCreditLine;
		this.signingPeriod = timer.getPeriod().intValue();
		this.originalFaceValue = volume;
		this.faceValue = volume;
		this.volume = volume;
		this.interest = interest;
		if (isCreditLine) this.start = signingPeriod + 1;
		else this.start = signingPeriod;
		if (volume>duration) this.end = start+(duration-1); 
		else this.end = start;  // Ensures that redemption is calculated correctly.
		this.isSettled = false;
		this.isExtended = false;
		this.isDeferred = false;
		this.deferredInterest = 0;
		this.lastDeferment = 0;
	}

	
	/**
	 * Returns the debtor.
	 */
	public Debtor getDebtor() { 
		return debtor; 
	}

	
	/**
	 * Returns the creditor. 
	 */
	public Creditor getCreditor() { 
		return creditor; 
	}
	
	
	/**
	 * Subtracts an amount from the volume of the contract.
	 */
	public void subtract(int amount) {
		if (amount <= 0) throw new IllegalArgumentException("The subtracted volume cannot be zero or negative.");
		if (amount > volume) throw new IllegalArgumentException("The subtracted volume exceeds the volume of the contract.");
		volume -= amount;
		if (volume==0 & deferredInterest==0) isSettled = true;
	}
	
	
	
	/**
	 * Updates the amount of deferred interest.
	 */
	public void interestPayment(int amount) {
		
		if (amount <= 0) throw new IllegalArgumentException("The interest payment cannot be zero or negative.");
		if (deferredInterest>0){
			if (amount>=deferredInterest) deferredInterest = 0;
			else deferredInterest -= amount;
		}	
		if (volume==0 & deferredInterest==0) isSettled = true;
	}
	
	
	
	/**
	 * Returns the monthly redemption requirement<br>.
	 */
	public int getRedemption() {
		if (isCreditLine) return 0;
		if (volume==0) return 0;  // Some contracts may be unsettled only because of deferred interest.
		int monthlyRedemption = faceValue/getTerm();
		int remainder = volume % monthlyRedemption;
		if (monthlyRedemption+remainder>volume) throw new RuntimeException("Too much redemption.");
		return monthlyRedemption + remainder;
	}
	
	
	/**
	 * Defers the payment and updates the contract. <p>
	 * Must be called *after* the redemption payments have been made!
	 */
	public void deferInterest(int deferredInterest) { 
		if (isSettled) throw new RuntimeException("Contract is already settled.");
		if (deferredInterest<0) throw new IllegalArgumentException("The deferred volume cannot be negative.");
		
		this.deferredInterest = deferredInterest;
		isDeferred = true;
	}
	
	
	/**
	 * Defers the payment and updates the contract. <p>
	 * Must be called *after* the redemption payments have been made!
	 */
	public void deferPayment() { 
		if (isSettled) throw new RuntimeException("Contract is already settled.");
		
		faceValue = volume;		
		start = timer.getPeriod().intValue() + 1;
		end += 1;
		if (end<start) end = start; //TODO: why on earth is this actually necessary?
		isExtended = true;
	}
	
	
	/**
	 * Indicates whether the contract is still valid.
	 */
	public boolean isValid() { 
		return (timer.getPeriod().intValue() <= end); 
	}
	
		
	/**
	 * Indicates whether payments of this contract are due.
	 */
	public boolean isDue() { 
		return (timer.getPeriod().intValue() >= start); 
	}
	
	
	/**
	 * Indicates whether payments of this contract are due in the next time period.
	 */
	public boolean isDueNextPeriod() { 
		return (timer.getPeriod().intValue() + 1 <= end); 
	}
	

	/**
	 * Indicates whether the contract has been settled.
	 */
	public boolean isSettled() { 
		return isSettled; 
	}
	
	
	/**
	 * Breaches the contract.<br>
	 * Used in case of bankruptcy.
	 */
	public void breach() {
		if (!isValid()) throw new RuntimeException("Invalid Contract Exception!");
		end = timer.getPeriod().intValue();
	}

	
	/**
	 * Returns the number of time periods between the signing of the contract and the first installment. 
	 */
	public int getSigningPeriod() { 
		return signingPeriod; 
	}

	
	/**
	 * Returns the number of time periods over which the contract needs to be redeemed.
	 */
	public int getTerm() { 
		return end-start+1; 
	}

	/**
	 * Returns the interest rate of the contract.
	 */
	public float getInterest() { 
		return interest; 
	}
	
	/**
	 * Returns the current credit volume (exclusive of deferred interest).
	 */
	public int getVolume() { 
		return volume; 
	}
	
	
	/**
	 * Returns the interest payments which have not been made on time.
	 */
	public int getDeferredInterest() { 
		if (deferredInterest<0) throw new RuntimeException("Negative interest!");
		return deferredInterest; 
	}
	
	
	/**
	 * Returns the original credit volume.
	 */
	public int getFaceValue() { 
		return faceValue; 
	}

}