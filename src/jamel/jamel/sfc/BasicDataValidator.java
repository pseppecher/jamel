package jamel.jamel.sfc;

import jamel.basic.data.Expression;
import jamel.basic.data.ExpressionFactory;
import jamel.basic.data.MacroDatabase;
import jamel.basic.util.InitializationException;
import jamel.basic.util.Timer;

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
 * A basic data validator.
 * TODO: create a Test object to encapsulate conditions and results.
 * TODO: clean-up, refactor, comment, validate !
 */
public class BasicDataValidator implements DataValidator {

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

	/** A string that describes the content of the panel. */
	private String content = "Data Consistency Validation";

	/** A string buffer to store test failure declarations. */
	final private StringBuffer failures = new StringBuffer("<br>"+rc);

	/** The name of the data validator.*/
	private String name;

	/** A text panel to display the result of the tests. */
	final private JEditorPane resultPanel = new JEditorPane(){
		{
			setContentType("text/html");
			setEditable(false);
		}
	};

	/** An array to store the result of each test. */
	final private Boolean[] results;

	/** When the tests starts. */
	private int start = 0;

	/** A scroll panel to display the result panel */
	final private Component testPanel = new JScrollPane(resultPanel) {{setName("Tests");}};

	/** An array to store, for each test, the two values to compare. */
	final private Expression[][] tests;

	/** The timer. */
	private final Timer timer;

	/**
	 * Creates a new data validator.
	 * @param file the XML file that contains the description of the tests.
	 * @param timer the timer.
	 * @param macroDatabase the dataset. 
	 * @throws InitializationException If something goes wrong.
	 * TODO: devrait recevoir l'Žlement root et non un fichier.
	 */
	public BasicDataValidator(File file, Timer timer, MacroDatabase macroDatabase) throws InitializationException {
		this.timer=timer;
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(file);
			final Element root = document.getDocumentElement();
			if (!"validation".equals(root.getNodeName())) {
				throw new ParserConfigurationException("The root node of the scenario file must be named <validation>.");
			}
			this.name = root.getAttribute("title");
			if (!"".equals(name)) {
				this.testPanel.setName(name);				
			}
			final String content = root.getAttribute("content");
			if (!"".equals(content)) {
				this.content=content;
			}
			final String start = root.getAttribute("start");
			if (!"".equals(start)) {
				this.start =Integer.parseInt(start);
			}
			final NodeList testNodeList = root.getElementsByTagName(KEY.TEST);
			this.tests = new Expression[testNodeList.getLength()][];
			this.results = new Boolean[tests.length];
			for (int i = 0; i<testNodeList.getLength(); i++) {
				final Expression[] item = new Expression[2];
				final String a = ((Element) testNodeList.item(i)).getAttribute(KEY.VAL1);
				final String b = ((Element) testNodeList.item(i)).getAttribute(KEY.VAL2);
				item[0]= ExpressionFactory.newExpression(a, macroDatabase);
				item[1]= ExpressionFactory.newExpression(b, macroDatabase);
				this.tests[i]=item;
				this.results[i] = true;
			}
		}
		catch (final Exception e) {
			throw new InitializationException("Something went wrong while parsing the file: "+file,e);
		}
		updateResultPanel();
	}

	/**
	 * Returns a string representation of the results of the tests.
	 * @return a string representation of the results of the tests.
	 */
	private String getTestResults() {
		final StringBuffer content = new StringBuffer(textPanelStyle);
		if (this.content !=null) {
			content.append("<H3>"+this.content+"</H3>"+rc);			
		}
		if (timer.getPeriod().intValue()>start) {
			for (int i=0; i<tests.length; i++) {
				final String result;
				if (results[i]) {
					result="ok";
				}
				else {
					result="failure";
				}
				content.append("<div class='"+result+"'>Test "+i+ ": "+result+" ("+tests[i][0].toString()+" = "+tests[i][1].toString()+")</div>"+rc);
			}
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

	@Override public boolean checkConsistency() {
		boolean success = true;
		if (timer.getPeriod().intValue()>start) {
			for(int i=0; i<tests.length; i++) {
				final Double a = tests[i][0].value();
				final Double b = tests[i][1].value();
				if ((a!=null && !a.equals(b)) || (a==null && b!=null)) {
					success=false;
					failures.append("Period "+timer.getPeriod().intValue()+", test "+i+": failure ("+a+", "+b+")<br>"+rc);
					results[i]=false;
				}
			}
			if (timer.getPeriod().intValue()>start || !success) {
				this.updateResultPanel();
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
		return testPanel;
	}
	
}

// ***
