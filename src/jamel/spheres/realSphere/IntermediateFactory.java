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
 * Represents a factory that produces intermediate goods.
 */
public class IntermediateFactory extends AbstractFactory {

	/**
	 * Represents a machine of the final goods sector.
	 */
	private class IntermediateMachine extends AbstractMachine {

		/**
		 * Represents a production process.
		 */
		private class Process extends AbstractProductionProcess {
			
			/**
			 * The product of the process.
			 */
			private class Product extends IntermediateGoods {
				
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
			
			/**
			 * Creates a new production process.
			 */
			private Process() {
				super(IntermediateMachine.this.productivity,IntermediateMachine.this.productionTime);
			}
		
			/**
			 * Increments the production process.<br>
			 * The labor power is expended().
			 * If the process is completed, a new volume of commodities is created.<br>
			 * Generates an exception if the process has been already called in the current period
			 * or if the process is already completed or canceled.
			 * @param laborPower - the labor power to expend.
			 */
			public void progress(LaborPower laborPower) {
				super.progress(laborPower);
				if (isCompleted()) {
					IntermediateFactory.this.addProduct(new Product());
					process = null;
				}
			}
		
		}

		/**
		 * Creates a new machine with the given productivity.
		 * @param productivity the productivity.
		 * @param productionTime the production cycle time.
		 */
		public IntermediateMachine(int productivity, int productionTime) {
			super(productivity, productionTime);
		}

		/**
		 * Returns a new production process.
		 * @return the new process.
		 */
		@Override
		public ProductionProcess newProductionProcess() {
			return new Process();
		}
		
	}

	/**
	 * Creates a new factory.
	 * @param parameters - the parameters.
	 */
	public IntermediateFactory(Blackboard parameters) {
		super(parameters);
		this.finishedGoodsInventory = new IntermediateGoods();
	}

	/**
	 * Returns a map that contains the default parameters for this factory.
	 * @return a map<String,String>.
	 */
	@Override
	protected Map<String, Object> getDefaultParameters() {
		final Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put(Labels.PARAM_FACTORY_MACHINES, "10");		
		map2.put(Labels.PARAM_FACTORY_PROD_MIN, "100");		
		map2.put(Labels.PARAM_FACTORY_PROD_MAX, "100");		
		map2.put(Labels.PARAM_FACTORY_PRODUCTION_TIME, "4");
		return map2;
	}

	/**
	 * Returns the max level of production according to the current resources of the factory. 
	 * @return 100.
	 */
	@Override
	protected float getMaxLevelOfProduction() {
		return 100;
	}

	/**
	 * Returns 0 (this factory does not hold raw materials).
	 * @return 0.
	 */
	@Override
	protected long getRawMaterialsInventoryValue() {
		return 0;
	}

	/**
	 * Returns 0, cause a factory of the sector of intermediate goods does not require raw materials. 
	 * @return 0 
	 */
	@Override
	protected int getRawMaterialsNeedVolume() {
		return 0;
	}

	/**
	 * Returns a new machine that can produce intermediate goods.
	 * @return a new machine.
	 */
	@Override
	protected Machine newMachine(int productivity, int productionTime) {
		return new IntermediateMachine(productivity, productionTime);
	}
	
}










