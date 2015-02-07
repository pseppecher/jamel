package jamel.util;

import jamel.Simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * A file parser.
 */
public class FileParser {

	@SuppressWarnings("javadoc")
	private static class KEY {
		public static final String BEGIN = "\\begin";
		public static final String END = "\\end";
	}

	/**
	 * Returns a list of strings, each string representing a line of the file.
	 * For each line, the comments are removed, leading and trailing whitespace are omitted.
	 * @param file the file to be parsed.
	 * @return a list of string.
	 * @throws FileNotFoundException if the file is not found.
	 */
	public static List<String> parseList(File file) throws FileNotFoundException {
		List<String> result = new LinkedList<String>();
		final Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine().split("//", 2)[0].trim();
			if (!line.isEmpty()) {
				result.add(line);
			}
		}
		scanner.close();
		return result;
	}

	/**
	 * Returns a list of strings, each string representing a line of the file.
	 * For each line, the comments are removed, leading and trailing whitespace are omitted.
	 * @param fileName the name of the file to be parsed.
	 * @return a list of string.
	 * @throws FileNotFoundException if the file is not found.
	 */
	public static List<String> parseList(String fileName) throws FileNotFoundException {
		return parseList(new File(Simulator.getScenarioFile().getParent()+"/"+fileName));		
	}

	/**
	 * Reads the file and returns its content as a map of String key,String value. 
	 * @param file the file to read.
	 * @return a map of String key,String value.
	 * @throws FileNotFoundException if the file is not found.
	 */
	public static Map<String, String> parseMap(File file) throws FileNotFoundException {
		final Map<String, String> map = new LinkedHashMap<String,String>();
		@SuppressWarnings("serial") final LinkedList<String> prefix = new LinkedList<String>() {
			@Override
			public String toString() {
				String result = "";
				for(String string:this) {
					result+=string+".";
				}
				return result;
			}			
		}; 
		final List<String> list = parseList(file);
		for (String line: list) {
			if (line.startsWith(KEY.BEGIN)) {
				final String substring = line.substring(6);
				if (substring.startsWith("{") && substring.endsWith("}")) {
					prefix.add(substring.substring(1, substring.length()-1).trim());
				} 
				else {
					throw new RuntimeException("Syntax error in: "+substring);						
				}
			}
			else if (line.startsWith(KEY.END)) {
				final String substring = line.substring(4);
				if (substring.startsWith("{") && substring.endsWith("}")) {
					final String word = substring.substring(1, substring.length()-1).trim();
					if (word.equals(prefix.getLast())) {
						prefix.removeLast();
					}
					else {
						throw new RuntimeException("Unexpected end: "+word);							
					}
				} 
				else {
					throw new RuntimeException("Syntax error in: "+substring);						
				}
			}
			else {
				final String[] entry = line.split("=", 2);
				try {
					map.put(prefix.toString()+entry[0].trim(),entry[1].trim());
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					throw new RuntimeException("Syntax error: "+line);
				}
			}
		}
		return map;
	}


	/**
	 * Reads the file and returns its content as a map of String key,String value. 
	 * @param fileName the name of the file to read.
	 * @return a map of String key,String value.
	 * @throws FileNotFoundException if the file is not found.
	 */
	public static Map<String, String> parseMap(String fileName) throws FileNotFoundException {
		return parseMap(new File(Simulator.getScenarioFile().getParent()+"/"+fileName));
	}

	/**
	 * Reads the specified file in resource and returns its contain as a String.
	 * @param filename  the name of the resource file.
	 * @return a String.
	 */
	public static String readResourceFile(String filename) {
		BufferedReader br=new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/"+filename)));
		String line;		
		String string="";
		final String rc = System.getProperty("line.separator");
		try {
			while ((line=br.readLine())!=null){
				string+=line+rc;
			}
			br.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return string;
	}

	/**
	 * Returns an array of string.
	 * Splits the given string by commas.
	 * The resulting string are trimed. 
	 * @param value the string to split.
	 * @return an array of string.
	 */
	public static String[] toArray(String value) {
		final String[] result = value.split(",");
		for(int i =0; i<result.length; i++) {
			result[i] = result[i].trim();
		}
		return result;
	}

}

// ***
