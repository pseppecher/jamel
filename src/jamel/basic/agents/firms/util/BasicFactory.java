package jamel.basic.agents.firms.util;

import jamel.basic.agents.util.LaborPower;
import jamel.basic.util.Commodities;
import jamel.manhattan.InvestmentProcess;

/**
 * Represents a basic factory.
 */
public class BasicFactory implements Factory {

	/** A flag that indicates either the factory is closed or not. */
	private boolean closed = false;

	/** The inventory stock of finished goods. */
	private final Commodities finishedGoods= new BasicCommodities();

	/** The value of inventory losses (when the firm goes bankrupt). */
	private long inventoryLosses=0;
	
	/** The value of finished goods produced in the last production process. */
	private long productionValue;

	/** The volume of finished goods produced in the last production process. */
	private long productionVolume;
	
	/** The number of labor powers put in the last production process. */
	private int workforce;

	/** The work in progress. */
	private final WorkInProgress workInProgress;

	/**
	 * Creates a basic factory.
	 * @param productionTime the production time.
	 * @param capacity the capacity of production.
	 * @param productivity the productivity. 
	 */
	public BasicFactory(int productionTime, int capacity, float productivity) {
		this.workInProgress = newWorkInProgress(productionTime, capacity, productivity);
	}

	/**
	 * Creates and returns a new WorkInProgress object.
	 * @param productionTime the production time.
	 * @param capacity the capacity of production.
	 * @param productivity the productivity. 
	 * @return a new WorkInProgress object.
	 * @since 23-11-2014
	 */
	protected WorkInProgress newWorkInProgress(int productionTime, int capacity, float productivity) {
		return new BasicWorkInProgress(productionTime, capacity, productivity);
	}

	/**
	 * Returns the average productivity since the beginning of the factory.
	 * @return the average productivity.
	 */
	float getAverageProductivity() {
		return this.workInProgress.getAverageProductivity();
	}	

	/**
	 * Returns the value of the goods at the specified stage.
	 * @param stage the stage.
	 * @return the value.
	 */
	double getValueAt(int stage) {
		return this.workInProgress.getValueAt(stage);
	}

	/**
	 * Returns the volume of the goods at the specified stage.
	 * @param stage the stage.
	 * @return the volume.
	 */
	double getVolumeAt(int stage) {
		return this.workInProgress.getVolumeAt(stage);
	}

	@Override
	public void bankrupt() {
		if (this.closed) {
			throw new RuntimeException("This factory is already closed.");
		}
		this.inventoryLosses=this.getValue();
		this.finishedGoods.consume();
		this.workInProgress.close();
		this.closed = true;
	}

	@Override
	public float getCapacity() {
		return this.workInProgress.getCapacity();
	}

	@Override
	public Commodities getCommodities(long demand) {
		if (this.closed) {
			throw new RuntimeException("This factory is definitively closed.");
		}
		return this.finishedGoods.detach(demand);
	}

	@Override
	public long getFinishedGoodsValue() {
		return this.finishedGoods.getValue();
	}

	@Override
	public long getFinishedGoodsVolume() {
		return this.finishedGoods.getVolume();
	}

	@Override
	public double getGoodsInProcessValue() {
		return this.workInProgress.getValue();
	}

	@Override
	public double getInventoryLosses() {
		return this.inventoryLosses;
	}

	@Override
	public double getMaxUtilAverageProduction() {
		return this.workInProgress.getMaxUtilAverageProduction();
	}

	@Override
	public long getProductionValue() {
		return this.productionValue;
	}

	@Override
	public long getProductionVolume() {
		return this.productionVolume;
	}

	/**
	 * Returns the productivity.
	 * @return the productivity.
	 */
	@Override
	public double getProductivity() {
		return this.workInProgress.getProductivity();
	}

	@Override
	public double getUnitCost() {
		return this.finishedGoods.getUnitCost();
	}

	@Override
	public long getValue() {
		return this.workInProgress.getValue()+this.finishedGoods.getValue();
	}

	@Override
	public int getWorkforce(){
		return this.workforce;
	}

	@Override
	public void process(final LaborPower... laborPowers) {
		if (this.closed) {
			throw new RuntimeException("This factory is definitively closed.");
		}
		if (laborPowers.length>0) {
			this.workforce=laborPowers.length;
			final Commodities product = this.workInProgress.process(laborPowers);
			this.productionVolume = product.getVolume();
			this.productionValue = product.getValue();
			this.finishedGoods.put(product);
		}
		else {
			this.workforce=0;
			this.productionVolume=0;
		}
	}

	/**
	 * Sets the productivity.
	 * @param productivity the productivity to set.
	 */
	@Override
	public void setProductivity(float productivity) {
		if (this.closed) {
			throw new RuntimeException("This factory is definitively closed.");
		}
		this.workInProgress.setProductivity(productivity);
	}

	@Override
	public void investment(InvestmentProcess investmentProcess) {
		this.workInProgress.investment(investmentProcess);
	}

}

// ***
