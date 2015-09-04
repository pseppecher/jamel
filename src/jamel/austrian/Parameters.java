package jamel.austrian;


/** The general parameters of the system. */
public class Parameters {
	
	
	/** 
	 * The general parameters of the system. 
	 */
	private static Parameters parameters;
	
	
	/** Time preference lower bound. */
	public final float rho = 0.0016f;
	
	/** Time preference elasticity.<br>
	 * Increasing this parameter raises savings. */
	public float rhoFlex =  200000f;
		
	/** The productivity of labor. <br>
	 *  Is here also used as prM.*/
	public int prL = 1;
	
	/** The productivity of combining labor with intermediate goods. */
	public int prX = 3;
	
	/** The processing capacity of a worker. */
	public int capL = 1; 
	
	/** The processing capacity of a machine. */
	public int capK = 3; 

	/** The durability of machines. */
	public int tauK = 6;
	
	/** The number of labor units necessary to produce one machine. */
	public int muK = 3; 	
	
	/** Describes the returns to fixed capital. <p>
	 *  phi=0 -> constant returns to capital intensification.<br>
	 *  phi>0 -> decreasing returns.<br>
	 *  phi<0 -> increasing returns.  */
	public float phi = 0.5f; // currently not in use

	/** Describes the returns to fixed capital. <p>
	 *  psi=0 -> constant returns to capital intensification.<br>
	 *  psi>0 -> decreasing returns.<br>
	 *  psi<0 -> increasing returns.  */
	public float psi = 100f;
	
	/** The expansion probability. */
	public final float expansionProbability = 0.15f; 
	
	/** The number of firms at the highest stage, which are necessary for expansion. */
	public final float expansionRequirement1 = 8; 
	
	/** The total sales of these firms. */
	public float expansionRequirement2; 
	
	/** The standard size of firms (in number of workers). */
	public final int standardFirmSize = 2; 
	
	/** The extended size of firms (in number of workers). */
	public final int extendedFirmSize = 8; 
	
	/** Threshold for firm size determination. */
	public final int Q = 100; 
		
	/** The flexibility of offered wages. */
	public final float wFlex = 1000; 
	
	/** The flexibility of asked prices. */
	public final float pFlex = 1000; 
	
	/** The flexibility of offered quantities. */
	public final float qFlex = 0.2f; 
	
	/** The limit of the stochastic modifier. */
	public final float epsilon = 0.02f; 

	/** The lower bound of the target range for inventories (in percent of offered goods). */
	public final float sigma1 = 0.05f; 

	/** The upper bound of the target range for inventories (in percent of offered goods). */
	public final float sigma2 = 0.15f;
	
	/** The lending buffer for banks. <br>
	 * The targeted percentage of loanable funds that the bank wants to permanently withhold. */
	public final float lendingBuffer = 0.9f;
	
	/** The flexibility of interest rates. */
	public final float rFlex = 0.02f;
	
	/** The volume of a credit contract (future money). */
	public final int timeDepositSize = 10000;
	
	/** The number of offers each agent consults when searching a market. */
	public final int numSearch = 3;
	
	/** The number of owners to which firms and banks are assigned when initialized. <br>
	 * The actual number can be lower due to stochastic selection. */
	public final int numOwners = 3;
	
	/** The markup requirement for firms. */
	public final float firmMarkup = 0.05f;
	
	/** The distance to markup requirement for firms that do not use capital. */
	public final float delta = 0.01f;
	
	/** The minimal spread between the savings and the lending rate. */
	public final float bankMarkup = 0.01f;
	
	public final float minSavingsRate = 0.00001f;
	
	
	/** Generates the object  */
	public Parameters(){
		expansionRequirement2 = prL*13*expansionRequirement1;	
	}

	/**
	 * Returns the parameters.
	 */
	public static Parameters getParameters() {
		return parameters;
	}
	
}
