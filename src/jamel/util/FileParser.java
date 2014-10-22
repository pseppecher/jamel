package jamel.util;

import jamel.Simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * A file parser.
 */
public class FileParser {

	/**
	 * Reads the file and returns its content as a list of strings. 
	 * @param fileName the name of the file to read.
	 * @return a list of strings.
	 * @throws FileNotFoundException if the file is not found.
	 */
	public static Map<String, String> parse(String fileName) throws FileNotFoundException {
		final File file = new File(Simulator.getScenarioFile().getParent()+"/"+fileName);
		final HashMap<String,String> map = new HashMap<String,String>();
		final Scanner scanner=new Scanner(file);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine().split("//", 2)[0].trim();
			if (!line.isEmpty()) {
				final String[] entry = line.split("=", 2);
				map.put(entry[0].trim(),entry[1].trim());
			}
		}
		scanner.close();
		return map;
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
