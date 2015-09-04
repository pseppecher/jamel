package jamel.austrian.roles;

import jamel.austrian.roles.Offerer;
import jamel.austrian.widgets.Cheque;
import jamel.austrian.widgets.CreditContract;
import jamel.austrian.widgets.TimeDeposit;


public interface Creditor extends Offerer {

	Cheque acceptDebtor(CreditContract newContract);

	void receiveInterestPayment(Cheque cheque);

	void receiveRedemption(Cheque cheque);

	void notifyDefault(TimeDeposit timeDeposit);
	
	void notifySettlement(TimeDeposit timeDeposit);
	
	void notifyRedemption(CreditContract contract);

	void notifyDefault(CreditContract contract);

	
}
