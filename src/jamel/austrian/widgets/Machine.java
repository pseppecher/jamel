

package jamel.austrian.widgets;

import jamel.basic.util.Timer;



/**
 * Represents a machine.
 * <p> 
 * A Machine is a capital good which is not fully used up in a production process, i.e.
 * it can be used for a given number of time periods.<br>
 * Machines enhance the processing capacities of workers.
 * */
public class Machine implements Comparable<Machine>  {
	
	/** The timer. */
	private final Timer timer;

	/** The last period in which the machine can be used.*/
	private final int expiryDate;
	
	/** The processing capacity of the machine.*/
	private final int capacity;


	/**
	 * Creates a new machine with the given durability.
	 */
	public Machine(int machineSize, int capK, int durability, Timer timer) {
		this.timer = timer;
		this.capacity = (int) Math.pow(capK, machineSize);  
		this.expiryDate = timer.getPeriod().intValue() + durability;
	}
	
	
	/**
	 * @return the capacity of the machine.
	 */
	public int getCapacity() {
		return capacity;
	}

	
	/** Indicates whether the machine can still be used.<br>
	 *  Machines can be used in the time period in which they are purchased, but they cannot be 
	 *  used in the time period in which they expire.*/
	public boolean isExpired(){
		return (timer.getPeriod().intValue() >= expiryDate);
	}
	
	
	/**
	 * Makes machines comparable.
	 */
	public int compareTo(Machine m){
		if (this.capacity<m.capacity) return 1;
		if (this.capacity>m.capacity) return -1;
		return 0;
	}
}
	

