package jamel.util.markets;

import jamel.agents.roles.Provider;

import java.util.Comparator;

/** 
 * The provider comparator.<p>
 * To compare providers according to the price they offer on the goods market.
 */
public class ProviderComparator implements Comparator<Provider> {

	public int compare(Provider p1, Provider p2) {
		final GoodsOffer offer1  = p1.getGoodsOffer();
		final GoodsOffer offer2  = p2.getGoodsOffer();
		if ((offer1 == null) & (offer2 == null)) return 0;
		if (offer1 == null) return 1;
		if (offer2 == null) return -1;
		if ((offer1.getVolume() == 0) | (offer2.getVolume() == 0)) new RuntimeException();
		if ((offer1.getPrice() == 0) | (offer2.getPrice() == 0)) new RuntimeException();
		return (new Double(offer1.getPrice()).compareTo(offer2.getPrice()));
	}

}
