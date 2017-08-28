package jamel.v170804.models.basicModel4.households;

import java.util.Comparator;

import jamel.v170804.models.basicModel4.interfaces.Supplier;

/**
 * Not so good idea ?
 */
@SuppressWarnings("javadoc")
class Households {

	private static class SupplierComparator implements Comparator<Supplier> {

		@Override
		public int compare(Supplier s1, Supplier s2) {
			final int result;
			final Double p1;
			final Double p2;
			if (s1 != null && s1.getSupply() != null && s1.getSupply().getVolume() > 0) {
				p1 = s1.getSupply().getPrice();
			} else {
				p1 = null;
			}
			if (s2 != null && s2.getSupply() != null && s2.getSupply().getVolume() > 0) {
				p2 = s2.getSupply().getPrice();
			} else {
				p2 = null;
			}
			if (p1 == null && p2 == null) {
				result = 0;
			} else if (p1 == null) {
				result = 1;
			} else if (p2 == null) {
				result = -1;
			} else {
				result = p1.compareTo(p2);
			}
			return result;
		}

	}

	final public static SupplierComparator supplierComparator = new SupplierComparator();

}
