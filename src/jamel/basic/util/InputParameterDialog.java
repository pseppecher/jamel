package jamel.basic.util;

import javax.swing.JOptionPane;

/**
 * A dialog requesting input from the user.
 * Used when a parameter is missing.
 * 
 * @author pascal
 *
 */
public class InputParameterDialog implements Runnable {

	/**
	 * Key of the missing parameter.
	 */
	private final String key;

	/**
	 * The <code>String</code> to display in the dialog title bar
	 */
	private final String title;

	/**
	 * The value entered by the user.
	 */
	private String value = null;

	/**
	 * Creates a new input dialog.
	 * 
	 * @param title
	 *            the <code>String</code> to display in the dialog title bar.
	 * @param key
	 *            the key for the missing parameter.
	 */
	public InputParameterDialog(String title, String key) {
		this.title = title;
		this.key = key;
	}

	/**
	 * Returns the user input (or <code>null</code>).
	 * 
	 * @return the user input (or <code>null</code>).
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Shows a dialog requesting input from the user.
	 */
	@Override
	public void run() {
		this.value = JOptionPane.showInputDialog(null, "Missing parameter: " + key, title,
				JOptionPane.QUESTION_MESSAGE);
	}

}

// ***
