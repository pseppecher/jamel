package jamel.jamel.firms.util;

import jamel.basic.util.Timer;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.LaborPower;

/**
 * Represents a basic factory.
 */
public class BasicFactory implements Factory {

	/** A flag that indicates either the factory is closed or not. */
	protected boolean closed = false;

	/** The inventory stock of finished goods. */
	protected final Commodities finishedGoods= new BasicCommodities();

	/** The value of inventory losses (when the firm goes bankrupt). */
	protected long inventoryLosses=0;
	
	/** The value of finished goods produced in the last production process. */
	protected long productionValue;

	/** The volume of finished goods produced in the last production process. */
	protected long productionVolume;
	
	/** The number of labor powers put in the last production process. */
	protected int workforce;

	/** The work in progress. */
	protected final WorkInProgress workInProgress;

	/**
	 * Creates a basic factory.
	 * @param productionTime the production time.
	 * @param capacity the capacity of production.
	 * @param productivity the productivity. 
	 * @param timer the timer.
	 */
	public BasicFactory(int productionTime, int capacity, float productivity, Timer timer) {
		this.workInProgress = newWorkInProgress(productionTime, capacity, productivity, timer);
	}

	/**
	 * Returns the average productivity since the beginning of the factory.
	 * @return the average productivity.
	 */
	protected float getAverageProductivity() {
		return this.workInProgress.getAverageProductivity();
	}

	/**
	 * Returns the value of the goods at the specified stage.
	 * @param stage the stage.
	 * @return the value.
	 */
	protected double getValueAt(int stage) {
		return this.workInProgress.getValueAt(stage);
	}	

	/**
	 * Returns the volume of the goods at the specified stage.
	 * @param stage the stage.
	 * @return the volume.
	 */
	protected double getVolumeAt(int stage) {
		return this.workInProgress.getVolumeAt(stage);
	}

	/**
	 * Creates and returns a new WorkInProgress object.
	 * @param productionTime the production time.
	 * @param capacity the capacity of production.
	 * @param productivity the productivity. 
	 * @param timer the timer.
	 * @return a new WorkInProgress object.
	 */
	protected WorkInProgress newWorkInProgress(int productionTime, int capacity, float productivity, Timer timer) {
		return new BasicWorkInProgress(productionTime, capacity, productivity, timer);
	}

	@Override
	public void bankrupt() {
		if (this.closed) {
			throw new RuntimeException("This factory is already closed.");
		}
		this.inventoryLosses=this.getValue();
		this.finishedGoods.consume();
		this.workInProgress.cancel();
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
	public void investment(InvestmentProcess investmentProcess) {
		this.workInProgress.investment(investmentProcess);
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
			this.productionValue=0;
		}
	}

	@Override
	public void scrap(double threshold) {
		throw new RuntimeException("Not yet implemented");
	}

}

// ***
