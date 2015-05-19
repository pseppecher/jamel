package jamelV3.basic.util;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * A basic timer. 
 */
public class BasicTimer implements Timer {

	/**
	 * An implementation of the Period interface.
	 */
	private class BasicPeriod implements Period {

		/** The value of the period. */
		private final Integer value;
		
		/**
		 * Creates a period.
		 * @param t the value of the new period. 
		 */
		public BasicPeriod(int t) {
			this.value = t;
		}

		@Override
		public int compareTo(Period arg0) {
			return value.compareTo(arg0.intValue());
		}

		@Override
		public boolean equals(int t) {
			return this.value==t;
		}

		@Override
		public boolean equals(Period p) {
			return this.value==p.intValue();
		}

		/**
		 * Returns the next period.
		 * @return a period.
		 */
		@Override
		public Period getNext() {
			return new BasicPeriod(this.value+1);
		}

		@Override
		public int intValue() {
			return this.value;
		}

		@Override
		public boolean isAfter(int t) {
			return this.value>t;
		}

		@Override
		public boolean isAfter(Period period2) {
			return this.value>period2.intValue();
		}

		@Override
		public boolean isBefore(int t) {
			return this.value<t;
		}

		@Override
		public boolean isBefore(Period p) {
			return this.value<p.intValue();
		}

		@Override
		public boolean isPresent() {
			return this.equals(current);
		}

		@Override
		public Period plus(int term) {
			return new BasicPeriod(this.value+term);
		}
		
	}

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

	/**
	 * Returns the graphical counter.
	 * @return the graphical counter.
	 */
	public Component getCounter() {
		return this.counter;
	}

	@Override 
	public Period getPeriod() {
		return this.current;}

	/**
	 * Changes to the next period.
	 */
	public void next() {
		this.current=this.current.getNext();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				counter.setText(""+current.intValue());
			}
		});
	}

}

// ***
