package jamel.basic.util;

/**
 * The listener interface for receiving time events. 
 * The class that is interested in processing a time event implements this interface, and the object created with that class is registered with a timer, using the timer's <code>addTimeListener</code> method.
 * When the time event occurs, that object's <code>setTime</code> method is invoked. 
 */
public interface TimeListener {
	
	/**
	 * Invoked when a time event occurs.
	 * @param period the new period.
	 */
	void setTime(int period);

}

// ***
