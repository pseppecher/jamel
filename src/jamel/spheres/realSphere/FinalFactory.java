/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/javadoc/index.html>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JAMEL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.spheres.realSphere;


import jamel.Circuit;
import jamel.agents.firms.Labels;
import jamel.agents.firms.util.Mediator;

/**
 * Represents a factory that produces final goods.
 */
public class FinalFactory extends AbstractFactory {

	/**
	 * Represents a machine of the intermediate goods sector.
	 */
	public class FinalMachine extends AbstractMachine {

		/**
		 * Represents a production process.
		 * <p>
		 * A production process is incremented by the expenditure of a {@link LaborPower}.
		 * When the production process is completed, a new nonempty {@link FinalGoods} object is created.
		 */
		private class Process extends AbstractProductionProcess {

			/**
			 * The product of the process.
			 */
			private class Product extends FinalGoods {

				/**
				 * Creates a nonempty volume of commodities from a production-process.
				 */
				private Product() {
					if (!Process.this.isCompleted()) 
						new RuntimeException("Production process not completed."); 
					this.setVolume((int) (Process.this.getProgress()*Process.this.getProductivity()));
					this.setValue(Process.this.getValue());
					Process.this.cancel();
					if (this.getVolume()<=0) new RuntimeException("The product volume negative or null."); 
					if (this.getValue()<=0) new RuntimeException("The product value negative or null.");
				}

			}

			/** The volume of intermediate goods consumed by a step of the process. */
			final private int intermediateConsumption;

			/**
			 * Creates a new production process.
			 */
			private Process() {
				super(FinalMachine.this.getProductivity(),FinalMachine.this.getProductionTime());
				this.intermediateConsumption = (int) (getRequiredVolumeOfRawMaterials()*FinalMachine.this.getProductivity());
			}

			/**
			 * Increments the production process.<br>
			 * The labor power is expended().
			 * If the process is completed, a new volume of commodities is created.<br>
			 * Generates an exception if the process has been already called in the current period
			 * or if the process is already completed or canceled.
			 * @param laborPower - the labor power to expend.
			 */
			@Override
			public void progress(LaborPower laborPower) {
				final IntermediateGoods input = (IntermediateGoods) rawMaterialsInventory.newGoods(this.intermediateConsumption);
				this.addValue(input.getValue());
				input.consumption();
				super.progress(laborPower);
				if (isCompleted()) {
					FinalFactory.this.addProduct(new Product());
					process = null;
				}
			}	

		}

		@SuppressWarnings("javadoc")
		private static final String PARAM_TECHNICAL_COEF = "Firms.technicalCoefficient";

		/**
		 * Returns the technical coefficient.
		 * @return the technical coefficient.
		 */
		private float getCoefficient() {
			return Float.parseFloat(Circuit.getParameter(PARAM_TECHNICAL_COEF));
		}

		/**
		 * Returns the volume of raw materials required for one month of production.
		 * @return the volume of raw materials required for one month of production.
		 */
		private float getRequiredVolumeOfRawMaterials() {
			return getCoefficient()*getProductivity();
		}

		/**
		 * Returns a new production process.
		 * @return the new process.
		 */
		@Override
		protected ProductionProcess newProductionProcess() {
			return new Process();
		}

	}

	/** The raw materials inventory. */
	final private IntermediateGoods rawMaterialsInventory;

	/**
	 * Creates a new factory.
	 * @param mediator  the mediator.
	 */
	public FinalFactory(Mediator mediator) {
		super(mediator);
		this.finishedGoodsInventory = new FinalGoods();
		this.rawMaterialsInventory=new IntermediateGoods();
	}

	/**
	 * Returns the level of the raw materials inventory (with respect to the normal volume). 
	 * @return the level of the raw material inventory.
	 */
	private float getRawMaterialsInventoryRate() {
		return this.rawMaterialsInventory.getVolume()/this.getRequiredVolumeOfRawMaterials();
	}

	/**
	 * Returns the volume of raw material required for one period of production at full capacity.
	 * @return the required volume of raw material.
	 */
	private int getRequiredVolumeOfRawMaterials() {
		int sum = 0;
		for (Machine machine: this.machinery) {
			sum += ((FinalMachine)machine).getRequiredVolumeOfRawMaterials();   
		}
		return sum;
	}

	/**
	 * Returns the max level of production according to the current resources of the factory. 
	 * @return a float between 0 and 100.
	 */
	@Override
	protected float getMaxLevelOfProduction() {
		float maxLevel = 100f*this.getRawMaterialsInventoryRate();
		if (maxLevel>100) maxLevel= 100;
		return maxLevel;
	}

	/**
	 * Returns the value of the inventory of raw materials.
	 * @return the value of the inventory of raw materials.
	 */
	@Override
	protected long getRawMaterialsInventoryValue() {
		return this.rawMaterialsInventory.getValue();
	}

	/**
	 * Returns the volume of the need of raw materials.
	 * @return the volume of the need of raw materials.
	 */
	@Override
	protected int getRawMaterialsNeedVolume() {
		int need = 2*this.getRequiredVolumeOfRawMaterials()-this.rawMaterialsInventory.getVolume();
		if (need<0) 
			need=0;
		need = Math.min(need, this.getRequiredVolumeOfRawMaterials());
		return need;
	}

	@Override
	protected Machine newMachine() {
		return new FinalMachine();
	}

	@Override
	public Object get(String key) {
		Object result = null;
		if (key.equals(Labels.RAW_MATERIALS_VOLUME)) {
			result = this.rawMaterialsInventory.getVolume();
		}
		else {
			result = super.get(key);			
		}
		return result;
	}
	
	/**
	 * Takes the raw materials required for the production process.
	 */
	public void takeRawMaterials() {
		final IntermediateGoods rawMaterials=(IntermediateGoods) this.mediator.get(Labels.RAW_MATERIALS);
		this.rawMaterialsInventory.add(rawMaterials);
	}

}





