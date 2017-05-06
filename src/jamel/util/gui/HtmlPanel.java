package jamel.basic.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JEditorPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultCaret;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import jamel.Jamel;
import jamel.basic.data.Expression;
import jamel.basic.data.ExpressionFactory;
import jamel.basic.data.MacroDatabase;
import jamel.basic.util.InitializationException;

/**
 * A display area for a HTML content.
 */
public class HtmlPanel extends JScrollPane implements Updatable, AdjustmentListener {

	/** The number format. */
	final private static NumberFormat nf = getNumberFormat();

	/**
	 * Creates and returns a new {@link HtmlElement} that provides a dynamic
	 * access to a value of the specified database.
	 * 
	 * @param elem
	 *            an XML element that contains the name of the requested value.
	 * @param macroDatabase
	 *            the database.
	 * @return a new {@link HtmlElement} that provides a dynamic access to a
	 *         value of the specified database.
	 */
	private static HtmlElement getNewDataElement(Element elem, MacroDatabase macroDatabase) {
		final String query = elem.getAttribute("value");
		HtmlElement newDataElement;
		try {

			final Expression exp = ExpressionFactory.newExpression(query, macroDatabase);
			newDataElement = new HtmlElement() {

				@Override
				public String getText() {
					final String result;
					final Double val = exp.value();
					if (val == null) {
						result = "null";
					} else if (val.isNaN()) {
						result = "not a number";
					} else {
						result = nf.format(exp.value());
					}
					return result;
				}

			};
		} catch (Exception e) {
			try {
				throw new InitializationException("Something went wrong while parsing the query: " + query, e);
			} catch (InitializationException e1) {
				e1.printStackTrace();
			}
			newDataElement = new ErrorElement();
		}
		return newDataElement;
	}

	/**
	 * Returns a new error element.
	 * 
	 * @param message
	 *            the error message to display.
	 * @return a new error element.
	 */
	private static HtmlElement getNewErrorElement(final String message) {
		return new HtmlElement() {

			@Override
			public String getText() {
				return "<font color=\"red\">Error: " + message + "</font>";
			}

		};
	}

	/**
	 * Converts the specified XML element into a new HTML element.
	 * 
	 * @param elem
	 *            the XML element to be converted.
	 * @param macroDatabase
	 *            the database.
	 * @return a new {@link HtmlElement}.
	 */
	private static HtmlElement getNewHtmlElement(Element elem, MacroDatabase macroDatabase) {

		final String tagName = elem.getTagName();
		final NodeList list = elem.getChildNodes();
		final NamedNodeMap attributes = elem.getAttributes();
		String attributes1 = "";
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node item = attributes.item(i);
			attributes1 += " " + item.getNodeName() + "=\"" + item.getNodeValue() + "\"";
		}
		final String attributes2 = attributes1;

		final List<HtmlElement> htmlElements = new LinkedList<HtmlElement>();
		for (int i = 0; i < list.getLength(); i++) {
			final HtmlElement htmlElement = parseNode(list.item(i), macroDatabase);
			if (htmlElement != null) {
				htmlElements.add(htmlElement);
			}
		}

		return new HtmlElement() {

			@Override
			public String getText() {
				String result;
				if (htmlElements.size() == 0) {
					result = "<" + tagName + attributes2 + "/>";
				} else {
					result = "<" + tagName + attributes2 + ">";
					for (HtmlElement htmlElement : htmlElements) {
						result += htmlElement.getText();
					}
					result += "</" + tagName + ">";
				}
				return result;
			}

		};
	}

	/**
	 * Creates and returns a new {@link HtmlElement} that provides a dynamic
	 * access to an info of the specified database.
	 * 
	 * @param elem
	 *            an XML element that contains the name of the requested value.
	 * @param macroDatabase
	 *            the database.
	 * @return a new {@link HtmlElement} that provides a dynamic access to a
	 *         value of the specified database.
	 */
	private static HtmlElement getNewInfoElement(Element elem, final MacroDatabase macroDatabase) {
		final String sector = elem.getAttribute("sector");
		final String agent = elem.getAttribute("agent");
		final String period = elem.getAttribute("period");
		final String key = elem.getAttribute("key");

		final int lag;
		if (period.equals("t")) {
			lag = 0;
		} else if (period.startsWith("t-")) {
			lag = Integer.parseInt(period.substring(2));
		} else {
			try {
				throw new InitializationException("This period attribute is malformed: " + period);
			} catch (InitializationException e) {
				e.printStackTrace();
				return new ErrorElement();
			}
		}

		final HtmlElement newInfoElement = new HtmlElement() {

			@Override
			public String getText() {
				final String result;
				result = macroDatabase.getMessage(sector, agent, key, lag);

				return result;
			}

		};

		return newInfoElement;
	}

	/**
	 * Returns an html element that contains this Jamel version id.
	 * 
	 * @return an html element that contains this Jamel version id.
	 */
	private static HtmlElement getNewJamelVersionElement() {
		final String result = Jamel.getVersion();
		return new HtmlElement() {

			@Override
			public String getText() {
				return result;
			}

		};
	}

	/**
	 * Creates and returns a new {@link HtmlElement} that provides a dynamic
	 * test on two values of the specified database.
	 * 
	 * @param elem
	 *            an XML element that contains the test.
	 * @param macroDatabase
	 *            the database.
	 * @return a new {@link HtmlElement} that provides a dynamic result of the
	 *         test.
	 */
	private static HtmlElement getNewTestElement(Element elem, final MacroDatabase macroDatabase) {
		final String q1 = elem.getAttribute("val1");
		final String q2 = elem.getAttribute("val2");
		final String label = elem.getAttribute("label");
		HtmlElement newHtmlElement;
		try {

			final Expression exp1 = ExpressionFactory.newExpression(q1, macroDatabase);
			final Expression exp2 = ExpressionFactory.newExpression(q2, macroDatabase);
			newHtmlElement = new HtmlElement() {

				private int errors = 0;

				@Override
				public String getText() {
					final String result;
					final Double val1 = exp1.value();
					final Double val2 = exp2.value();
					if (val1 != null && val2 != null && !val1.equals(val2)) {
						errors++;
						Jamel.println();
						Jamel.println("Test " + label + " failed when t=" + macroDatabase.getPeriod());
						Jamel.println(exp1.getQuery() + " = " + exp1.value());
						Jamel.println(exp2.getQuery() + " = " + exp2.value());
					}

					if (errors == 0) {
						result = "<font color=\"green\">ok</font>";
					} else {
						result = "<font color=\"red\">" + errors + " errors</font>";
					}
					return result;
				}

			};
		} catch (Exception e) {
			try {
				throw new InitializationException("Something went wrong while parsing the test: ", e);
			} catch (InitializationException e1) {
				e1.printStackTrace();
			}
			newHtmlElement = getNewErrorElement("Error while parsing the test. See log file for more details.");
		}
		return newHtmlElement;
	}

	/**
	 * Converts the specified XML node into an HTML text element.
	 * 
	 * @param node
	 *            the XML node to be converted.
	 * @return an HTML text element.
	 */
	private static HtmlElement getNewTextElement(Node node) {
		final Text text = (Text) node;
		final String result = text.getWholeText();
		return new HtmlElement() {

			@Override
			public String getText() {
				return result;
			}

		};
	}

	/**
	 * Returns the number format.
	 * 
	 * @return the number format.
	 */
	private static NumberFormat getNumberFormat() {
		final NumberFormat result = NumberFormat.getInstance(Locale.US);
		result.setMaximumFractionDigits(1);
		return result;
	}

	/**
	 * Parses the node and returns a new {@link HtmlElement}.
	 * 
	 * @param node
	 *            the description of the HTML element to be created.
	 * @param macroDatabase
	 *            the database.
	 * @return a new {@link HtmlElement}.
	 */
	private static HtmlElement parseNode(final Node node, MacroDatabase macroDatabase) {
		final HtmlElement result;
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			final Element elem = (Element) node;
			if (elem.getNodeName().equals("data")) {
				result = getNewDataElement(elem, macroDatabase);
			} else if (elem.getNodeName().equals("test")) {
				result = getNewTestElement(elem, macroDatabase);
			} else if (elem.getNodeName().equals("info")) {
				result = getNewInfoElement(elem, macroDatabase);
			} else if (elem.getNodeName().equals("jamel.version")) {
				result = getNewJamelVersionElement();
			} else {
				result = getNewHtmlElement(elem, macroDatabase);
			}
		} else if (node.getNodeType() == Node.TEXT_NODE) {
			result = getNewTextElement(node);
		} else if (node.getNodeType() == Node.COMMENT_NODE) {
			result = null;
		} else {
			result = getNewErrorElement("Unexpected node type: " + node.getNodeType());
		}
		return result;
	}

	/**
	 * Returns an html panel with an error message.
	 * 
	 * @param errorMessage
	 *            the error message to be displayed.
	 * @return an html panel with an error message.
	 */
	public static Component getErrorPanel(String errorMessage) {
		return new HtmlPanel(getNewErrorElement(errorMessage));
	}

	/**
	 * Returns a new HTML panel.
	 * 
	 * @param elem
	 *            an XML element that contains the description of the panel to
	 *            create.
	 * @param macroDatabase
	 *            the database.
	 * @param file
	 *            the parent file.
	 * @return a new HTML panel.
	 * @throws InitializationException
	 *             if something went wrong.
	 */
	public static HtmlPanel getNewHtmlPanel(Element elem, MacroDatabase macroDatabase, File file)
			throws InitializationException {
		final HtmlPanel result;
		final String source = elem.getAttribute("source");
		if (source.equals("")) {
			result = new HtmlPanel(elem, macroDatabase);
		} else {
			final String path = file.getParent();
			final File sourceFile = new File(path + "/" + source);
			final Element root;
			try {
				root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(sourceFile).getDocumentElement();
			} catch (Exception e) {
				throw new InitializationException("Something goes wrong while creating the html panel.", e);
			}
			if (!"html".equals(root.getNodeName())) {
				throw new InitializationException("The root node of the file must be named <html>.");
			}

			result = new HtmlPanel(root, macroDatabase);
		}
		return result;
	}

	/**
	 * If the scroll bar has to be adjusted.
	 */
	private boolean adjustScrollBar = true;

	/**
	 * The content of the panel.
	 */
	private final HtmlElement htmlElement;

	/**
	 * The component that displays the text.
	 */
	private final JEditorPane jEditorPane;

	/**
	 * Previous maximum value of the scroll bar.
	 */
	private int previousMaximum = -1;

	/**
	 * Previous value of the scroll bar.
	 */
	private int previousValue = -1;

	/**
	 * The text displayed by the panel.
	 */
	private String text = null;

	/**
	 * Creates a new panel.
	 * 
	 * @param elem
	 *            an XML element that contains the description of the content to
	 *            be displayed by the panel.
	 * @param database
	 *            the database that contains the data to be displayed.
	 */
	private HtmlPanel(Element elem, MacroDatabase database) {
		this(parseNode(elem, database));
	}

	/**
	 * Creates a new panel.
	 * 
	 * @param htmlElement
	 *            the content of the panel.
	 */
	private HtmlPanel(HtmlElement htmlElement) {
		super();
		this.htmlElement = htmlElement;
		this.jEditorPane = new JEditorPane();
		jEditorPane.setContentType("text/html");
		jEditorPane.setText(htmlElement.getText());
		jEditorPane.setEditable(false);
		jEditorPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
					try {
						java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
			}
		});
		this.setPreferredSize(new Dimension(660, 400));
		final Border border = BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(JamelColor.getColor("background"), 5),
				BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		this.setBorder(border);
		this.setViewportView(jEditorPane);

		// Smart scrolling

		final DefaultCaret caret = (DefaultCaret) jEditorPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		this.getVerticalScrollBar().addAdjustmentListener(this);
	}

	/**
	 * Smart scrolling.
	 * Inspired by Rob Camick on March 3, 2013
	 * https://tips4java.wordpress.com/2013/03/03/smart-scrolling/
	 */
	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// The scroll bar listModel contains information needed to
				// determine
				// whether the viewport should be repositioned or not.

				final JScrollBar scrollBar = (JScrollBar) e.getSource();
				final BoundedRangeModel listModel = scrollBar.getModel();
				int value = listModel.getValue();
				final int extent = listModel.getExtent();
				final int maximum = listModel.getMaximum();

				final boolean valueChanged = previousValue != value;
				final boolean maximumChanged = previousMaximum != maximum;

				// Check if the user has manually repositioned the scrollbar

				if (valueChanged && !maximumChanged) {
					adjustScrollBar = value + extent >= maximum;
				}

				// Reset the "value" so we can reposition the viewport and
				// distinguish between a user scroll and a program scroll.
				// (ie. valueChanged will be false on a program scroll)

				if (adjustScrollBar) {
					// Scroll the viewport to the end.
					scrollBar.removeAdjustmentListener(HtmlPanel.this);
					value = maximum - extent;
					scrollBar.setValue(value);
					scrollBar.addAdjustmentListener(HtmlPanel.this);
				}

				previousValue = value;
				previousMaximum = maximum;
			}
		});
	}

	@Override
	public void update() {
		final String newText = htmlElement.getText();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (!newText.equals(text)) {
					text = newText;
					jEditorPane.setText(newText);
					revalidate();
					repaint();
				}
			}
		});
	}

}

// ***
