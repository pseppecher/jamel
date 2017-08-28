package jamel.v170804.models.basicModel3.households;

import java.util.Comparator;

import jamel.util.Agent;
import jamel.v170804.models.basicModel3.firms.Supplier;

/**
 * Not so good idea ?
 */
@SuppressWarnings("javadoc")
public class Households {

	private static class SupplierComparator implements Comparator<Agent> {

		@Override
		public int compare(Agent o1, Agent o2) {
			final int result;
			final Double s1;
			final Double s2;
			if (o1 != null && ((Supplier) o1).getSupply() != null && ((Supplier) o1).getSupply().getVolume() > 0) {
				s1 = ((Supplier) o1).getSupply().getPrice();
			} else {
				s1 = null;
			}
			if (o2 != null && ((Supplier) o2).getSupply() != null && ((Supplier) o2).getSupply().getVolume() > 0) {
				s2 = ((Supplier) o2).getSupply().getPrice();
			} else {
				s2 = null;
			}
			if (s1 == null && s2 == null) {
				result = 0;
			} else if (s1 == null) {
				result = 1;
			} else if (s2 == null) {
				result = -1;
			} else  {
				result = s1.compareTo(s2);
			} 
			return result;
		}

	}

	final public static SupplierComparator supplierComparator = new SupplierComparator();

}
