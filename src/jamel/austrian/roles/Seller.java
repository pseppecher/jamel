package jamel.austrian.roles;


import jamel.austrian.roles.Offerer;
import jamel.austrian.widgets.Cheque;


public interface Seller extends Offerer {

	/**
	 * Sells one unit of a good to another agent.
	 * @param offer - the offer to which the buyer responds.
	 * @param cheque - the payment.
	 */
	void sell(Cheque cheque);

}
