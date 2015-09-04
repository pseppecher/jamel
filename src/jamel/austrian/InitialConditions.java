package jamel.austrian;


/** The general parameters of the system. */
public class InitialConditions {
	
	
	/** 
	 * The general parameters of the system. 
	 */
	private static InitialConditions initialConditions;
	
	/** The interest rate on savings when banks are initialized. */
	public float savingsRate = 0.02f;
	
	/**  The interest rate on loans when banks are initialized. */
	public float lendingRate = 0.05f;
		
	/** The money per agent when initialized. */
	public int initialMoney = 50000; 
	
	
	/** Generates the object  */
	public InitialConditions(){
		
	}

	/**
	 * Returns the parameters.
	 */
	public static InitialConditions getInitialConditions() {
		return initialConditions;
	}
	
}
