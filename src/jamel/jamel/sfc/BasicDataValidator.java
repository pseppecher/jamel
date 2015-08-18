package jamel.jamel.sfc;

import jamel.basic.data.Expression;
import jamel.basic.data.MacroDatabase;
import jamel.basic.util.InitializationException;
import jamel.basic.util.Timer;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A basic data validator. TODO: create a Test object to encapsulate conditions
 * and results. TODO: clean-up, refactor, comment, validate !
 */
public class BasicDataValidator implements DataValidator {

	/** A light green color. */
	private static final Color LIGHT_GREEN = new Color(0.8f, 1f, 0.8f);

	/** A light red color. */
	private static final Color LIGHT_RED = new Color(1f, 0.8f, 0.8f);

	/** A blue color. */
	private static final Color BLUE = new Color(0.4f, 0.4f, 1f);

	/** The line separator. */
	final private static String rc = System.getProperty("line.separator");

	/** Titles of the columns of the JTable. */
	final private static String tableTitles[] = { "Test", "Label", "Value 1",
			"", "Value 2", "Result" };

	@SuppressWarnings("javadoc")
	public static final String LABEL = "label";

	@SuppressWarnings("javadoc")
	public static final String TEST = "test";

	@SuppressWarnings("javadoc")
	public static final String VAL1 = "val1";

	@SuppressWarnings("javadoc")
	public static final String VAL2 = "val2";

	/** The overall panel that displays all the results. */
	final private Component dataValidatorPanel;

	/** A text panel to display the result of the tests. */
	final private JEditorPane failurePanel = new JEditorPane() {
		{
			setContentType("text/html");
			setEditable(false);
			setText("No failure.");
		}
	};

	/** A string buffer to store test failure declarations. */
	final private StringBuffer failures = new StringBuffer("Failure Trace:<br>"
			+ rc);

	/** An array to store the labels of the tests. */
	final private String[] labels;

	/** The name of the data validator. */
	private String name;

	/** An array to store the result of each test. */
	final private Boolean[] results;

	/** When the tests starts. */
	private int start = 0;

	/** The table model to display results of the tests. */
	private final AbstractTableModel tableModel = new AbstractTableModel() {

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public String getColumnName(int col) {
			return tableTitles[col];
		}

		@Override
		public int getRowCount() {
			return results.length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			final Object result;
			switch (col) {
			case 0:
				result = row;
				break;
			case 1:
				result = labels[row];
				break;
			case 2:
				result = tests[row][0].toString();
				break;
			case 3:
				result = "=";
				break;
			case 4:
				result = tests[row][1].toString();
				break;
			case 5:
				if (results[row] == null) {
					result = "n/a";
				} else {
					result = results[row];
				}
				break;
			default:
				result = "value";
			}
			return result;
		}
	};

	/** An array to store, for each test, the two values to compare. */
	final private Expression[][] tests;

	/** The timer. */
	private final Timer timer;

	/**
	 * Creates a new data validator.
	 * 
	 * @param file
	 *            the XML file that contains the description of the tests.
	 * @param timer
	 *            the timer.
	 * @param macroDatabase
	 *            the dataset.
	 * @throws InitializationException
	 *             If something goes wrong. TODO: devrait recevoir l'Žlement
	 *             root et non un fichier.
	 */
	public BasicDataValidator(File file, Timer timer,
			MacroDatabase macroDatabase) throws InitializationException {
		this.timer = timer;
		final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(file);
			final Element root = document.getDocumentElement();
			if (!"validation".equals(root.getNodeName())) {
				throw new ParserConfigurationException(
						"The root node of the scenario file must be named <validation>.");
			}
			this.name = root.getAttribute("title");
			final String content = root.getAttribute("content");
			if (!"".equals(content)) {
			}
			final String start = root.getAttribute("start");
			if (!"".equals(start)) {
				this.start = Integer.parseInt(start);
			}
			final NodeList testNodeList = root.getElementsByTagName(TEST);
			this.tests = new Expression[testNodeList.getLength()][];
			this.labels = new String[testNodeList.getLength()];
			this.results = new Boolean[tests.length];
			for (int i = 0; i < testNodeList.getLength(); i++) {
				final String label = ((Element) testNodeList.item(i))
						.getAttribute(LABEL);
				final Expression[] item = new Expression[2];
				final String a = ((Element) testNodeList.item(i))
						.getAttribute(VAL1);
				final String b = ((Element) testNodeList.item(i))
						.getAttribute(VAL2);
				item[0] = macroDatabase.newQuery(a);
				item[1] = macroDatabase.newQuery(b);
				this.labels[i] = label;
				this.tests[i] = item;
				this.results[i] = null;
			}
		} catch (final Exception e) {
			throw new InitializationException(
					"Something went wrong while parsing the file: " + file, e);
		}
		dataValidatorPanel = getDataValidatorPanel();
		if (!"".equals(name)) {
			this.dataValidatorPanel.setName(name);
		}
	}

	/**
	 * Creates and returns a new panel that displays the results of the tests.
	 * 
	 * @return a new panel that displays the results of the tests.
	 */
	private Component getDataValidatorPanel() {

		final Component jTable = new JTable(tableModel) {
			{
				this.getColumnModel().getColumn(0).setMaxWidth(30);
				this.getColumnModel().getColumn(1).setPreferredWidth(40);
				this.getColumnModel().getColumn(2).setPreferredWidth(40);
				this.getColumnModel().getColumn(3).setMaxWidth(10);
				this.getColumnModel().getColumn(4).setPreferredWidth(800);
				this.getColumnModel().getColumn(5).setMaxWidth(40);
				this.setGridColor(Color.LIGHT_GRAY);
				TableCellRenderer renderer = new DefaultTableCellRenderer() {

					@Override
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						final Component cell = super
								.getTableCellRendererComponent(table, value,
										isSelected, hasFocus, row, column);
						if (isRowSelected(row)) {
							cell.setBackground(BLUE);
							cell.setForeground(Color.WHITE);
						} else if (results[row] == null) {
							cell.setBackground(Color.WHITE);
							cell.setForeground(Color.BLACK);
						} else if (!results[row]) {
							cell.setBackground(LIGHT_RED);
							cell.setForeground(Color.BLACK);
						} else {
							cell.setBackground(LIGHT_GREEN);
							cell.setForeground(Color.BLACK);
						}
						return cell;
					}
				};
				this.setDefaultRenderer(Object.class, renderer);
			}

		};
		final JScrollPane scrollPane = new JScrollPane(jTable);
		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				scrollPane, new JScrollPane(failurePanel)) {
			{
				this.setResizeWeight(0.5);
				this.setDividerLocation(0.5);
				this.setDividerSize(2);
			}
		}

		;
		return splitPane;
	}

	@Override
	public boolean checkConsistency() {
		boolean success = true;
		boolean dataChanged = false;
		if (timer.getPeriod().intValue() > start) {
			for (int i = 0; i < tests.length; i++) {
				final Double a = tests[i][0].value();
				final Double b = tests[i][1].value();
				final boolean singleTestResult;
				if ((a != null && !a.equals(b)) || a == null || b == null) {
					singleTestResult = false;
					success = false;
					failures.append("Period " + timer.getPeriod().intValue()
							+ ", test " + i + ": failure (" + a + ", " + b
							+ ")<br>" + rc);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							failurePanel.setText(failures.toString());
						}
					});
				} else {
					singleTestResult = true;
				}
				if (results[i] == null
						|| (!singleTestResult && singleTestResult != results[i])) {
					results[i] = singleTestResult;
					dataChanged = true;
				}
			}
			if (dataChanged) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						tableModel.fireTableDataChanged();
					}
				});
			}
		}
		return success;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Component getPanel() {
		return dataValidatorPanel;
	}

}

// ***
