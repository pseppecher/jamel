package jamel.jamel.sectors;

import java.util.List;

import jamel.jamel.firms.Firm;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.widgets.Cheque;

/**
 * A capitalist sector.
 * <p>
 * A capitalist sector is a sector composed of {@link Shareholder}.
 */
public interface CapitalistSector {

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
	 * Vends une firme d'occasion.
	 * 
	 * @param firm
	 *            la firme mise en vente.
	 * @return la liste des cheques de paiements.
	 */
	Cheque[] sellFim(Firm firm);

}

// ***
