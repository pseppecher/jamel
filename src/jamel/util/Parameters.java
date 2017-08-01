package jamel.util;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Encapsulates a set of parameters.
 * To facilitate the parsing of XML elements.
 */
public class Parameters {

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
	 * Creates a new Parameters object.
	 * 
	 * @param element
	 *            the XML element to be encapsulated.
	 */
	public Parameters(final Element element) {
		this.element = element;
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
		final String string = this.element.getAttribute(name);
		if (string.isEmpty()) {
			result = null;
		} else {
			result = Double.parseDouble(string);
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
	 * @param string
	 *            The name of the attribute to look for.
	 * @return <code>true</code> if an attribute with the given name is
	 *         specified on this element or has a default value,
	 *         <code>false</code>
	 *         otherwise.
	 */
	public boolean hasAttribute(String string) {
		return this.element.hasAttribute(string);
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

}
