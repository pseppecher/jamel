package jamel.basic.gui;

import jamel.basic.util.TimeListener;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * A time counter.
 */
public class TimeCounter extends JTextField implements TimeListener {
	
	/**
	 * Creates a new time counter.
	 * @param period the initial period.
	 */
	public TimeCounter(int period) {
		super(5);
		this.setHorizontalAlignment(RIGHT);
		this.setEditable(false);
		this.setTime(period);
	}

	@Override
	public void setTime(final int period) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				TimeCounter.this.setText(""+period);
			}
		});		
	}

}

// ***
