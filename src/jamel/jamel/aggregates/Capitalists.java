package jamel.jamel.aggregates;

import java.util.List;

import jamel.jamel.roles.Corporation;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.widgets.Cheque;

/**
 * A capitalist sector.
 * <p>
 * A capitalist sector is a sector composed of {@link Shareholder}.
 */
public interface Capitalists {

	/**
	 * Returns the list of all the {@link Shareholder} in the sector, sorted in
	 * random order.
	 * 
	 * @return the list of all the {@link Shareholder} in the sector, sorted in
	 *         random order.
	 */
	List<Shareholder> selectCapitalOwners();

	/**
	 * Returns a {@link Shareholder} selected at random among the agents of the
	 * sector.
	 * 
	 * @return a {@link Shareholder} selected at random among the agents of the
	 *         sector.
	 */
	Shareholder selectRandomCapitalOwner();

	/**
	 * Returns a list of {@link Shareholder} selected at random among the agents
	 * of the sector.
	 * 
	 * @param n
	 *            the number of shareholders to be selected.
	 * @return a list of {@link Shareholder} selected at random among the agents
	 *         of the sector.
	 */
	List<Shareholder> selectRandomCapitalOwners(int n);

	/**
	 * Achète une entreprise d'occasion.
	 * 
	 * @param corporation
	 *            la firme mise en vente.
	 * @return an array that contains the cheques for the payment.
	 */
	Cheque[] buyCorporation(Corporation corporation);

}

// ***
