package jamel.jamel.firms;

import jamel.basic.Circuit;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.Phase;

/**
 * Un secteur expérimental pour des firmes qui investissent.
 * WORK IN PROGRESS 16-09-2015
 * TODO: comment
 */
public class IndustrialSector150908 extends BasicIndustrialSector {

	/**
	 * The inputs purchase phase: when firms buy raw materials and other inputs.
	 */
	@Override
	protected void inputsPurchase() {
		for (final Firm firm : firms.getShuffledList()) {
			firm.inputsPurchase();
		}
	}

	/**
	 * Creates a new industrial sector.
	 * 
	 * @param name
	 *            the name of the sector.
	 * @param circuit
	 *            the circuit.
	 */
	public IndustrialSector150908(String name, Circuit circuit) {
		super(name, circuit);
	}
	
	@Override
	public Phase getPhase(String phaseName) {
		final Phase newPhase;
		if ("investment".equals(phaseName)) {
			newPhase = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					for (final Firm firm : firms.getShuffledList()) {
						((Investor) firm).invest();
					}
				}
			};
		} else {
			newPhase = super.getPhase(phaseName);			
		}
		return newPhase;
	}

}

// ***

