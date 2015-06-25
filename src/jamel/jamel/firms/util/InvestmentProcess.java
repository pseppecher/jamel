package jamel.jamel.firms.util;

import jamel.jamel.widgets.Commodities;

/**
 * An investment process.
 */
public interface InvestmentProcess {

	/**
	 * Cancels the process.
	 */
	void cancel();

	/**
	 * Returns the volume of commodities required to complete the process.
	 * @return the volume of commodities required to complete the process.
	 */
	long getMissingVolume();

	/**
	 * Returns the productivity.
	 * @return the productivity.
	 */
	float getProductivity();

	/**
	 * Returns <code>true</code> if the process is complete.
	 * @return <code>true</code> if the process is complete.
	 */
	boolean isComplete();

	/**
	 * Processes the investment.
	 * @param input the input.
	 */
	void progress(Commodities input);

}

// ***
