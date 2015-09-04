package jamel.austrian.roles;

import jamel.austrian.widgets.Cheque;
import jamel.austrian.widgets.TimeDeposit;

public interface Debtor extends Offerer {
	
	
	void payInterest();

	void acquireFunding(TimeDeposit timeDeposit, Cheque cheque);

	int getEquityValue(Shareholder owner);

}
