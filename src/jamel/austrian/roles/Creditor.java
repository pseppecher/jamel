package jamel.austrian.roles;

import jamel.austrian.roles.Offerer;
import jamel.austrian.widgets.AbstractCheque;
import jamel.austrian.widgets.CreditContract;
import jamel.austrian.widgets.TimeDeposit;


public interface Creditor extends Offerer {

	AbstractCheque acceptDebtor(CreditContract newContract);

	void receiveInterestPayment(AbstractCheque cheque);

	void receiveRedemption(AbstractCheque cheque);

	void notifyDefault(TimeDeposit timeDeposit);
	
	void notifySettlement(TimeDeposit timeDeposit);
	
	void notifyRedemption(CreditContract contract);

	void notifyDefault(CreditContract contract);

	
}
