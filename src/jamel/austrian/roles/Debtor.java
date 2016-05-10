package jamel.austrian.roles;

import jamel.austrian.widgets.AbstractCheque;
import jamel.austrian.widgets.TimeDeposit;

public interface Debtor extends Offerer {
	
	
	void payInterest();

	void acquireFunding(TimeDeposit timeDeposit, AbstractCheque cheque);

	int getEquityValue(Shareholder owner);

}
