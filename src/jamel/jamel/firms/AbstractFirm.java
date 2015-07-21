package jamel.jamel.firms;

import jamel.basic.agent.AgentDataset;
import jamel.basic.agent.BasicAgentDataset;
import jamel.basic.util.Period;
import jamel.basic.util.Timer;
import jamel.jamel.firms.managers.CapitalManager;
import jamel.jamel.firms.managers.PricingManager;
import jamel.jamel.firms.managers.ProductionManager;
import jamel.jamel.firms.managers.WorkforceManager;
import jamel.jamel.firms.util.Factory;
import jamel.jamel.roles.Supplier;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.util.BasicMemory;
import jamel.jamel.util.Memory;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.Cheque;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.Supply;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

/**
 * An abstract firm.
 */
public abstract class AbstractFirm implements Firm {

	@SuppressWarnings("javadoc")
	public final static String CAPITAL_PROPENSITY2DISTRIBUTE = "capital.propensityToDistribute";

	@SuppressWarnings("javadoc")
	public final static String CAPITAL_TARGET = "capital.target";

	@SuppressWarnings("javadoc")
	public final static String INVENTORY_NORMAL_LEVEL = "inventory.normalLevel";

	@SuppressWarnings("javadoc")
	public final static String LABOUR_CONTRACT_MAX = "labourContract.max";

	@SuppressWarnings("javadoc")
	public final static String LABOUR_CONTRACT_MIN = "labourContract.min";

	@SuppressWarnings("javadoc")
	public static final String MEM_DIVIDEND = "dividend";

	@SuppressWarnings("javadoc")
	public final static String NORMAL_VACANCY_RATE = "vacancy.normalRate";

	@SuppressWarnings("javadoc")
	public final static String PRICE_FLEXIBILITY = "price.flexibility";

	@SuppressWarnings("javadoc")
	public final static String PRODUCTION_CAPACITY = "production.capacity";

	@SuppressWarnings("javadoc")
	public final static String PRODUCTION_TIME = "production.time";

	@SuppressWarnings("javadoc")
	public final static String PRODUCTIVITY = "production.productivity";

	@SuppressWarnings("javadoc")
	public final static String PROPENSITY2SELL = "inventory.propensity2sell";

	@SuppressWarnings("javadoc")
	public final static String SELLING_CAPACITY = "sales.capacity";

	@SuppressWarnings("javadoc")
	public final static String UTILIZATION_RATE_FLEXIBILITY = "utilizationRate.flexibility";

	@SuppressWarnings("javadoc")
	public final static String UTILIZATION_RATE_INITIAL_VALUE = "utilizationRate.initialValue";

	@SuppressWarnings("javadoc")
	public final static String WAGE_FLEX_DOWN = "wage.flexibility.downward";

	@SuppressWarnings("javadoc")
	public final static String WAGE_FLEX_UP = "wage.flexibility.upward";

	@SuppressWarnings("javadoc")
	public final static String WAGE_INITIAL_VALUE = "wage.initialValue";

	@SuppressWarnings("javadoc")
	public final static String WAGE_MINIMUM = "wage.minimum";

	/** A flag that indicates if the data of the firm is to be exported. */
	private boolean exportData;

	/** The account. */
	protected final BankAccount account;

	/** A flag that indicates if the firm is bankrupted. */
	protected boolean bankrupted = false;

	/** The capital manager. */
	protected final CapitalManager capitalManager;

	/** Date of creation. */
	protected final int creation;

	/** The data of the agent. */
	protected AgentDataset data;

	/** The factory. */
	protected final Factory factory;

	/** The history of the firm. */
	protected final LinkedList<String> history = new LinkedList<String>() {

		@Override
		public boolean add(String string) {
			final boolean result;
			if (recordHistoric) {
				result=super.add(string);
			}
			else {
				result=false;
			}
			return result;
		}
	};

	/** The memory. */
	protected final Memory memory;

	/** The name. */
	protected final String name;

	/** The pricing manager. */
	protected final PricingManager pricingManager;

	/** The production manager. */
	protected final ProductionManager productionManager;

	/** The random. */
	final protected Random random;

	/** A flag that indicates if the agent records its history. */
	protected boolean recordHistoric = false;

	/** The sector. */
	protected final IndustrialSector sector;

	/** The supply. */
	protected Supply supply;

	/** The timer. */
	final protected Timer timer;

	/** The employer behavior. */
	protected final WorkforceManager workforceManager;

	/**
	 * Creates a new firm.
	 * @param name the name.
	 * @param sector the sector.
	 */
	public AbstractFirm(String name,IndustrialSector sector) {
		this.history.add("Creation: "+name);
		this.name=name;
		this.sector=sector;
		this.timer = this.sector.getTimer();
		this.creation = this.timer.getPeriod().intValue();
		this.random = this.sector.getRandom();
		this.memory = new BasicMemory(timer, 24);
		this.account = this.sector.getNewAccount(this);
		this.factory = getNewFactory();
		this.capitalManager = getNewCapitalManager();
		this.pricingManager = getNewPricingManager();
		this.workforceManager = getNewWorkforceManager();
		this.productionManager = getNewProductionManager();
	}

	/**
	 * Exports agent data in a csv file.
	 * @throws IOException in the case of an I/O exception.
	 */
	private void exportData() throws IOException {
		if (this.exportData) {
			// TODO gerer la localisation du dossier exports, son existence
			final File outputFile = new File("exports/"+sector.getSimulationID()+"-"+this.name+".csv");
			if (!outputFile.exists()) {
				this.data.exportHeadersTo(outputFile);
			}
			this.data.exportTo(outputFile);
		}
	}

	/**
	 * Creates and returns a new commodity supply.
	 * @return a new {@linkplain Supply}.
	 */
	protected Supply createSupply() {
		final Supply supply;
		final Period validPeriod = timer.getPeriod();
		final long initialSize = Math.min((long) (sector.getParam(PROPENSITY2SELL)*factory.getFinishedGoodsVolume()), (long) (sector.getParam(SELLING_CAPACITY)*factory.getMaxUtilAverageProduction()));

		if (pricingManager.getPrice()==null) {
			pricingManager.updatePrice();
		}
		final Double price = pricingManager.getPrice();

		final long initialValue;

		if (initialSize>0) {
			initialValue=(long) (price*initialSize);
		}
		else {
			initialValue=0;
		}

		supply = new Supply() {

			private AgentDataset dataset = new BasicAgentDataset("Supply");

			private long grossProfit = 0;

			private long salesValue=0;

			private long salesValueAtCost=0;

			private long salesVolume=0;

			private long volume=initialSize; 

			@Override
			public Commodities buy(long demand,Cheque cheque) {
				if (!validPeriod.isPresent()) {
					throw new AnachronismException("Bad period.");
				}
				if (demand>this.volume) {
					throw new IllegalArgumentException("Demand cannot exceed supply.");
				}
				if ((long)(this.getPrice()*demand)!=cheque.getAmount()) {
					throw new IllegalArgumentException("Cheque amount : expected <"+(long) (demand*this.getPrice())+"> but was <"+cheque.getAmount()+">");
				}
				account.deposit(cheque);
				this.volume-=demand;
				this.salesValue+=cheque.getAmount();
				this.salesVolume+=demand;
				final Commodities sales = factory.getCommodities(demand);
				this.salesValueAtCost += sales.getValue();
				this.grossProfit = this.salesValue-this.salesValueAtCost;
				return sales;
			}

			@Override
			public void close() {
				this.dataset.put("supply.vol", (double) initialSize);
				this.dataset.put("supply.val", (double) initialValue);
				this.dataset.put("sales.vol", (double) salesVolume);
				this.dataset.put("sales.val", (double) salesValue);
				this.dataset.put("sales.costValue", (double) salesValueAtCost);
				this.dataset.put("grossProfit", (double) this.grossProfit);		
			}

			@Override
			public AgentDataset getData() {
				return this.dataset;
			}

			@Override
			public double getGrossProfit() {
				return this.grossProfit;
			}

			@Override
			public double getPrice() {
				return price;
			}

			@Override
			public long getPrice(long volume) {
				return (long) (price*volume);
			}

			@Override
			public double getSalesRatio() {
				// TODO: a revoir. Est-ce ici qu'il faut calculer a ?
				final double result;
				if (initialSize>0) {
					result = ((double) this.salesVolume)/initialSize; 
				}
				else {
					result=0;
				}
				return result;
			}

			@Override
			public Supplier getSupplier() {
				return AbstractFirm.this;
			}

			@Override
			public long getVolume() {
				return this.volume;
			}

			@Override
			public String toString() {
				return "Supply by "+name+": price <"+price+">, volume <"+volume+">"; 
			}

		};
		return supply;
	}


	/**
	 * Returns the age of the firm (= the number of periode since its creation).
	 * @return the age of the firm.
	 */
	protected int getAge() {
		return timer.getPeriod().intValue()-this.creation;
	}

	/**
	 * Returns the inventory ratio.<br>
	 * If inventoryRatio > 1 : the volume of finished goods exceeds the normal volume,<br>
	 * If inventoryRatio = 1 : the volume of finished goods meets the normal volume,<br>
	 * If inventoryRatio < 1 : the volume of finished goods is under the normal volume.
	 * @return the inventory ratio.
	 */
	protected double getInventoryRatio() {
		return this.factory.getFinishedGoodsVolume()/(sector.getParam(INVENTORY_NORMAL_LEVEL)*this.factory.getMaxUtilAverageProduction());
	}

	/**
	 * Creates and returns a new capital manager.
	 * @return a new {@linkplain CapitalManager}.
	 */
	abstract protected CapitalManager getNewCapitalManager();

	/**
	 * Creates and returns a new agent dataset.
	 * @return a new agent dataset.
	 */
	protected AgentDataset getNewDataset() {
		return new BasicAgentDataset(name);
	}

	/**
	 * Creates and returns a new factory.
	 * @return a new factory.
	 */
	abstract protected Factory getNewFactory();

	/**
	 * Creates and returns a new pricing manager.
	 * @return a new pricing manager.
	 */
	abstract protected PricingManager getNewPricingManager();

	/**
	 * Creates and returns a new {@linkplain ProductionManager}. 
	 * @return a new {@linkplain ProductionManager}.
	 */
	abstract protected ProductionManager getNewProductionManager();

	/**
	 * Returns a new {@link WorkforceManager}.
	 * @return a new {@link WorkforceManager}.
	 */
	abstract protected WorkforceManager getNewWorkforceManager();

	/**
	 * Updates the data.
	 */
	protected void updateData() {

		this.data.put("firms", 1.);
		this.data.put("age", (double) getAge());

		this.data.putAll(this.workforceManager.getData());
		this.data.putAll(this.pricingManager.getData());
		this.data.putAll(this.factory.getData());
		this.data.putAll(this.capitalManager.getData());
		this.data.putAll(this.supply.getData());

		// TODO: trouver un manager pour calculer a. Factory ?
		this.data.put("inventories.fg.vol.normal", sector.getParam(INVENTORY_NORMAL_LEVEL)*factory.getMaxUtilAverageProduction());						

		if (bankrupted){
			this.data.put("bankruptcies", 1.);
		}
		else {
			this.data.put("bankruptcies", 0.);					
		}

	}

	@Override
	public void close() {

		this.pricingManager.close();
		this.capitalManager.close();
		this.workforceManager.close();
		this.factory.close();
		this.supply.close();

		this.updateData();

		try {
			this.exportData();
		} catch (IOException e) {
			throw new RuntimeException("Error while exporting firm data",e);
		}
	}

	@Override
	public long getBookValue() {
		return this.capitalManager.getCapital();
	}

	@Override
	public AgentDataset getData() {
		return this.data;
	}

	@Override
	public JobOffer getJobOffer() {
		return this.workforceManager.getJobOffer();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Supply getSupply() {
		final Supply result;
		if (this.supply.getVolume()>0){
			result=this.supply;
		}
		else {
			result=null;
		}
		return result;
	}

	@Override
	public void goBankrupt() {
		this.bankrupted = true;
		this.factory.bankrupt();
	}

	@Override
	public boolean isBankrupted() {
		return this.bankrupted;
	}

	@Override
	public boolean isSolvent() {
		return this.capitalManager.isSolvent();
	}

	@Override
	public void open() {
		this.data=getNewDataset();
		this.history.add("");
		this.history.add("Period: "+timer.getPeriod().intValue());
		this.supply=null;
		if (this.bankrupted) {
			this.capitalManager.bankrupt();
			this.workforceManager.layoff();
		}
		else {
			this.factory.open();
			this.pricingManager.open();
			this.capitalManager.open();
			this.workforceManager.open();
		}
	}

	@Override
	public void payDividend() {
		if (this.bankrupted) {
			throw new RuntimeException("This firm is bankrupted.");
		}
		this.capitalManager.payDividend();
	}

	@Override
	public void prepareProduction() {

		if (this.bankrupted) {
			throw new RuntimeException("This firm is bankrupted.");
		}
		// *** Updates the price.

		this.pricingManager.updatePrice();

		// *** Updates the wage.

		this.workforceManager.updateWage();

		// *** Updates the targeted production level.

		this.productionManager.updateCapacityUtilizationTarget();

		// *** Updates the workforce and computes the payroll.

		this.workforceManager.updateWorkforce();

		// *** Secures financing.
		
		// TODO: c'est au capital manager de faire les taches ci-dessous.

		final long payroll = this.workforceManager.getPayroll();

		if ( payroll>this.account.getAmount() ) {
			account.lend(payroll-this.account.getAmount()) ;
		}
		if (account.getAmount() < payroll) {
			throw new RuntimeException("Production is not financed.") ;
		}

	}

	@Override
	public void production() {
		if (this.bankrupted) {
			throw new RuntimeException("This firm is bankrupted.");
		}
		this.workforceManager.payWorkers();
		factory.process(this.workforceManager.getLaborPowers()) ;
		this.supply = createSupply();
	}

}

// ***