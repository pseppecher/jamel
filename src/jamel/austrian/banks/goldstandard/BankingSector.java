package jamel.austrian.banks.goldstandard;


import jamel.austrian.banks.AbstractBankingSector;
import jamel.austrian.banks.CommercialBank;
import jamel.austrian.widgets.InvestmentProject;
import jamel.basic.Circuit;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.Phase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;



/**
 * Represents the banking sector.
 */
public class BankingSector extends AbstractBankingSector {

	
	/**
	 * Creates a new sector for banks.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public BankingSector(String name, Circuit aCircuit) {
		super(name, aCircuit);
	}

	
	/**
	 * Creates banks.
	 * @param lim the number of banks to create.
	 * @return a list containing the new banks. 
	 */
	protected List<CommercialBank> createBanks(int lim) {
		final List<CommercialBank> result = new ArrayList<CommercialBank>(lim);
		try {
			
			Float lendingRate = getParam("lendingRate");
			Float savingsRate = getParam("savingsRate");
			Integer initialMoney = getParam("initialMoney").intValue();
			
			
			for(int index=0;index<lim;index++) {
				this.countAgents++;
				
				final String name = "Bank"+this.countAgents;
				CommercialBank newBank = (CommercialBank) Class.forName(this.agentType,false,ClassLoader.getSystemClassLoader()).
						getConstructor(String.class,Circuit.class,BankingSector.class,Float.class,Float.class,Integer.class).newInstance(
								name, circuit, BankingSector.this, lendingRate, savingsRate, initialMoney);
				// Ownership is defined after instantiation.
				
				result.add(newBank);
			}
		} catch (Exception e) {
			throw new RuntimeException("Bank creation failure",e); 
		}
		return result;
	}
	
	
	@Override
	public Object askFor(String key) {
		throw new RuntimeException("Not used");
	}

	@Override
	public Phase getPhase(String name) {
		Phase result = null;
		
		if (name.equals("opening")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					
					newBanks=0;
					LinkedList<InvestmentProject> projects = getInvestmentBank().getNewBankProjects();

					if (projects.size()>0){
						for (InvestmentProject project:projects){

							float lendingRate = getMarket("loans").getPriceLevel();
							float savingsRate = getMarket("savings").getPriceLevel();							
							
							CommercialBank newBank;
							try {
								
								newBank = (CommercialBank) Class.forName(BankingSector.this.agentType,false,ClassLoader.getSystemClassLoader()).getConstructor(
										InvestmentProject.class,Circuit.class,BankingSector.class,Float.class,Float.class).
										newInstance(project, circuit, BankingSector.this, lendingRate, savingsRate);
								
							}
							catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException("Firm creation failure"); 
							}
							banks.put(newBank);
							newBanks++;
						}
					}
					
					for (CommercialBank bank:bankFailures) {
						banks.remove(bank);
						bank.terminateOperations();
						getInvestmentBank().newProject("Bank"); // The max. number of banks in the economy is constant.
					}
					bankFailures.clear();
					
					for (final CommercialBank bank:banks.getList()) {
						bank.open();
					}
				}				
			};	
		}
	
		
		else if (name.equals("offering")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final CommercialBank bank:banks.getList()) {
						bank.makeOffer();
					}
				}				
			};			
		}
		
		else if (name.equals("updateInterestRates")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final CommercialBank bank:banks.getList()) {
						bank.updateInterestRates();
					}
				}				
			};			
		}
		
		else if (name.equals("payInterest")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final CommercialBank bank:banks.getList()) {
						bank.payInterest();
					}
				}				
			};			
		}
		
		else if (name.equals("updateProfits")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final CommercialBank bank:banks.getList()) {
						bank.updateProfits();
					}
				}				
			};			
		}
		
		else if (name.equals("closure")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final CommercialBank bank:banks.getList()) {
						bank.close();
					}
				}				
			};			
		}
		
		return result;
	}

}
