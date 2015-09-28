package jamel.basic.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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
public class HtmlPanel extends JScrollPane implements Updatable {

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
			htmlElements.add(htmlElement);
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
			} else if (elem.getNodeName().equals("info")) {
				result = getNewInfoElement(elem, macroDatabase);
			} else if (elem.getNodeName().equals("jamel.version")) {
				result = getNewJamelVersionElement();
			} else {
				result = getNewHtmlElement(elem, macroDatabase);
			}
		} else if (node.getNodeType() == Node.TEXT_NODE) {
			result = getNewTextElement(node);
		} else {
			throw new RuntimeException("Not yet implemented");
		}
		return result;
	}

	/**
	 * The content of the panel.
	 */
	private final HtmlElement htmlElement;

	/**
	 * The component that displays the text.
	 */
	private final JEditorPane jEditorPane;

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
	}

	/**
	 * Creates a new panel.
	 * 
	 * @param elem
	 *            an XML element that contains the description of the content to
	 *            be displayed by the panel.
	 * @param database
	 *            the database that contains the data to be displayed.
	 */
	public HtmlPanel(Element elem, MacroDatabase database) {
		this(parseNode(elem, database));
	}

	/**
	 * Returns a new panel with the specified text.
	 * 
	 * @param text
	 *            the text to be displayed by the panel.
	 */
	public HtmlPanel(final String text) {
		this(new HtmlElement() {

			@Override
			public String getText() {
				return text;
			}

		});
	}

	@Override
	public void update() {
		final String text = htmlElement.getText();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				jEditorPane.setText(text);
			}
		});
	}
}

// ***
