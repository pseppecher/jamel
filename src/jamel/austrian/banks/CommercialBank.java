package jamel.austrian.banks;

import jamel.austrian.roles.AccountHolder;
import jamel.austrian.roles.Creditor;
import jamel.austrian.roles.Debtor;
import jamel.austrian.widgets.AbstractCheque;

/**
 * An interface for commercial banks.
 */
public interface CommercialBank extends Creditor, Debtor {

	void open();

	void getNewAccount(AccountHolder accountHolder, int money);
	
	int getBalance(AccountHolder accountHolder);

	void deposit(AccountHolder accountHolder, AbstractCheque cheque);
	
	AbstractCheque newCheque(AccountHolder accountHolder, int amount);
	
	void closeAccount(AccountHolder accountHolder);

	void updateInterestRates();
	
	void updateProfits();
	
	void close();
	
	void notifyRejection();
	
	void terminateOperations();
	
	void setOwnershipStructure();

}
