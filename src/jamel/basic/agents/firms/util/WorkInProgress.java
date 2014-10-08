package jamel.basic.agents.firms.util;

import jamel.basic.agents.util.LaborPower;
import jamel.basic.util.AnachronismException;
import jamel.basic.util.Commodities;
import jamel.util.Circuit;
import jamel.util.Period;

/**
 * Represents the work-in-progress inventory.
 * This object encapsulates the production process.
 */
class WorkInProgress {

	/** The capacity. */
	final private int capacity;

	/** The date of the last call of the process method. */
	private Period lastUse;

	/** Statistic of calls of the process() method since the creation of the process. */
	private int process=0;

	/** Statistic of the volume of the production (finished goods) since the creation of the process. */
	private long production=0;

	/** The production time, ie the number of stages of the production process. */
	final private int productionTime;

	/** The productivity. */
	private float productivity;

	/** The list of values of the different unfinished goods. */
	final private long[] value;

	/** The list of volumes of the different unfinished goods. */
	final private long[] volume;

	/** A flag that indicates either the process is closed or not. */
	private boolean closed = false;

	/**
	 * Creates a new work-in-process inventory.
	 * @param productionTime the production time.
	 * @param capacity the production capacity.
	 * @param productivity the productivity.
	 */
	WorkInProgress(int productionTime, int capacity, float productivity) {
		this.productionTime = productionTime;
		this.capacity = capacity;
		this.productivity = productivity;
		this.value=new long[productionTime];
		this.volume=new long[productionTime];	
	}

	/**
	 * Returns the total value of the work-in-progress inventory. 
	 * @return the value.
	 */
	long getValue() {
		long sum=0;
		for (long val:value){
			sum+=val;
		}
		return sum;
	}

	/**
	 * Returns the value of the unfinished goods at the given stage of the production process.
	 * @param index the stage.
	 * @return the value.
	 */
	long getValueAt(int index) {
		return this.value[index-1];
	}

	/**
	 * Returns the volume of the unfinished goods at the given stage of the production process.
	 * @param index the stage.
	 * @return the value.
	 */
	long getVolumeAt(int index) {
		return this.volume[index-1];
	}

	/**
	 * Prints a description of the object.
	 * For debugging purposes. 
	 */
	void print() {
		System.out.println(this);
		System.out.println(this.capacity);
		System.out.println(this.productionTime);
		System.out.println(this.productivity);
		for (int i=0;i<this.productionTime;i++) {
			System.out.println(i+", "+this.volume[i]+", "+this.value[i]);			
		}
		System.out.println();
	}

	/**
	 * Produces new goods by the expense of the given labor powers.
	 * @param laborPowers the labor powers.
	 * @return the product.
	 */
	Commodities process(LaborPower... laborPowers) {
		if (this.closed) {
			throw new RuntimeException("This process is definitively closed.");
		}
		if (this.lastUse==null) {
			this.lastUse=Circuit.getCurrentPeriod();
		}
		else if (!this.lastUse.isBefore(Circuit.getCurrentPeriod())) {
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
			this.process++;
			while(!laborPower.isExhausted()) {
				if (index==0) {
					volume2[0]+=this.productionTime*this.productivity*laborPower.getEnergy();
					value2[0]+=laborPower.getValue();
					laborPower.expend();
				}
				else {
					if (volume[index-1]==0) {
						//index--;
					}
					else {
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
							final float work=((float)volume[index-1])/(this.productionTime*this.productivity);
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

	/**
	 * Returns the ex-post average productivity.
	 * @return the average productivity.
	 */
	public float getAverageProductivity() {
		return ((float)this.production)/this.process;
	}

	/**
	 * Returns the capacity.
	 * @return the capacity.
	 */
	public float getCapacity() {
		return this.capacity;
	}

	/**
	 * Returns the ex-ante average production at full utilization of the production capacity. 
	 * @return the average production.
	 */
	public long getMaxUtilAverageProduction() {
		return (long) (this.capacity*this.productivity);
	}

	/**
	 * Returns the productivity.
	 * @return the productivity.
	 */
	public float getProductivity() {
		return productivity;
	}

	/**
	 * Sets the productivity.
	 * @param productivity the productivity to set.
	 */
	public void setProductivity(float productivity) {
		if (this.closed) {
			throw new RuntimeException("This process is definitively closed.");
		}
		this.productivity=productivity;
	}

	/**
	 * Definitively closes the the process.
	 */
	public void close() {
		this.closed  = true;
	}

}
