package jamelV3.jamel.firms.util;

import jamelV3.basic.util.Period;
import jamelV3.basic.util.Timer;
import jamelV3.jamel.util.AnachronismException;
import jamelV3.jamel.widgets.Commodities;
import jamelV3.jamel.widgets.LaborPower;

/**
 * A basic implementation of the <code>WorkInProgress</code> interface.
 * This object encapsulates the production process.
 */
public class BasicWorkInProgress implements WorkInProgress {

	/** The capacity. */
	protected int capacity;

	/** The date of the last call of the process method. */
	protected Period lastUse;

	/** Statistic of the volume of the production (finished goods) since the creation of the process. */
	protected long production=0;

	/** The production time, ie the number of stages of the production process. */
	protected final int productionTime;

	/** The productivity. */
	protected float productivity;

	/** A flag that indicates either the process is closed or not. */
	protected boolean terminated = false;

	/** The timer. */
	final protected Timer timer;

	/** Number of labor powers expended for this process since the creation of the process. */
	protected int totalLaborPowers=0;

	/** The list of values of the different unfinished goods. */
	protected final long[] value;

	/** The list of volumes of the different unfinished goods. */
	protected final long[] volume;

	/**
	 * Creates a new work-in-process inventory.
	 * @param productionTime the production time.
	 * @param capacity the production capacity.
	 * @param productivity the productivity.
	 * @param timer the timer.
	 */
	public BasicWorkInProgress(int productionTime, int capacity, float productivity, Timer timer) {
		this.productionTime = productionTime;
		this.capacity = capacity;
		this.productivity = productivity;
		this.value = new long[productionTime];
		this.volume = new long[productionTime];
		this.timer  = timer;
	}

	@Override
	public void cancel() {
		this.terminated  = true;
		for (int i=0; i<value.length; i++) {
			value[i]=0;
			volume[i]=0;			
		}
	}

	/* (non-Javadoc)
	 * @see jamel.basic.agents.firms.util.WorkInProgress#getAverageProductivity()
	 */
	@Override
	public float getAverageProductivity() {
		return ((float)this.production)/this.totalLaborPowers;
	}

	/* (non-Javadoc)
	 * @see jamel.basic.agents.firms.util.WorkInProgress#getCapacity()
	 */
	@Override
	public float getCapacity() {
		return this.capacity;
	}

	/* (non-Javadoc)
	 * @see jamel.basic.agents.firms.util.WorkInProgress#getMaxUtilAverageProduction()
	 */
	@Override
	public float getMaxUtilAverageProduction() {
		return this.capacity*this.productivity;
	}

	/* (non-Javadoc)
	 * @see jamel.basic.agents.firms.util.WorkInProgress#getProductivity()
	 */
	@Override
	public float getProductivity() {
		return productivity;
	}

	/* (non-Javadoc)
	 * @see jamel.basic.agents.firms.util.WorkInProgress#getValue()
	 */
	@Override
	public long getValue() {
		long sum=0;
		for (long val:value){
			sum+=val;
		}
		return sum;
	}

	/* (non-Javadoc)
	 * @see jamel.basic.agents.firms.util.WorkInProgress#getValueAt(int)
	 */
	@Override
	public long getValueAt(int index) {
		return this.value[index-1];
	}

	/* (non-Javadoc)
	 * @see jamel.basic.agents.firms.util.WorkInProgress#getVolumeAt(int)
	 */
	@Override
	public long getVolumeAt(int index) {
		return this.volume[index-1];
	}

	@Override
	public void investment(InvestmentProcess investmentProcess) {
		if (this.terminated) {
			throw new IllegalArgumentException("The investissment process is terminated.");
		}
		if (investmentProcess.getProductivity()!=this.productivity) {
			throw new RuntimeException("Not yet implemented.");
		}
		if (!investmentProcess.isComplete()) {
			throw new IllegalArgumentException("The investissment process is not complete.");
		}
		this.capacity++;
		investmentProcess.cancel();
	}

	/* (non-Javadoc)
	 * @see jamel.basic.agents.firms.util.WorkInProgress#process(jamel.basic.agents.util.LaborPower)
	 */
	@Override
	public Commodities process(LaborPower... laborPowers) {
		if (this.terminated) {
			throw new RuntimeException("This process is definitively closed.");
		}
		if (this.lastUse==null) {
			this.lastUse = this.timer.getPeriod();
		}
		else if (!this.lastUse.isBefore(this.timer.getPeriod())) {
			throw new AnachronismException("");
		}
		if (laborPowers.length>this.capacity) {
			throw new IllegalArgumentException("Over capacity: laborPowers.length is "+laborPowers.length+" but capacity is "+this.capacity);
		}
		if (laborPowers.length==0) {
			throw new IllegalArgumentException("Workforce is empty.");
		}
		final long[] value2 = new long[productionTime];
		final long[] volume2 = new long[productionTime];
		int index=productionTime-1;
		for(LaborPower laborPower:laborPowers) {
			this.totalLaborPowers++;
			while(!laborPower.isExhausted()) {
				if (index==0) {
					volume2[0]+=this.productionTime*this.productivity*laborPower.getEnergy();
					value2[0]+=laborPower.getValue();
					laborPower.expend();
				}
				else {
					if (volume[index-1]!=0) {
						long pVolume = (long) (this.productionTime*this.productivity*laborPower.getEnergy());
						if (pVolume==volume[index-1]) {
							value2[index]+=value[index-1]+laborPower.getValue();
							volume2[index]+=volume[index-1];
							laborPower.expend();
							value[index-1]=0;
							volume[index-1]=0;
						}
						else if (pVolume<volume[index-1]) {
							final long pValue=value[index-1]*pVolume/volume[index-1];
							value2[index]+=pValue+laborPower.getValue();
							volume2[index]+=pVolume;
							laborPower.expend();
							value[index-1]-=pValue;
							volume[index-1]-=pVolume;
						}
						else {
							final float work=(volume[index-1])/(this.productionTime*this.productivity);
							final long lValue=(long) (laborPower.getWage()*work);
							value2[index]+=value[index-1]+lValue;
							volume2[index]+=volume[index-1];
							laborPower.expend(work);
							value[index-1]=0;
							volume[index-1]=0;
						}
					}
				}
				index--;
				if (index<0) {
					index=productionTime-1;
				}
			}
		}
		// Consolidation.
		for (index=0; index<productionTime; index++) {
			value[index]+=value2[index];
			volume[index]+=volume2[index];
		}
		return new BasicCommodities() {{
			this.setValue(value[productionTime-1]);
			this.setVolume(volume[productionTime-1]);
			value[productionTime-1]=0;
			volume[productionTime-1]=0;
			production+=this.getVolume();
		}};
	}

}

// ***
