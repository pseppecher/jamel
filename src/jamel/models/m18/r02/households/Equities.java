package jamel.models.m18.r02.households;

import java.util.LinkedHashMap;
import java.util.Map;

import jamel.Jamel;
import jamel.models.m18.r02.util.Equity;

/**
 * Represents a portfolio of equities.
 * 
 * 2018-02-16: m18.q04.households.Equities
 * Pour permettre aux shareholders de connaître leur richesse.
 */
@SuppressWarnings("javadoc")
public class Equities {

	private Map<String, Equity> titles = new LinkedHashMap<>();

	public void add(Equity title) {
		if (titles.containsKey(title.getOwner().getName()) || titles.containsValue(title)) {
			Jamel.println("***");
			Jamel.println("title.getOwner(): " + title.getOwner().getName());
			Jamel.println("titles.containsKey(title.getOwner()): " + titles.containsKey(title.getOwner().getName()));
			Jamel.println("titles.containsValue(title): " + titles.containsValue(title));
			Jamel.println();
			throw new RuntimeException("Inconsistency");
		}
		titles.put(title.getCompanyName(), title);
	}

	public long getValue() {
		long result=0;
		for (Equity title : titles.values()) {
			result+= title.getValue();
		}
		return result;
	}

}
