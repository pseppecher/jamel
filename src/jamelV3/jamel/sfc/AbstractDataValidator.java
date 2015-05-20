package jamelV3.jamel.sfc;

import jamelV3.basic.util.InitializationException;
import jamelV3.basic.util.Timer;

import java.awt.Component;
import java.io.File;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An abstract data validator.
 */
public abstract class AbstractDataValidator implements DataValidator {

	/**
	 * A convenient static class to store String constants. 
	 */
	@SuppressWarnings("javadoc")
	public static class KEY {
		public static final String TEST = "test";
		public static final String VAL1 = "val1";
		public static final String VAL2 = "val2";
	}

	/** The line separator. */
	final private static String rc = System.getProperty("line.separator");

	/** The style for the text panel. */
	final private static String textPanelStyle = "<STYLE TYPE=\"text/css\"> body {font-family:sans-serif; font-size:12pt} div.ok {color:green} div.failure {color:red}</STYLE>";

	/** A string buffer to store test failure declarations. */
	final private StringBuffer failures = new StringBuffer("<br>"+rc);

	/** A text panel to display the result of the tests. */
	final private JEditorPane resultPanel = new JEditorPane(){
		{
			setContentType("text/html");
			setEditable(false);
		}
	};

	/** An array to store the result of each test. */
	final private Boolean[] results;

	/** A scroll panel to display the result panel */
	final private Component testPanel = new JScrollPane(resultPanel) {{setName("Tests");}};

	/** An array to store, for each test, the two values to compare. */
	final private String[][] tests;

	/** The timer. */
	private final Timer timer;

	/**
	 * Creates a new data validator.
	 * @param file the XML file that contains the description of the tests.
	 * @param timer the timer.
	 * @throws InitializationException If something goes wrong.
	 */
	public AbstractDataValidator(File file, Timer timer) throws InitializationException {
		this.timer=timer;
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(file);
			final Element root = document.getDocumentElement();
			if (!"validation".equals(root.getNodeName())) {
				throw new ParserConfigurationException("The root node of the scenario file must be named <validation>.");
			}
			final NodeList testNodeList = root.getElementsByTagName(KEY.TEST);
			this.tests = new String[testNodeList.getLength()][];
			this.results = new Boolean[tests.length];
			for (int i = 0; i<testNodeList.getLength(); i++) {
				final String[] item = new String[2];
				item[0]= ((Element) testNodeList.item(i)).getAttribute(KEY.VAL1);
				item[1]= ((Element) testNodeList.item(i)).getAttribute(KEY.VAL2);
				this.tests[i]=item;
				this.results[i] = true;
			}
		}
		catch (final Exception e) {
			throw new InitializationException("Something went wrong with the file "+file,e);
		}
		updateResultPanel();
	}

	/**
	 * Returns a string representation of the results of the tests.
	 * @return a string representation of the results of the tests.
	 */
	private String getTestResults() {
		final StringBuffer content = new StringBuffer(textPanelStyle);
		content.append("<H3>Data Consistency Validation</H3>"+rc);
		for (int i=0; i<tests.length; i++) {
			final String result;
			if (results[i]) {
				result="ok";
			}
			else {
				result="failure";
			}
			content.append("<div class='"+result+"'>Test "+i+ ": "+result+" ("+tests[i][0]+" = "+tests[i][1]+")</div>"+rc);
		}
		content.append(failures);
		return content.toString();
	}

	/**
	 * Updates the content of the result panel. 
	 */
	private void updateResultPanel() {
		final String text=getTestResults();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				resultPanel.setText(text);
				resultPanel.getDocument().getLength();
			}
		});				
	}

	@Override public boolean CheckConsistency() {
		boolean success = true;
		for(int i=0; i<tests.length; i++) {
			final Double a = getValue(tests[i][0]);
			final Double b = getValue(tests[i][1]);
			if ((a!=null && !a.equals(b)) || (a==null && b!=null)) {
				success=false;
				failures.append("Period "+timer.getPeriod().intValue()+", test "+i+": failure ("+a+", "+b+")<br>"+rc);
				results[i]=false;
			}
		}
		if (!success) {
			this.updateResultPanel();
		}
		return success;
	}

	@Override public Component getPanel() {
		return testPanel;
	}

	/**
	 * Returns the specified value of the simulation data.  
	 * @param string the key of the value to return.
	 * @return the specified value.
	 */
	public abstract Double getValue(String string);

}

// ***
