package jamel.austrian.banks;



import jamel.austrian.banks.BankingSector;
import jamel.austrian.sfc.SFCSector;
import jamel.austrian.widgets.InvestmentProject;
import jamel.basic.Circuit;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.AgentSet;
import jamel.basic.sector.BasicAgentSet;
import jamel.basic.sector.Phase;
import jamel.basic.data.SectorDataset;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;


/**
 * Represents the banking sector.
 */
public class BankingSector extends SFCSector {

	/** The collection of agents. */
	protected final AgentSet<Bank> banks;
	
	/** The list of defaulting banks. */
	private final LinkedList<CommercialBank> bankFailures;

	/** The number of newly created banks. */
	private int  newBanks;

	protected String typeOfTheCommercialBank;

	
	/**
	 * Creates a new sector for banks.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public BankingSector(String name, Circuit aCircuit) {
		super(name, aCircuit);
		this.banks = new BasicAgentSet<Bank>(this.random);
		this.bankFailures = new LinkedList<CommercialBank>();
	}

	
	@Override
	public void doEvent(Element event) {
		if (event.getNodeName().equals("new")) {
			final int size = Integer.parseInt(event.getAttribute("size"));
			this.banks.putAll(this.createBanks(this.agentType,size));
			getInvestmentBank().setBankID(size);
		}
		else if (event.getNodeName().equals("assignOwnership")) {
			for (Bank bank:banks.getList()) bank.setOwnershipStructure();
		}
		else {
			throw new RuntimeException("Unknown event or not yet implemented: "+event.getNodeName());			
		}
	}
	
	
	/**
	 * Creates banks.
	 * @param type the type of banks to create.
	 * @param lim the number of banks to create.
	 * @return a list containing the new banks. 
	 */
	private List<Bank> createBanks(String type, int lim) {
		final List<Bank> result = new ArrayList<Bank>(lim);
		try {
			
			Float lendingRate = initialConditions.lendingRate;
			Float savingsRate = initialConditions.savingsRate; 
			Integer initialMoney = initialConditions.initialMoney;
			
			
			for(int index=0;index<lim;index++) {
				this.countAgents++;
				
				final String name = "Bank"+this.countAgents;
				CommercialBank newBank = (CommercialBank) Class.forName(type,false,ClassLoader.getSystemClassLoader()).
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
								
								newBank = (CommercialBank) Class.forName(BankingSector.this.typeOfTheCommercialBank,false,ClassLoader.getSystemClassLoader()).getConstructor(
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
					
					for (final Bank bank:banks.getList()) {
						bank.open();
					}
				}				
			};	
		}
	
		
		else if (name.equals("offering")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Bank bank:banks.getList()) {
						bank.makeOffer();
					}
				}				
			};			
		}
		
		else if (name.equals("updateInterestRates")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Bank bank:banks.getList()) {
						bank.updateInterestRates();
					}
				}				
			};			
		}
		
		else if (name.equals("payInterest")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Bank bank:banks.getList()) {
						bank.payInterest();
					}
				}				
			};			
		}
		
		else if (name.equals("updateProfits")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Bank bank:banks.getList()) {
						bank.updateProfits();
					}
				}				
			};			
		}
		
		else if (name.equals("closure")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Bank bank:banks.getList()) {
						bank.close();
					}
				}				
			};			
		}
		
		return result;
	}
	
	
	/**
	 * An agents files bankruptcy.
	 */
	public void bankruptcy(CommercialBank aBankruptAgent){	
		bankFailures.add(aBankruptAgent);	
	}
	
	
	@Override
	public SectorDataset getDataset() {
		SectorDataset data = banks.collectData();
		data.putSectorialValue("bankFailures", (double) bankFailures.size());
		data.putSectorialValue("bankNumber", (double) banks.getList().size());
		data.putSectorialValue("newBanks", (double) newBanks);

		return data;
	}
	
	
	/**
	 * Returns a bank selected at random.
	 */
	public CommercialBank selectRandomBank() {
		return (CommercialBank) banks.getRandomAgent();
	}

}
