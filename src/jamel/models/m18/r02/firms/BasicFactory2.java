package jamel.models.m18.r02.firms;

import jamel.Jamel;
import jamel.models.util.Commodities;
import jamel.util.Parameters;
import jamel.util.Simulation;

/**
 * A factory for {@link BasicFirm2}.
 * 
 * 2018-03-12: jamel.models.m18.r01.firms.BasicFactory2
 * 
 */
class BasicFactory2 extends BasicFactory {

	/**
	 * Stock de biens d'investissement destiné aux futures machines.
	 */
	private final Commodities inputForNewMachines;

	/**
	 * Creates a new basic factory.
	 * 
	 * @param parameters
	 *            the parameters of the firm.
	 * @param simulation
	 *            the parent simulation.
	 */
	public BasicFactory2(final Parameters parameters, final Simulation simulation) {
		super(parameters, simulation);
		this.inputForNewMachines = new BasicGoods(this.getQualityOfInputForTheCreationOfANewMachine());
	}

	/**
	 * Expands the capacity.
	 * 
	 * @param input
	 *            the input that will be consumed in the process of machine
	 *            creation.
	 */
	public void expandCapacity(Commodities input) {
		this.inputForNewMachines.add(input);
		if (this.inputForNewMachines.getVolume() >= getInputVolumeForANewMachine()) {
			final int size = (int) (this.inputForNewMachines.getVolume() / getInputVolumeForANewMachine());
			super.expandCapacity(size, this.inputForNewMachines);
		}
	}

	@Override
	public void expandCapacity(int size, Commodities input) {
		Jamel.notUsed();
		// Cette methode ne doit pas être directement appelée.
	}

	/**
	 * Returns the volume of investment goods in inventories available as input
	 * for the creation of new machines.
	 * 
	 * @return the volume of investment goods in inventories available as input
	 *         for the creation of new machines.
	 */
	public long getInputVolume() {
		return this.inputForNewMachines.getVolume();
	}

	@Override
	public long getValue() {
		return super.getValue() + this.inputForNewMachines.getValue();
	}

}
