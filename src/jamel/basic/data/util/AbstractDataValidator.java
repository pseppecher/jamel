package jamel.basic.data.util;

import jamel.util.Circuit;
import jamel.util.FileParser;

import java.awt.Component;
import java.io.FileNotFoundException;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

@SuppressWarnings("javadoc")
public abstract class AbstractDataValidator implements DataValidator {
	
	/** The line separator. */
	final private static String rc = System.getProperty("line.separator");

	final private StringBuffer failures = new StringBuffer("<br>"+rc);
	
	@SuppressWarnings("serial")
	final private JEditorPane jEditorPane = new JEditorPane(){{setContentType("text/html");setEditable(false);}};

	final private Boolean[] results;
		
	final private String style = "<STYLE TYPE=\"text/css\"> body {font-family:sans-serif; font-size:12pt} div.ok {color:green} div.failure {color:red}</STYLE>";
	
	@SuppressWarnings("serial") 
	final private Component testPanel = new JScrollPane(jEditorPane) {{setName("Tests");}};

	final private String[][] tests;
	
	public AbstractDataValidator(String validationConfigFileName) throws FileNotFoundException {
		List<String> lines = null;
		lines = FileParser.parseList(validationConfigFileName);
		if (lines!=null) {
			tests = new String[lines.size()][];
			results = new Boolean[tests.length];
			int i = 0;
			for(String line:lines) {
				tests[i] = FileParser.toArray(line);
				results[i] = true;
				i++;
			}
		}
		else {
			tests = new String[0][];
			results = new Boolean[0];
		}
		updatePanelContent();
	}

	private String getContent() {
		final StringBuffer content = new StringBuffer(style);
		content.append("<H3>Data Consistency Validation</H3>"+rc);
		for (int i=0; i<tests.length; i++) {
			final String truc;
			if (results[i]) {
				truc="ok";
			}
			else {
				truc="failure";
			}
			content.append("<div class='"+truc+"'>Test "+i+ " ("+tests[i][0]+"="+tests[i][1]+"): "+truc+"</div>"+rc);
		}
		content.append(failures);
		return content.toString();
	}

	private void updatePanelContent() {
		final String text=getContent();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jEditorPane.setText(text);
				jEditorPane.setCaretPosition(jEditorPane.getDocument().getLength());
			}
		});				
	}

	@Override public boolean CheckConsistency() {
		boolean success = true;
		for(int i=0; i<tests.length; i++) {
			final Double a = get(tests[i][0]);
			final Double b = get(tests[i][1]);
			if ((a!=null && !a.equals(b)) || (a==null && b!=null)) {
				success=false;
				failures.append("Period="+Circuit.getCurrentPeriod().getValue()+", failure: "+tests[i][0]+"="+a+", "+tests[i][1]+"="+b+"<br>"+rc);
				results[i]=false;
			}
		}
		if (!success) {
			this.updatePanelContent();
		}
		return success;
	}

	public abstract Double get(String string);

	@Override public Component getPanel() {
		return testPanel;
	}
	

}
