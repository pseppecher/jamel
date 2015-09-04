package jamel.jamel.widgets;

import jamel.jamel.roles.Supplier;

/**
 * Represents the supply of a firm on the goods market.
 */
public interface Supply {

	/**
	 * Buys the specified volume of goods.
	 * @param volume the volume of goods to be bought.
	 * @param cheque the cheque for the purchase.
	 * @return the purchase.
	 */
	Commodities buy(long volume, Cheque cheque);

	/**
	 * Returns the unit price of the supply.
	 * @return the unit price.
	 */
	double getPrice();

	/**
	 * Returns the price of the given volume of commodities.
	 * @param volume the volume to be evaluated.
	 * @return the price.
	 */
	long getPrice(long volume);

	/**
	 * Returns the supplier.
	 * @return the supplier.
	 */
	Supplier getSupplier();

	/**
	 * Returns the current volume of the supply.
	 * @return the volume.
	 */
	long getVolume();

}

// ***
