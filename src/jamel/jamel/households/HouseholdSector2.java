package jamel.jamel.households;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jamel.basic.Circuit;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.Phase;
import jamel.basic.util.InitializationException;
import jamel.jamel.aggregates.Employers;
import jamel.jamel.widgets.JobOffer;

/**
 * TODO: WORK IN PR0GRESS 27-11-2015 Un secteur des ménages qui admet plusieurs
 * secteurs employeurs. Devrait fusionner à terme avec BasicHouseholdSector.
 */
public class HouseholdSector2 extends BasicHouseholdSector {

	/**
	 * A class to manage multiple employers.
	 */
	private class MultiSectoralEmployers implements Employers {

		/**
		 * The employer sectors.
		 */
		private final Map<String, Employers> sectors = new LinkedHashMap<String, Employers>();

		/**
		 * The total size (= the total of potential jobs).
		 */
		private int totalSize = 0;

		/**
		 * The weight of the each sector.
		 */
		private final Map<String, Integer> weight = new HashMap<String, Integer>();

		@Override
		public JobOffer[] getJobOffers(final int size) {
			final JobOffer[] result;
			if (this.sectors.isEmpty()) {
				throw new RuntimeException("There is no employer sector.");
			}
			final List<JobOffer> offers = new LinkedList<JobOffer>();			
			for(Entry<String,Employers> entry: this.sectors.entrySet()) {
				final int n = Math.max(1,size*this.weight.get(entry.getKey())/totalSize);
				final JobOffer[] offer = entry.getValue().getJobOffers(n);
				for(JobOffer jobOffer: offer) {
					offers.add(jobOffer);
				}
			}
			Collections.shuffle(offers,random);
			if (offers.size()>size) {
				result = offers.subList(0, size).toArray(new JobOffer[0]);
			}
			else {
				result = offers.toArray(new JobOffer[0]);
			}
			return result;
		}

		@Override
		public int getSize() {
			return this.totalSize;
		}

		/**
		 * Returns <tt>true</tt> if this record contains no employer.
		 *
		 * @return <tt>true</tt> if this record contains no employer.
		 */
		public boolean isEmpty() {
			return this.sectors.isEmpty();
		}

		/**
		 * Associates the specified employer with the specified key in this map.
		 *
		 * @param key
		 *            key with which the specified employer is to be associated.
		 * @param employer
		 *            employer to be associated with the specified key.
		 */
		public void register(String key, Employers employer) {
			if (key == null) {
				throw new IllegalArgumentException("Key cannot be null.");
			}
			if (employer == null) {
				throw new IllegalArgumentException("Employer cannot be null.");
			}
			if (this.sectors.containsKey(key)) {
				throw new IllegalArgumentException("This employer is already registred.");
			}
			this.sectors.put(key, employer);
			this.weight.put(key, 1);
			this.totalSize++;
		}

		/**
		 * Updates the weight of each sector.
		 */
		public void updateWeight() {
			this.totalSize = 0;
			for (Entry<String, Employers> entry : sectors.entrySet()) {
				final String key = entry.getKey();
				final Employers employer = entry.getValue();
				final int size = employer.getSize();
				this.weight.put(key, size);
				this.totalSize+=size;
			}
		}

	}

	/**
	 * Creates a new sector for households.
	 * 
	 * @param name
	 *            the name of the sector.
	 * @param circuit
	 *            the circuit.
	 */
	public HouseholdSector2(String name, Circuit circuit) {
		super(name, circuit);
	}

	/**
	 * Creates and returns a closure phase.
	 * 
	 * @return a closure phase.
	 */
	@Override
	protected Phase getClosurePhase() {
		return new AbstractPhase(PHASE_CLOSURE, this) {
			@Override
			public void run() {
				for (final Household household : households.getList()) {
					household.close();
				}
				((MultiSectoralEmployers) employers).updateWeight();
			}
		};
	}

	/**
	 * Initializes the employer sectors.
	 * 
	 * @param list
	 *            the list of the employers.
	 * @throws InitializationException
	 *             if something goes wrong.
	 */
	@Override
	protected void initEmployers(NodeList list) throws InitializationException {
		final MultiSectoralEmployers multiEmployers = new MultiSectoralEmployers();
		for (int i = 0; i < list.getLength(); i++) {
			final Element element = (Element) list.item(i);
			final String key = element.getAttribute("value");
			final Employers newEmployer = (Employers) circuit.getSector(key);
			if (newEmployer == null) {
				throw new InitializationException("Employers sector not found: " + key);
			}
			multiEmployers.register(key, newEmployer);
		}
		
		if (multiEmployers.isEmpty()) {
			// TODO: Faut-il obligatoirement des employeurs ? Par exemple un
			// secteur de ménages capitalistes ne devrait pas avoir besoin
			// d'employeurs.
			// On devrait donc accepter qu'il n'y en n'ait pas.
			// A décider sans doute en amont de cette procédure.
			throw new InitializationException("Employers not found.");
		}
		this.employers = multiEmployers;
	}

}

// ***
