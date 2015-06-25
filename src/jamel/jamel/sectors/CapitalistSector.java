package jamel.jamel.sectors;

import java.util.List;

import jamel.jamel.roles.Shareholder;

/**
 * The banking sector.
 */
public interface CapitalistSector {

	/**
	 * Returns a <code>Shareholder</code> selected at random among the agents of the sector.
	 * @return a <code>Shareholder</code> selected at random among the agents of the sector.
	 */
	Shareholder selectRandomCapitalOwner();

	/**
	 * Returns a list of <code>Shareholder</code> selected at random among the agents of the sector.
	 * @param n the number of shareholder to be selected.
	 * @return a list of <code>Shareholder</code> selected at random among the agents of the sector.
	 */
	List<Shareholder> selectRandomCapitalOwners(int n);

}

// ***
