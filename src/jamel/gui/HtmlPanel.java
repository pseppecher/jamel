package jamel.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

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

import jamel.data.Expression;
import jamel.util.Simulation;

/**
 * A jPanel that contains Html text and dynamical data.
 */
public class HtmlPanel extends JScrollPane implements AdjustmentListener, Updatable {

	/**
	 * Error message.
	 */
	private static final String ERROR_STRING = "<div style=\"color:red\">ERROR</div>";

	/**
	 * Returns a new data element.
	 * 
	 * @param expression
	 *            the expression.
	 * @return a new data element.
	 */
	private static HtmlElement getNewDataElement(final Expression expression) {
		return new HtmlElement() {

			@Override
			public String getText() {
				return "" + expression.getValue();
			}

		};
	}

	/**
	 * Converts the specified XML element into a new HTML element.
	 * 
	 * @param elem
	 *            the XML element to be converted.
	 * @param simulation
	 *            the parent simulation.
	 * @return a new {@link HtmlElement}.
	 */
	private static HtmlElement getNewHtmlElement(final Element elem, final Simulation simulation) {

		final String tagName = elem.getTagName();
		final NodeList list = elem.getChildNodes();
		final NamedNodeMap attributes = elem.getAttributes();
		String attributes1 = "";
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node item = attributes.item(i);
			attributes1 += " " + item.getNodeName() + "=\"" + item.getNodeValue() + "\"";
		}
		final String attributes2 = attributes1;

		final List<HtmlElement> htmlElements = new LinkedList<>();
		for (int i = 0; i < list.getLength(); i++) {

			// ***

			final HtmlElement htmlElement;
			if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
				final Element elem2 = (Element) list.item(i);
				if (elem2.getNodeName().equals("data")) {
					Expression expression = null;
					try {
						expression = simulation.getExpression(elem2.getTextContent());
					} catch (final Exception e) {
						e.printStackTrace();
					}
					if (expression == null) {
						htmlElement = getNewTextElement(ERROR_STRING);
					} else {
						htmlElement = getNewDataElement(expression);
					}
					/*
					 * TODO implement or remove :
					 * 
					} else if (elem2.getNodeName().equals("test")) {
						result = getNewTestElement(elem2, macroDatabase);
					} else if (elem2.getNodeName().equals("info")) {
						result = getNewInfoElement(elem2, macroDatabase);
					} else if (elem2.getNodeName().equals("jamel.version")) {
						result = getNewJamelVersionElement();*/
				} else {
					htmlElement = getNewHtmlElement(elem2, simulation);
				}
			} else if (list.item(i).getNodeType() == Node.TEXT_NODE) {
				final String string = ((Text) list.item(i)).getWholeText();
				htmlElement = getNewTextElement(string);
			} else if (list.item(i).getNodeType() == Node.COMMENT_NODE) {
				htmlElement = null;
			} else {
				throw new RuntimeException("not yet implemented");
				// htmlElement = getNewErrorElement("Unexpected node type: " +
				// node.getNodeType());
			}

			// ***

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
	 * Returns a new text element.
	 * 
	 * @param string
	 *            the string to be displayed.
	 * @return a new text element.
	 */
	private static HtmlElement getNewTextElement(String string) {
		return new HtmlElement() {

			@Override
			public String getText() {
				return string;
			}

		};
	}

	/**
	 * If the scroll bar has to be adjusted.
	 */
	private boolean adjustScrollBar = true;

	/**
	 * The main html element.
	 */
	final private HtmlElement htmlElement;

	/**
	 * The component that displays the text.
	 */
	private JEditorPane jEditorPane;

	/**
	 * Previous maximum value of the scroll bar.
	 */
	private int previousMaximum = -1;

	/**
	 * Previous value of the scroll bar.
	 */
	private int previousValue = -1;

	/**
	 * The text displayed by this panel.
	 */
	private String text = "";

	/**
	 * Creates and returns a new HtmlPanel.
	 * 
	 * @param elem
	 *            the description of the panel to be created.
	 * @param gui
	 *            the parent gui.
	 */
	public HtmlPanel(final Element elem, final Gui gui) {
		super();

		final Element panelDescription;

		if (elem.hasAttribute("src")) {

			/*
			 * Opens and reads the XML file that contains the specification of the panel.
			 */

			final String src = elem.getAttribute("src");
			final String fileName = gui.getFile().getParent() + "/" + src;
			final File panelFile = new File(fileName);
			final Element root;
			try {
				root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(panelFile).getDocumentElement();
			} catch (final Exception e) {
				throw new RuntimeException("Something went wrong while reading \"" + fileName + "\"", e);
			}
			if (!root.getTagName().equals("html")) {
				throw new RuntimeException(fileName + ": Bad element: " + root.getTagName());
			}
			panelDescription = root;

		} else {
			panelDescription = elem;
		}

		this.jEditorPane = new JEditorPane();
		this.jEditorPane.setContentType("text/html");
		this.htmlElement = getNewHtmlElement(panelDescription, gui.getSimulation());
		this.jEditorPane.setText(this.htmlElement.getText());
		this.jEditorPane.setEditable(false);
		this.jEditorPane.addHyperlinkListener(new HyperlinkListener() {
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

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
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
		final String newText = this.htmlElement.getText();
		if (!this.text.equals(newText)) {
			this.text = newText;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					HtmlPanel.this.jEditorPane.setText(newText);
					HtmlPanel.this.revalidate();
					HtmlPanel.this.repaint();
				}
			});
		}
	}

}
