package jamel.basic.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

/**
 * A class to store the parameters of a simulation.
 */
@SuppressWarnings("serial")
public class JamelParameters extends TreeMap<String, String> {

	/**
	 * For the JTree presentation of the parameters.
	 */
	@SuppressWarnings("javadoc")
	public class Param {
		private final String key;
		private final String path;
		public Param(String key, String path) {
			this.key=key;
			this.path=path;
		}
		public String getKey() {
			return key;
		}
		public String getValue() {
			final String value = JamelParameters.this.get(key);
			String result;
			if (value!=null) {
				result=key+"="+value;
			}
			else {
				result="";
				for (Entry<String,String> entry: entrySet()) {
					if (entry.getKey().startsWith(path)) {
						result+=entry.getKey()+"="+entry.getValue()+rc;
					}
				}
			}
			return result;
		}
		public String toString() {
			final String[] string = getKey().split("\\.");
			return string[string.length-1];
		}
	}

	/** Line break tag. */
	private static final String br = "<br>";

	/** CMD_PREAMBLE_BEGIN */
	private static final String CMD_BEGIN = "*BEGIN*";

	/** CMD_SIMULATION_BEGIN */
	private static final String CMD_END = "*END*";

	/** Error message. */
	private static final String ERROR_WHILE_PARSING_THE_PREAMBLE = "Error while parsing the preamble: ";

	/** The line separator. */
	private static final String rc = System.getProperty("line.separator");

	/**
	 * Returns a map that contains the initial parameters of the simulation.
	 * @param scenario a list of strings to be parsed.
	 * @return a Map<key,value> where key is the name of the parameter, value a string containing its value.
	 */
	private static TreeMap<String, String> parseScenario(List<String> scenario) {
		final TreeMap<String, String> params = new TreeMap<String, String>();
		boolean inPreamble = false;
		final LinkedList<String> preamble = new LinkedList<String>();

		for (String line: scenario) {
			line = line.split("//",2)[0].trim();
			if (!line.isEmpty()) {
				if (line.equals(CMD_BEGIN)){
					inPreamble=true;
				}
				else if (inPreamble) {
					if (line.equals(CMD_END)) {
						// End of the scenario.
						break;
					}
					else {
						preamble.add(line);
					}
				}
			}
		}

		// Parsing the scenario // TODO a better management of errors 

		LinkedList<String> prefixes = new LinkedList<String>(); 
		for (String line: preamble) {
			if (line.endsWith("{")) {
				// new prefix detected.
				String result[] = line.split("\\{",2);
				if (!result[1].equals("")) {
					throw new RuntimeException(ERROR_WHILE_PARSING_THE_PREAMBLE+"too many brackets in: "+line);
				}
				final String prefix = result[0].trim(); 
				if (prefix.equals("")) {
					throw new RuntimeException(ERROR_WHILE_PARSING_THE_PREAMBLE+"prefix not found in: "+line);
				}
				prefixes.add(prefix);
			}
			else if (line.equals("}")) {
				try {
					prefixes.removeLast();
				} catch (NoSuchElementException e) {
					e.printStackTrace();
					throw new RuntimeException(ERROR_WHILE_PARSING_THE_PREAMBLE+"too many }.");				
				}
			}
			else if (line.contains("{") || line.contains("}")) {
				throw new RuntimeException(ERROR_WHILE_PARSING_THE_PREAMBLE+"unexpected bracket in: "+line);				
			}
			else {
				final String prefix=prefixes.toString().replaceAll(", ",".").replaceAll("[\\[\\]]", "");;
				final String[] word = line.split("=",2);
				final String key;
				if (word.length!=2 || word[0].equals("") || word[1].equals("")){
					throw new RuntimeException(ERROR_WHILE_PARSING_THE_PREAMBLE+"something is missing in: "+line);									
				}
				word[0]=word[0].trim();
				word[1]=word[1].trim();
				if (word[0].equals("this")){
					key = prefix;
				}
				else if (!prefix.equals("")) {
					key=prefix+"."+word[0];
				}
				else {
					key=word[0];
				}
				params.put(key, word[1]);				
			}
		}
		
		return params;
	}

	/**
	 * Splits the specified string by the given delimiter. In each resulting item, leading and trailing white space are removed.
	 * @param string the string to be split
	 * @param delimiter the delimiter
	 * @return an array of strings
	 */
	public static String[] split(String string, String delimiter) {
		final String[] array=string.split(delimiter);
		int index=0;
		for (String item:array) {
			array[index]=item.trim();
			index++;
		}
		return array;
	}

	/**
	 * Creates a new map of the parameters.
	 */
	public JamelParameters() {
		super();
	}

	/**
	 * Creates a new map of the parameters.
	 * @param scenario a list of strings containing the parameters.
	 */
	public JamelParameters(List<String> scenario) {
		super();
		this.putAll(parseScenario(scenario));
	}

	/**
	 * Returns the parameter to which the specified key is mapped, or null if the parameters contains no mapping for the key.
	 * @param keys strings that will be concatenated using dots to form the key whose associated parameter is to be returned.
	 * @return the parameter to which the specified key is mapped, or null if the parameters contains no mapping for the key.
	 */
	public String get(String... keys) {
		String key = keys[0];
		for (int index=1;index<keys.length;index++) {
			key+="."+keys[index];
		}
		/*if (!this.containsKey(key)) { // a ne marche pas, rŽflŽchir ˆ a. Construire une Exception spŽcifique ?
			throw new IllegalArgumentException("Unknown parameter: "+key);
		}*/
		return this.get(key);
	}

	/**
	 * Splits and returns the parameter to which the specified key is mapped. An empty array is returned if the parameters contains no mapping for the key.
	 * @param keys strings that will be concatenated using dots to form the key whose associated parameter is to be returned.
	 * @return the array of strings computed by splitting the parameter around matches of the given regular expression
	 */
	public String[] getArray(String[] keys) {
		final String string = get(keys);
		final String[] result;
		if (string!=null) {
			result = split(string,",");
			for(int i =0; i<result.length; i++) {
				result[i] = result[i].trim();
			}
		}
		else {
			result = new String[0];
		}
		return result;
	}

	/**
	 * Returns a hierarchical view of the parameters.
	 * @return a <code>JTree</code>.
	 */
	public JTree getJTree() {
		final Map<String,DefaultMutableTreeNode> treeMap = new TreeMap<String,DefaultMutableTreeNode>();
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode(new Param("Parameters","")); 
		for(String key:keySet()) {
			final String[] keys = key.split("\\.");
			String path="";
			DefaultMutableTreeNode parent = root;
			for (int i=0; i<keys.length; i++) {
				if ("".equals(path)){
					path=keys[i];
				} else {
					path+="."+keys[i];
				}
				if (treeMap.containsKey(path)) {
					parent=treeMap.get(path);
				}
				else {
					final DefaultMutableTreeNode newChild;
					if (i==keys.length-1) {
						newChild = new DefaultMutableTreeNode(new Param(key,path));
					}
					else {
						newChild = new DefaultMutableTreeNode(new Param(keys[i],path));
					}
					parent.add(newChild);						
					treeMap.put(path, newChild);
					parent=newChild;
				}
			}
		}
		final JTree jTree = new JTree(root){{
			setShowsRootHandles(true);
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			setCellRenderer(new DefaultTreeCellRenderer(){{
				setClosedIcon(null);
				setOpenIcon(null);
				setLeafIcon(null);
			}});
		}};
		return jTree;
	}

	/**
	 * Returns an array containing all the keys starting with the specified prefix.
	 * @param prefix the prefix.
	 * @return an array of strings.
	 */
	public String[] getStartingWith(String prefix) {
		final List<String> result = new ArrayList<String>(50);
		for(String key:this.keySet()) {
			if (key.startsWith(prefix)) {
				result.add(key);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Returns a html representation of the parameters.
	 * @return a string.
	 */
	public String toHtml() {
		String result="";
		for(Entry<String,String> entry:this.entrySet()) {
			final String key = entry.getKey();
			result+=key+"="+entry.getValue()+br;
		}
		return result;
	}

}

// ***
