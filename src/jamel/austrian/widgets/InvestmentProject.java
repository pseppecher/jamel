package jamel.austrian.widgets;


import jamel.austrian.roles.Shareholder;

import java.util.HashMap;


public class InvestmentProject {

		/** The overall type of the project (bank or firm). */
		private String overallType ;
		
		/** The overall ID of the project.<br>
		 * 	To distinguish between firms and banks. */
		private int ID ;
		
		/** The ID of the firm or bank. */
		private int specificID ;
		
		/** The type of a new firm. */
		private String type ;
		
		/** The stage of a new firm. */
		private int stage ;
		
		/** The size of a new firm. */
		private int firmSize ;
		
		/** The required start-up capital for this project. */
		private int requiredCapital ;	
		
		/** The funds which have been provided. */
		private int funding;
		
		/** The list of new investors. */
		private final HashMap<Shareholder,Number> investors ;
		
		/** Indicates if the firm is expected to work with capital. */
		private boolean physicalCapital;
		
		/** Indicates if the project is finalized. */
		private boolean complete;
		
		
		/** 
		 * Creates a new investment project. 
		 */
		public InvestmentProject(String overallType, int ID, int ID2, String type, int stage, int size, int requiredCapital, boolean physicalCapital){
			this.overallType = overallType;
			this.ID = ID;
			this.specificID = ID2;
			this.type = type;
			this.stage = stage;
			this.firmSize = size;
			this.requiredCapital = requiredCapital;
			this.physicalCapital = physicalCapital;
			investors = new HashMap<Shareholder,Number>();
			funding = 0;
			complete = false;
		}
		
		
		/** 
		 * Registers a new investor. 
		 */
		public void newInvestor(Shareholder drawer, int amount) {
			if (investors.containsKey(drawer)){
				int existingInvestment = investors.get(drawer).intValue();
				investors.put(drawer, existingInvestment+amount);
			}
			else investors.put(drawer, amount);
			funding+=amount;
			if (funding == requiredCapital) complete = true;	
			if (funding > requiredCapital) throw new RuntimeException();		
		}
		
		
		/** 
		 * Returns the capital requirement of the project. 
		 */
		public int getCapitalRequirement(){
			return requiredCapital;
		}
		
		
		/** 
		 * Returns the funding that has already been received. 
		 */
		public int getFunding(){
			return funding;
		}

		
		/** 
		 * Computes and returns the ownership structure of the new firm/bank.<br>
		 * Converts money investments into percentages of ownership. 
		 */
		public HashMap<Shareholder, Number> getOwners() {
			
			if (!complete) throw new RuntimeException();		
		
			int totalFunds = funding;
			HashMap<Shareholder,Number> ownershipStructure = new HashMap<Shareholder,Number>();
			for (Shareholder investor:investors.keySet()){
				float share = investors.get(investor).floatValue() / totalFunds;
				ownershipStructure.put(investor, share);
			}
					
			return ownershipStructure;			
		}	
		
		
		/** 
		 * Returns the overall type of the new firm. 
		 */
		public String getOverallType(){
			return overallType;
		}
		
		
		/** 
		 * Returns the type of the new firm. 
		 */
		public String getType(){
			return type;
		}
		
		
		/** 
		 * Returns the stage of the new firm. 
		 */
		public int getStage(){
			return stage;
		}
		
		
		/** 
		 * Returns the size of the new firm. 
		 */
		public int getFirmSize(){
			return firmSize;
		}
		
		
		/** 
		 * Returns the ID the project. 
		 */
		public int getID(){
			return ID;
		}
		
		/** 
		 * Returns the specific ID the project. 
		 */
		public int getSpecificID(){
			return specificID;
		}
		
		
		/** 
		 * Returns the unit size. 
		 */
		public boolean physicalCapital(){
			return physicalCapital;
		}
		
}
