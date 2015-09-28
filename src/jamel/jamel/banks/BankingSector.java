package jamel.jamel.banks;

import java.util.List;

import jamel.basic.sector.Sector;
import jamel.jamel.aggregates.Banks;
import jamel.jamel.roles.Corporation;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.widgets.Cheque;

/**
 * Represents the banking sector.
 */
interface BankingSector extends Sector,Banks {

	/**
	 * Returns a list of {@link Shareholder} selected at random among the agents
	 * of the capitalists sector.
	 * 
	 * @param n
	 *            the number of shareholders to be selected.
	 * @return a list of {@link Shareholder} selected at random among the agents
	 *         of the sector.
	 */
	List<Shareholder> selectCapitalOwner(int n);

	/**
	 * Sells the specified corporation.
	 * @param corporation the corporation to be sold.
	 * @return the cheques.
	 */
	Cheque[] sellCorporation(Corporation corporation);

}

// ***
