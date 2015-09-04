package jamel.austrian.util;



public class Alphabet{

	private static String alphabet =  "ABCDEFGHIJKLMNOPQRSTUVWXYZ" ;
	
	
	/** Returns the (n+1)th letter of the alphabet.<br>
	 * 	Thus: index=1 returns "B". */
	public static String getLetter(int index){
		return String.valueOf(alphabet.charAt(index));
	}
	
	
	/** Returns the alphabetical position of a letter. */
	public static int getPosition(String letter){
		return alphabet.indexOf(letter);
	}
	
	
	/** Returns the previous letter in the alphabet. */
	public static String getPreviousLetter(String letter){
		return getLetter(getPosition(letter)-1);
	}
	
	
	
}
