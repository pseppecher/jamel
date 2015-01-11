package jamel.basic.util;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import jamel.util.Period;
import jamel.util.Timer;

/**
 * A basic timer. 
 */
public class BasicTimer implements Timer {

	/** A time counter for the GUI. */
	@SuppressWarnings("serial")
	private final JTextField counter = new JTextField (5) {{
		this.setHorizontalAlignment(RIGHT);
		this.setEditable(false);
	}};

	/** The current period. */
	private Period current;

	/**
	 * Creates a new timer.
	 * @param t the value of the initial period.
	 */
	public BasicTimer(int t) {
		current = new BasicPeriod(t);
	}

	@Override
	public Component getCounter() {
		return this.counter;
	}

	@Override 
	public Period getPeriod() {
		return this.current;}

	@Override
	public void next() {
		this.current=this.current.getNext();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				counter.setText(""+current.getValue());
			}
		});
	}

}

// ***
