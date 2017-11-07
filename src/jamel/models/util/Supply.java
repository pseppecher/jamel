package jamel.models.util;

/**
 * Represents a supply of commodities.
 */
public interface Supply {

	/**
	 * Returns the unit price of the goods.
	 * 
	 * @return the unit price of the goods.
	 */
	Double getPrice();

	/**
	 * Returns the price of the given volume of commodities.
	 * 
	 * @param volume
	 *            the volume to be evaluated.
	 * @return the price.
	 */
	long getPrice(long volume);

	/**
	 * Returns the supplier.
	 * 
	 * @return the supplier.
	 */
	Supplier getSupplier();

	/**
	 * Returns the total value (at current price) of the supply.
	 * 
	 * @return the total value (at current price) of the supply.
	 */
	long getValue();

	/**
	 * Returns the volume of this supply.
	 * 
	 * @return the volume of this supply.
	 */
	long getVolume();

	/**
	 * Returns {@code true} if this supply is empty (no goods to sell).
	 * 
	 * @return {@code true} if this supply is empty.
	 */
	boolean isEmpty();

	/**
	 * Purchases some goods.
	 * 
	 * @param demand
	 *            the volume to be purchased.
	 * 
	 * @param payment
	 *            the payment cheque.
	 * 
	 * @return the purchased goods.
	 */
	Commodities purchase(long demand, Cheque payment);

}
