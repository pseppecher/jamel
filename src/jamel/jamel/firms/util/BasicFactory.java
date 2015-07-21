package jamel.jamel.firms.util;

import jamel.basic.agent.AgentDataset;
import jamel.basic.agent.BasicAgentDataset;
import jamel.basic.util.Timer;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.LaborPower;

/**
 * Represents a basic factory.
 */
public class BasicFactory implements Factory {

	/** The factory dataset. */
	private AgentDataset dataset;

	/** A flag that indicates either the factory is closed or not. */
	protected boolean closed = false;

	/** The inventory stock of finished goods. */
	protected final Commodities finishedGoods= new BasicCommodities();
	
	/** The value of inventory losses (when the firm goes bankrupt). */
	protected long inventoryLosses=0;

	/** The value of finished goods produced in the last production process. */
	protected long productionValue;
	
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
	public void close() {
		this.dataset.put("inventories.inProcess.val", (double) this.workInProgress.getValue());
		this.dataset.put("inventories.fg.val", (double) this.finishedGoods.getValue());
		this.dataset.put("inventories.fg.vol", (double) this.finishedGoods.getVolume());
		this.dataset.put("inventories.losses.val", (double) inventoryLosses);
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
	public AgentDataset getData() {
		return this.dataset;
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
	public double getUnitCost() {
		return this.finishedGoods.getUnitCost();
	}

	@Override
	public long getValue() {
		return this.workInProgress.getValue()+this.finishedGoods.getValue();
	}

	@Override
	public void investment(InvestmentProcess investmentProcess) {
		this.workInProgress.investment(investmentProcess);
	}

	@Override
	public void open() {
		this.dataset=new BasicAgentDataset("Factory");
	}

	@Override
	public void process(final LaborPower... laborPowers) {
		if (this.closed) {
			throw new RuntimeException("This factory is definitively closed.");
		}
		long productionVolume;
		if (laborPowers.length>0) {
			this.workforce=laborPowers.length;
			final Commodities product = this.workInProgress.process(laborPowers);
			productionVolume = product.getVolume();
			this.productionValue = product.getValue();
			this.finishedGoods.put(product);
		}
		else {
			this.workforce=0;
			productionVolume=0;
			this.productionValue=0;
		}
		this.dataset.put("workforce", (double) this.workforce);
		this.dataset.put("production.vol", (double) productionVolume);
		this.dataset.put("production.val", (double) this.productionValue);
		this.dataset.put("productivity", (double) this.workInProgress.getProductivity());
		this.dataset.put("capacity", (double) this.workInProgress.getCapacity());
	}

	@Override
	public void scrap(double threshold) {
		throw new RuntimeException("Not yet implemented");
	}

}

// ***
