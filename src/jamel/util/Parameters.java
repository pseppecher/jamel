package jamel.util;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Encapsulates a set of parameters.
 * To facilitate the parsing of XML elements.
 */
public class Parameters {

	/**
	 * Converts an element into a string.
	 * 
	 * @param elem
	 *            the element to be converted.
	 * @return a string representation of this element.
	 */
	private static String element2string(final Element elem) {
		final String result;
		try {
			final DOMSource domSource = new DOMSource(elem);
			final StringWriter writer = new StringWriter();
			final StreamResult sResult = new StreamResult(writer);
			final TransformerFactory tf = TransformerFactory.newInstance();
			final Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(domSource, sResult);
			result = writer.toString();
		} catch (TransformerException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	/**
	 * Converts a <code>NodeList</code> into a <code>List</code> of
	 * <code>Parameters</code>.
	 * 
	 * @param nodeList
	 *            the <code>NodeList</code> to be converted
	 * @return a <code>List</code> of <code>Parameters</code>
	 */
	private static List<Parameters> getList(final NodeList nodeList) {
		final List<Parameters> result = new LinkedList<>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i) instanceof Element) {
				result.add(new Parameters(nodeList.item(i)));
			}
		}
		return result;
	}

	/**
	 * The encapsulated XML element.
	 */
	final private Element element;
	
	/**
	 * The string representation of these parameters.
	 */
	final private String string;

	/**
	 * Creates a new Parameters object.
	 * 
	 * @param element
	 *            the XML element to be encapsulated.
	 */
	public Parameters(final Element element) {
		this.element = element;
		this.string = element2string(this.element);
	}

	/**
	 * Creates a new Parameters object.
	 * 
	 * @param node
	 *            the XML node to be encapsulated. The node should be an
	 *            instance of <code>Element</code>.
	 */
	public Parameters(final Node node) {
		ArgChecks.nullNotPermitted(node, "node");
		if (!(node instanceof Element)) {
			throw new IllegalArgumentException("The node should be an Element: " + node.getNodeName());
		}
		this.element = (Element) node;
		this.string = element2string(this.element);
	}

	/**
	 * Returns the specified (first) sub-parameters.
	 * If there is no such parameters, this returns <code>null</code>.
	 * 
	 * @param name
	 *            the name of the sub-parameters to be returned.
	 * @return the specified (first) sub-parameters, or <code>null</code> if not
	 *         found.
	 */
	public Parameters get(String name) {
		final Parameters result;
		final Node node = this.element.getElementsByTagName(name).item(0);
		if (node == null) {
			result = null;
		} else {
			result = new Parameters(node);
		}
		return result;
	}

	/**
	 * Returns all sub parameters.
	 * 
	 * @return a list of all sub parameters.
	 */
	public List<Parameters> getAll() {
		return getList(this.element.getChildNodes());
	}

	/**
	 * Returns all sub parameters with the given name
	 * 
	 * @param name
	 *            The name of the sub parameters to match on. The special value
	 *            "*" matches all parameters.
	 * 
	 * @return a list of all sub parameters with the given name
	 */
	public List<Parameters> getAll(String name) {
		return getList(this.element.getElementsByTagName(name));
	}

	/**
	 * Retrieves an attribute value by name.
	 * 
	 * @param name
	 *            The name of the attribute to retrieve.
	 * @return The attribute value as a string, or the empty string if that
	 *         attribute does not have a specified or default value.
	 */
	public String getAttribute(String name) {
		return this.element.getAttribute(name);
	}

	/**
	 * Retrieves an attribute <code>Double</code> value by name.
	 * 
	 * @param name
	 *            The name of the attribute to retrieve.
	 * @return The attribute value as a <code>Double</code>.
	 */
	public Double getDoubleAttribute(String name) {
		final Double result;
		final String attribute = this.element.getAttribute(name);
		if (attribute.isEmpty()) {
			result = null;
		} else {
			result = Double.parseDouble(attribute);
		}
		return result;
	}

	/**
	 * Returns the double value of the specified parameter.
	 * 
	 * Useful to display parameter values in the gui.
	 * 
	 * @param key
	 *            the string key of the parameter.
	 * @return the double value of the specified parameter.
	 */
	public Double getDoubleValue(String key) {
		final Double result;
		final String[] split = key.split("\\.", 2);
		if (split.length == 1) {
			result = this.getDoubleAttribute(key);
			if (result == null) {
				throw new RuntimeException("Parameter not found: " + key);
			}
		} else {
			final Parameters sub = this.get(split[0]);
			if (sub == null) {
				throw new RuntimeException("Parameter not found: " + key);
				// result = null;
			}
			result = sub.getDoubleValue(split[1]);
		}
		return result;
	}

	/**
	 * Returns the encapsulated element.
	 * 
	 * @return the encapsulated element.
	 */
	public Element getElem() {
		return this.element;
	}

	/**
	 * Retrieves an attribute <code>Integer</code> value by name.
	 * 
	 * @param name
	 *            The name of the attribute to retrieve.
	 * @return The attribute value as a <code>Integer</code>.
	 */
	public Integer getIntAttribute(String name) {
		if (!this.element.hasAttribute(name)) {
			throw new RuntimeException("Parameter not found: \"" + name+"\" in \""+this.element.getTagName()+"\".");
		}
		return Integer.parseInt(this.element.getAttribute(name));
	}

	/**
	 * Returns the name of the encapsulated element.
	 * 
	 * @return the name of the encapsulated element.
	 */
	public String getName() {
		return this.element.getNodeName();
	}

	/**
	 * Returns the text content of the encapsulated element and its descendants.
	 * All space characters are cut.
	 * 
	 * @return the text content of the encapsulated element and its descendants.
	 */
	public String getText() {
		return this.element.getTextContent().replaceAll("(\\p{javaSpaceChar}|\\r|\\n|\\t)", "");
	}

	/**
	 * Returns <code>true</code> when an attribute with a given name is
	 * specified on this element or has a default value, <code>false</code>
	 * otherwise.
	 * 
	 * @param name
	 *            The name of the attribute to look for.
	 * @return <code>true</code> if an attribute with the given name is
	 *         specified on this element or has a default value,
	 *         <code>false</code>
	 *         otherwise.
	 */
	public boolean hasAttribute(String name) {
		return this.element.hasAttribute(name);
	}

	/**
	 * Returns the array of strings computed by splitting the text content of
	 * the encapsulated element around matches of the given regular expression.
	 * 
	 * @param regex
	 *            the delimiting regular expression
	 *
	 * @return the array of strings computed by splitting the text content of
	 *         the encapsulated element around matches of the given regular
	 *         expression.
	 */
	public String[] splitTextContent(String regex) {
		return getText().split(regex);
	}

	@Override
	public String toString() {
		return this.string;
	}

}
