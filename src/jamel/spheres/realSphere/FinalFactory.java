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


import jamel.agents.firms.Labels;
import jamel.util.Blackboard;

import java.util.HashMap;
import java.util.Map;

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
				super(FinalMachine.this.productivity,FinalMachine.this.productionTime);
				this.intermediateConsumption = (int) (FinalMachine.this.coefficient*FinalMachine.this.productivity);
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

		/** The technical coefficient (the volume of intermediate good consumed by the production of 1 unit of final good). */
		final private float coefficient;

		/**
		 * Creates a new machine with the given productivity.
		 * @param aProductivity the productivity.
		 * @param aTime the production cycle time.
		 * @param aCoefficient the volume of intermediate good required to produce 1 unit of final good.
		 */
		public FinalMachine(int aProductivity, int aTime, float aCoefficient) {
			super(aProductivity, aTime);
			this.coefficient = aCoefficient;
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
	 * @param parameters - the list of parameters.
	 */
	public FinalFactory(Blackboard parameters) {
		super(parameters);
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
			sum += ((FinalMachine)machine).coefficient*machine.getProductivity();   
		}
		return sum;
	}

	/**
	 * Returns a HashMap that contains the default parameters for this factory.
	 * @return a HashMap.
	 */
	@Override
	protected Map<String, Object> getDefaultParameters() {
		final Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put(Labels.PARAM_FACTORY_MACHINES, 10);		
		map2.put(Labels.PARAM_FACTORY_PROD_MIN, 100);		
		map2.put(Labels.PARAM_FACTORY_PROD_MAX, 100);		
		map2.put(Labels.PARAM_FACTORY_PRODUCTION_TIME, 4);
		map2.put("coefficient", 100f);
		return map2;
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
	protected Machine newMachine(int productivity, int productionTime) {
		float coefficient=0;
		coefficient = (Float)this.blackboard.get(Labels.TECH_COEFF);
		return new FinalMachine(productivity, productionTime,coefficient);
	}

	/**
	 * Takes the raw materials required for the production process.
	 */
	public void takeRawMaterials() {
		final IntermediateGoods rawMaterials=(IntermediateGoods) this.blackboard.remove(Labels.RAW_MATERIALS);
		this.rawMaterialsInventory.add(rawMaterials);
		this.blackboard.put(Labels.RAW_MATERIALS_VOLUME,this.rawMaterialsInventory.getVolume());
	}

}





