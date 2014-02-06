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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jamel.Circuit;
import jamel.JamelObject;
import jamel.agents.firms.Labels;
import jamel.agents.firms.util.Mediator;
import jamel.agents.roles.Worker;
import jamel.util.markets.EmploymentContract;

/**
 * Represents a factory.
 * <p>
 * A factory encapsulates a machinery (= a collection of {@link Machine} objects).
 */
abstract class AbstractFactory extends JamelObject implements jamel.spheres.realSphere.Factory{

	@SuppressWarnings("javadoc")
	protected static final String PARAM_INVENTORY_NORMAL_LEVEL = "Firms.inventories.normalLevel";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_MACHINERY = "Firms.machinery";

	/** 
	 * The machine comparator.<p>
	 * To compare machines and sort them according to the progress of their process of production.
	 */
	public static Comparator<Machine>
	MACHINE_COMPARATOR =
		new Comparator<Machine>() {
		@Override
		public int compare(Machine m1, Machine m2) {
			if (m1.getProgress()>m2.getProgress()) return -1;
			if (m1.getProgress()<m2.getProgress()) return 1;
			if (m1.getProductivity()>m2.getProductivity()) return -1;			
			if (m1.getProductivity()<m2.getProductivity()) return 1;
			return 0;
		}
	};

	/** The inventory volume targeted. */
	private Float inventoryVolumeTarget = null;

	/** The average monthly volume that the factory produces when working at its full capacity. */
	private int maxProduction;

	/** The value of the production of the current period. */
	private long productionValue;

	/** The volume of the production of the current period. */
	private int productionVolume ;

	/** The inventory where the production of the firm is stored. */
	protected Goods finishedGoodsInventory;

	/** The list of machines. */
	protected final LinkedList<Machine> machinery = new LinkedList<Machine>() ;

	/** The mediator. */
	protected Mediator mediator;

	/**
	 * Creates a new factory.
	 * @param mediator  the mediator.
	 */
	public AbstractFactory(Mediator mediator) {
		this.mediator = mediator;
		this.toolUp();
	}
	
	/**
	 * Returns the average theoretical productivity of the factory.
	 * @return a double that represents the average theoretical productivity.
	 */
	@SuppressWarnings("unused")
	private double getProductivity() {
		int count = 0;
		long sum = 0;
		for (Machine machine : machinery) {
			count++;
			sum += machine.getProductivity();
		}
		return sum/count; 
	}

	/**
	 * Tools up the factory with new machines.
	 */
	private void toolUp() {
		final int machines = Integer.parseInt(Circuit.getParameter(PARAM_MACHINERY));
		if (machines == 0) new RuntimeException("The number of machines can't be nul.");
		for (int i = 0; i<machines; i++) {
			machinery.add(newMachine());
		}
	}

	/**
	 * Updates the average monthly volume that the factory produces when working at its full capacity.  
	 */
	private void updateMaxProduction() {
		int production = 0;				
		for (Machine thisMachine : machinery) 
			production += thisMachine.getProductivity();
		this.maxProduction = production;
	}

	/**
	 * Adds a new stock of commodity to the current stock of product.<br>
	 * The new product value is its production cost.
	 * @param product - the commodity to add.
	 */
	protected void addProduct(Goods product) {
		this.productionVolume += product.getVolume();
		this.productionValue += product.getValue();
		this.finishedGoodsInventory.add(product);				
	}
	
	/**
	 * Returns the max level of production according to the current resources of the factory. 
	 * @return a float between 0 and 100.
	 */
	abstract protected float getMaxLevelOfProduction();

	/**
	 * Returns the value of the inventory of raw materials.
	 * Used to calculate the total value of the factory.
	 * @return the value of the inventory of raw materials.
	 */
	protected abstract long getRawMaterialsInventoryValue();

	/**
	 * Returns the volume of the need of raw materials.
	 * @return a volume.
	 */
	abstract protected int getRawMaterialsNeedVolume();

	/**
	 * Return the value of unfinished goods stock within the factory.
	 * @return the value of the unfinished goods.
	 */
	protected long getUnfinishedGoodsValue() {
		long value = 0;
		for (Machine machine : machinery) value += machine.getProductionProcessValue();
		return value; 
	}

	/**
	 * Returns a new machine with the given parameters.
	 * @return a new machine.
	 */
	abstract protected Machine newMachine();

	@Override 
	public void close() {
	}
	
	@Override
	public Object get(String key) {
		Object result = null;
		if (key.equals(Labels.INVENTORIES_TOTAL_VALUE)) {
			result = this.getWorth();
		}
		else if (key.equals(Labels.INVENTORY_LEVEL_RATIO)) {
			result = this.finishedGoodsInventory.getVolume()/(this.inventoryVolumeTarget*this.maxProduction);
		}
		else if (key.equals(Labels.UNIT_COST)) {
			result = this.finishedGoodsInventory.getUnitCost();
		}
		else if (key.equals(Labels.PRODUCTION_LEVEL_MAX)) {
			result = this.getMaxLevelOfProduction();
		}
		else if (key.equals(Labels.INVENTORIES_OF_FINISHED_GOODS)) {
			result = this.finishedGoodsInventory;			
		}
		else if (key.equals(Labels.INVENTORIES_NORMAL_VOLUME)) {
			result = this.inventoryVolumeTarget*this.maxProduction;			
		}
		else if (key.equals(Labels.PRODUCTION_MAX)) {
			result = this.maxProduction;
		}
		else if (key.equals(Labels.INVENTORY_FG_VALUE)) {
			result = this.finishedGoodsInventory.getValue();
		}
		else if (key.equals(Labels.INVENTORY_FG_VOLUME)) {
			result = this.finishedGoodsInventory.getVolume();
		}
		else if (key.equals(Labels.INVENTORY_UG_VALUE)) {
			result = this.getUnfinishedGoodsValue();
		}
		else if (key.equals(Labels.MACHINERY)) {
			result = this.machinery.size();
		}
		else if (key.equals(Labels.RAW_MATERIALS_NEEDS)) {
			result = this.getRawMaterialsNeedVolume();
		}
		else if (key.equals(Labels.PRODUCTION_VALUE)) {
			result = this.productionValue;
		}
		else if (key.equals(Labels.PRODUCTION_VOLUME)) {
			result = this.productionVolume;
		}
		else if (key.equals(Labels.CLOSURE)) {
			this.close();
		}
		else if (key.equals(Labels.OPENING)) {
			this.open();
		}
		return result;
	}

	/**
	 * Returns the total value of the factory.
	 * This total value is the sum of the value of the inventory of finished goods
	 * plus the value of unfinished goods embedded in the processes of production
	 * plus the value of the inventory of raw materials. 
	 * @return a value.
	 */
	@Override
	public long getWorth() {
		return this.finishedGoodsInventory.getValue()+this.getUnfinishedGoodsValue()+this.getRawMaterialsInventoryValue();
	}
	
	/**
	 * Kills the factory.
	 */
	@Override
	public void kill() {
		for (Machine machine : machinery) machine.kill();
		this.machinery.clear();
	}

	/**
	 * Completes some technical operations at the beginning of the period.
	 */
	@Override
	public void open() {
		this.productionVolume = 0;
		this.productionValue = 0;
		updateMaxProduction();
		this.inventoryVolumeTarget  = Float.parseFloat(Circuit.getParameter(PARAM_INVENTORY_NORMAL_LEVEL));
	}

	/**
	 * Production function of the factory.<br>
	 * Summons each employee and makes him work on a machine.
	 */
	@Override
	public void production() {
		@SuppressWarnings("unchecked")
		final List<EmploymentContract> payroll = (List<EmploymentContract>) this.mediator.get(Labels.PAYROLL);
		Collections.sort(this.machinery,MACHINE_COMPARATOR);
		Iterator<Machine> machineIterator=this.machinery.iterator();
		for (EmploymentContract contract : payroll) {
			Worker worker = contract.getEmployee();
			long wage = contract.getWage();
			Machine machine = machineIterator.next();
			machine.work(worker,wage);
		}
	}
		
}










