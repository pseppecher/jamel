package jamel.austrian.banks;

import jamel.austrian.roles.Creditor;
import jamel.austrian.roles.Debtor;

/**
 * An interface for commercial banks.
 */
public interface Bank extends Creditor, Debtor {

	void open();

	void updateProfits();

	void setOwnershipStructure();

	void updateInterestRates();
	
	void close();
	
	void notifyRejection();

}
