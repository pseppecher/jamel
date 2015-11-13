/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2012, Pascal Seppecher.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/javadoc/index.html>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JAMEL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.austrian.firms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.w3c.dom.Element;

import jamel.austrian.markets.Market;
import jamel.austrian.sfc.SFCSector;
import jamel.austrian.util.Alphabet;
import jamel.austrian.widgets.InvestmentProject;
import jamel.austrian.widgets.StartupDetails;
import jamel.basic.Circuit;
import jamel.basic.data.SectorDataset;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.AgentSet;
import jamel.basic.sector.BasicAgentSet;
import jamel.basic.sector.Phase;


/**
 * Represents the firms sector.
 */
public class FirmSector extends SFCSector {

	/** The collection of agents. */
	protected final AgentSet<Firm> firms;

	/** The list of firms with positive investment demand (as updated during a time period). */
	private final AgentSet<Firm> firmsWithPositiveDemand ;

	/** The list of firms with zero investment demand (as updated during a time period). */
	private final AgentSet<Firm> firmsWithZeroDemand ;

	/** The structure of gross profits.*/
	private final HashMap<String, Integer> profitStructure ;

	/** The structure of net profits.*/
	private final HashMap<String, Integer> netProfitStructure ;

	/** The structure of revenue.*/
	private final HashMap<String, Integer> revenueStructure ;

	/** The structure of average operating margins.*/
	private final TreeMap<Float, String> profitabilityStructure ;

	/** The structure of net average operating margins.*/
	private final TreeMap<Float, String> netProfitabilityStructure ;

	/** The structure of average operating margins.*/
	private final TreeMap<Float, String> copyOfProfitabilityStructure ;

	/** The number of firms at each node.*/
	private final HashMap<String, Integer> firmPositions ;

	/** The number of workers at each node.*/
	private final HashMap<String, Integer> workerPositions ;

	/** The list of defaulting firms. */
	private final LinkedList<Firm> firmFailures;

	/** The number of newly created firms. */
	private int newFirms;

	/** The value of nominal GDP in the base year. */
	private static double nominalGDPBase = Float.NaN; 

	private boolean searchFirm = true;

	private boolean monitor;


	/**
	 * Creates a new sector for firms.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public FirmSector(String name, Circuit aCircuit) {
		super(name, aCircuit);
		this.firms = new BasicAgentSet<Firm>(this.random);
		this.firmsWithPositiveDemand = new BasicAgentSet<Firm>(this.random);
		this.firmsWithZeroDemand = new BasicAgentSet<Firm>(this.random);
		this.revenueStructure = new HashMap<String,Integer>();
		this.profitStructure = new HashMap<String,Integer>();
		this.netProfitStructure = new HashMap<String,Integer>();
		this.profitabilityStructure = new TreeMap<Float, String>();
		this.netProfitabilityStructure = new TreeMap<Float, String>();
		this.copyOfProfitabilityStructure = new TreeMap<Float, String>();
		this.firmPositions = new HashMap<String, Integer>();
		this.workerPositions = new HashMap<String, Integer>();
		this.firmFailures = new LinkedList<Firm>();
	}


	@Override
	public void doEvent(Element event) {
		if (event.getNodeName().equals("new")) {
			final int size = Integer.parseInt(event.getAttribute("size"));
			this.firms.putAll(this.createFirms(this.agentType,size));
			getInvestmentBank().setFirmID(size);
		}
		else {
			throw new RuntimeException("Unknown event or not yet implemented: "+event.getNodeName());			
		}
	}


	/**
	 * Creates firms.
	 * @param type the type of firms to create.
	 * @param lim the number of firms to create.
	 * @return a list containing the new firms. 
	 */
	private List<Firm> createFirms(String type, int lim) {
		final List<Firm> result = new ArrayList<Firm>(lim);

		// Instantiates firms at the beginning
		try {
			int typeMin = getParam("typeMin").intValue();
			int typeRange = getParam("typeRange").intValue();
			int initialMoney = initialConditions.initialMoney;
			String producerType = null;
			int stage = 0;

			for(int index=0;index<lim;index++) {
				this.countAgents++;

				//  Choose a random node for the firm to operate at. 
				stage = 1 + random.nextInt(getParam("stages").intValue());
				if (typeRange==0) producerType =  Alphabet.getLetter(typeMin);
				else producerType =  Alphabet.getLetter(typeMin + random.nextInt(typeRange+1));
				if (producerType.equals("A")) stage = 1;

				// Create the target market(s) if necessary.
				getMarketSector().addMarkets(producerType,stage);

				final Firm firm = (Firm) Class.forName(type,false,ClassLoader.getSystemClassLoader()).getConstructor(
						Integer.class,Circuit.class,FirmSector.class,HashMap.class,Integer.class,String.class,Integer.class).
						newInstance(countAgents,
								circuit,
								FirmSector.this,
								getHouseholdsSector().selectRandomShareholders(3),
								initialMoney, 
								producerType, 
								stage);
				result.add(firm);

				int firmNumber;
				if (!firmPositions.containsKey(producerType+stage)) firmNumber = 1;
				else firmNumber = firmPositions.get(producerType+stage)+1;
				firmPositions.put(producerType+stage, firmNumber);
			}
		} catch (Exception e) {
			throw new RuntimeException("Firm creation failure",e); 
		}
		return result;
	}




	/**
	 * Finds the position at which the next firm is created.<br>
	 * Called by the investmentBank.
	 */
	public StartupDetails getStartupDetails(){

		String type = null;
		int stage = 0;
		boolean physicalCapital;
		int maxStage;


		if (getRandom().nextFloat()<parameters.expansionProbability) { 

			int typeMin = getParam("typeMin").intValue();
			int typeRange =  getParam("typeRange").intValue();


			//looks to expand the system 
			type = Alphabet.getLetter(typeMin + random.nextInt(typeRange+1));
			maxStage = getMaxStage2(type);

			//non-machines
			if (!type.equals("A")){ 
				int nextLowerStage = maxStage-1;
				Market highestMarket = getMarket(type+nextLowerStage);
				if (firmPositions.get(type+maxStage) >= parameters.expansionRequirement1
						& highestMarket.getSales() > parameters.expansionRequirement2){
					stage = maxStage+1;
				}
			}
			// machines
			else stage = 1;
		}
		else {	// stays within the existing boundaries
			String key=null;

			// Choose according to labor allocation
			float highestWorkerToFirm = Float.MIN_VALUE;
			for (String currentKey : firmPositions.keySet()){
				if (firmPositions.get(currentKey)==0) continue;
				float workerToFirm = (float) workerPositions.get(currentKey) / (float) firmPositions.get(currentKey);
				if (workerToFirm>highestWorkerToFirm){
					highestWorkerToFirm = workerToFirm;
					key = currentKey;
				}
			}

			// Choose Randomly
			/* int randomIndex = getRandom().nextInt(firmPositions.size());
			int i=0;
			for (String randomKey : firmPositions.keySet()){
				if (i==randomIndex) {
					key = randomKey;
					break;
				}
				i++;
			}*/

			// Choose according to Profitability
			/*	if (copyOfProfitabilityStructure.size()>0){
				key = copyOfProfitabilityStructure.remove(copyOfProfitabilityStructure.lastKey());
			}
			else {
				copyOfProfitabilityStructure.putAll(profitabilityStructure);
				key = copyOfProfitabilityStructure.remove(copyOfProfitabilityStructure.lastKey());
			}*/

			type = String.valueOf(key.charAt(0));
			stage = Integer.parseInt(String.valueOf(key.charAt(1)));
			maxStage = getMaxStage2(type);
		}


		if (stage == 0) stage = 1 + getRandom().nextInt(maxStage);

		getMarketSector().addMarkets(type,stage);
		if (stage >= maxStage | type.equals("A")) physicalCapital = false;
		else physicalCapital = true;

		StartupDetails details = new StartupDetails(type, stage, physicalCapital);
		return details;
	}


	/**
	 * Initiates investments.<br>
	 * Investments are carried out in single units and in a random fashion
	 * so that no firm has a systematic advantage over others.
	 */
	public void investment() {

		do{	
			Firm firm = firmsWithPositiveDemand.getRandomAgent();// If no randomization then low-price and type-A firms could register first.
			if (firmsWithZeroDemand.contains(firm)) firmsWithZeroDemand.remove(firm);
			if (!firm.invest()) firmsWithPositiveDemand.remove(firm);
		} while (firmsWithPositiveDemand.getList().size()>0);

		for (Firm firm : firmsWithZeroDemand.getList()) {
			firm.laborRelease();
		}
	}


	/**
	 * An agents files bankruptcy.
	 */
	public void bankruptcy(Firm aBankruptAgent){	
		firmFailures.add(aBankruptAgent);	
	}


	@Override
	public Phase getPhase(String name) {
		Phase result = null;

		if (name.equals("opening")) {
			result = new AbstractPhase(name, this){

				@Override public void run() {

					newFirms=0;
					LinkedList<InvestmentProject> projects = getInvestmentBank().getNewFirmProjects();

					if (projects.size()>0){
						for (InvestmentProject project:projects){

							BasicFirm newFirm;
							try {
								String producerType = project.getType();
								int stage = project.getStage();
								monitor = false;
								// This is a search criterion. 
								// For the first firm which meets this criterion detailed output is produced.
								// To enable: switch both to true
								if (searchFirm & producerType.equals("A") & stage==1){// & getCurrentPeriod().getYearValue()>2020){
									searchFirm = false;
									monitor = false;
								}

								newFirm = (BasicFirm) Class.forName("jamel.austrian.firms.BasicFirm",false,ClassLoader.getSystemClassLoader()).getConstructor(
										InvestmentProject.class,Circuit.class,FirmSector.class,Boolean.class).
										newInstance(project,
												circuit,
												FirmSector.this,
												monitor);

								int firmNumber;
								if (!firmPositions.containsKey(producerType+stage)) firmNumber = 1;
								else firmNumber = firmPositions.get(producerType+stage)+1;
								firmPositions.put(producerType+stage, firmNumber);
							}
							catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException("Firm creation failure"); 
							}
							firms.put(newFirm);
							newFirms++;
						}
					}

					for (final Firm firm:firms.getList()) {
						firm.open();
					}
					firmsWithZeroDemand.clear();
					firmsWithZeroDemand.putAll(firms.getList());
				}				
			};			
		}

		if (name.equals("offering")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getList()) {
						firm.makeOffer();
					}
				}				
			};			
		}


		/**
		 * Firms revise their strategies.<br>
		 * If there is an acute shortage of capital they switch to pure labor production.
		 */
		if (name.equals("setProductionMode")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getList()) {
						firm.setProductionMode();
					}
				}				
			};			
		}


		/**
		 * Firms purchase fixed capital.
		 */
		if (name.equals("buyMachinery")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					AgentSet<Firm> copyOfFirmsList = new BasicAgentSet<Firm>(random);
					copyOfFirmsList.putAll(firms.getList());
					do{	Firm firm = copyOfFirmsList.getRandomAgent();
					if (!firm.buyMachinery()) copyOfFirmsList.remove(firm);
					} while (copyOfFirmsList.getList().size()>0);	
				}				
			};			
		}

		/**
		 * Firms update their prices and quantity targets.
		 */
		if (name.equals("updateMachinePrices")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getList()) {
						firm.updateMachinePrice();
					}
				}				
			};			
		}

		/**
		 * Firms plan their production activities
		 */
		if (name.equals("planProduction")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getList()) {
						firm.planProduction();
					}
				}				
			};			
		}

		/**
		 * Firms make offers in the labor market.
		 */
		if (name.equals("offerJobs")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getList()) {
						firm.offerJobs();
					}
				}				
			};			
		}


		/**
		 * Firms register demand for the second investment phase.
		 */
		if (name.equals("registerExpansionDemand")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getList()) {
						firm.registerExpansionDemand();
					}
				}				
			};			
		}


		/**
		 * Firms update their prices and quantity targets.
		 */
		if (name.equals("updatePrices")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getList()) {
						firm.updatePrice();
					}
				}				
			};			
		}

		/**
		 * Firms make interest and redemption payments.
		 */
		if (name.equals("payInterest")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getList()) {
						firm.payInterest();
					}
				}				
			};			
		}

		/**
		 * Firms produce.
		 */
		if (name.equals("production")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getList()) {
						firm.production();
					}
				}				
			};			
		}

		/**
		 * Firms manage their liquidity position.
		 */
		if (name.equals("updateProfits")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getList()) {
						firm.updateProfits();
					}
				}				
			};			
		}

		/**
		 * Firms close their operations.
		 */
		if (name.equals("closure")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					if (firmsWithPositiveDemand.getList().size()>0) throw new RuntimeException("Unfinished investment.");
					for (final Firm firm:firms.getList()) {
						firm.close();
					}
					updateData();
				}				
			};			
		}

		return result;
	}


	/**
	 * Updates data at the end of a period.<br>
	 * Gets the data from each firm and adds them 
	 * to the sector data.
	 */
	private void updateData() {

		revenueStructure.clear();
		profitStructure.clear();
		netProfitStructure.clear();
		profitabilityStructure.clear();
		copyOfProfitabilityStructure.clear();
		netProfitabilityStructure.clear();
		workerPositions.clear();


		// Updates the data for the individual firms.
		for (Firm firm : this.firms.getList()) {

			String key = firm.getTypeAlphabetic()+firm.getStage();


			Double workers = firm.getData("hiring");
			if (!workers.isNaN()){
				if (!workerPositions.containsKey(key)) workerPositions.put(key, (int) firm.getData("hiring"));
				else workerPositions.put(key, workerPositions.get(key) + (int) firm.getData("hiring"));
			}


			//if (firmDataset.get(FirmDataLabels.REVENUE).intValue()>0){
			if (!revenueStructure.containsKey(key)){
				profitStructure.put(key, (int) firm.getData("grossProfit"));
				netProfitStructure.put(key, (int) firm.getData("netProfit"));
				revenueStructure.put(key, (int) firm.getData("revenue"));
			}
			else{
				profitStructure.put(key,profitStructure.get(key) + (int) firm.getData("grossProfit"));	
				netProfitStructure.put(key,netProfitStructure.get(key) + (int) firm.getData("netProfit"));
				revenueStructure.put(key,revenueStructure.get(key) + (int) firm.getData("revenue"));
			}
			//}	
		}


		for (String type : revenueStructure.keySet()){
			float profitability = profitStructure.get(type).floatValue()/revenueStructure.get(type).floatValue();
			float netProfitability = netProfitStructure.get(type).floatValue()/revenueStructure.get(type).floatValue();
			profitabilityStructure.put(profitability,type);
			copyOfProfitabilityStructure.put(profitability,type);
			netProfitabilityStructure.put(netProfitability,type);
		}

	}


	/**
	 * Adds a firm to the list of firms with positive investment demand.
	 */
	public void registerInvestmentDemand(Firm firm) {
		if (!firmsWithPositiveDemand.contains(firm)) firmsWithPositiveDemand.put(firm);
	}


	/**
	 * Returns the number of firms with unsaturated demand;
	 */
	public int getDemanders() {
		return firmsWithPositiveDemand.getList().size();
	}


	/**
	 * Returns the highest stage with positive revenue for @param type.<br>
	 * Returns 1 if no revenue was made.
	 */

	public int getMaxStage(String type){
		int maxstage = 1;
		for (String j : revenueStructure.keySet()){
			String column = String.valueOf(j.charAt(0));
			int row = Integer.parseInt(String.valueOf(j.charAt(1)));
			if (column.equals(type) & row > maxstage) maxstage = row; 
		}
		return maxstage;
	}


	/**
	 * Returns the highest stage in the given line of production.<br>
	 * Returns 1 if there are no firms.
	 */

	public int getMaxStage2(String type){
		int maxstage = 1;
		for (String j : firmPositions.keySet()){
			String column = String.valueOf(j.charAt(0));
			int row = Integer.parseInt(String.valueOf(j.charAt(1)));
			if (column.equals(type) & row > maxstage) maxstage = row; 
		}
		return maxstage;
	}


	/**
	 * Returns the stage with the smallest firm population if it 
	 * is populated by less than minPopulationRequirement firms.
	 */

	public int getMinPopulation(String type, int minPopulationRequirement){
		int minPopulation = Integer.MAX_VALUE;
		int smallestStage = 0;
		for (String j : firmPositions.keySet()){
			String column = String.valueOf(j.charAt(0));
			if (column.equals(type) & firmPositions.get(j)<minPopulation){
				minPopulation = firmPositions.get(j);
				smallestStage = Integer.parseInt(String.valueOf(j.charAt(1)));
			}
		}
		if (minPopulation<minPopulationRequirement) return smallestStage;
		return 0;
	}



	@Override
	public SectorDataset getDataset() {

		SectorDataset data = firms.collectData();

		if (timer.getPeriod().intValue()>1){
			if ((Double.isNaN(nominalGDPBase) | nominalGDPBase == 0D)) {
				nominalGDPBase = data.getSum("revenue","consumerProducer=1")
						+ data.getSum("machineInvestment","")
						+ data.getSum("operationsInvestment","")
						- data.getSum("costsOfGoodsSold","");
				data.putSectorialValue("nominalGDP", Double.valueOf(nominalGDPBase));
				data.putSectorialValue("nominalGDP_index", Double.valueOf(100));
			}
			else {
				// Calculates the index of nominal GDP
				double GDP = data.getSum("revenue","consumerProducer=1")
						+ data.getSum("machineInvestment","")
						+ data.getSum("operationsInvestment","")
						- data.getSum("costsOfGoodsSold","");
				double nominalIndex = GDP / nominalGDPBase * 100;
				data.putSectorialValue("nominalGDP", Double.valueOf(GDP));
				data.putSectorialValue("nominalGDP_index", Double.valueOf(nominalIndex));
			}
		}

		data.putSectorialValue("maxStageB", (double) getMaxStage2("B"));
		data.putSectorialValue("firmFailures", (double) firmFailures.size());
		data.putSectorialValue("firmNumber", (double) firms.getList().size());
		data.putSectorialValue("newFirms", (double) newFirms);

		for (Firm firm:firmFailures) {

			String type = firm.getTypeAlphabetic();
			int stage = firm.getStage();
			String location = type+stage;
			int remainingFirms = firmPositions.get(location)-1;
			if (remainingFirms<0) throw new RuntimeException("False number of firms.");
			firmPositions.put(location, remainingFirms);
			firms.remove(firm);
			firm.kill();
			getInvestmentBank().newProject("Firm"); // The max. number of firms in the economy is constant.
		}
		firmFailures.clear();

		return data;
	}



	/**
	 * Kills the sector.
	 */
	public void kill() {
		for (Firm firm : firms.getList()) firm.kill() ;
		this.firms.clear();
	}



}