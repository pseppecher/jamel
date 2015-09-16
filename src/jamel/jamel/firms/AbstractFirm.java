package jamel.jamel.firms;

import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;
import jamel.basic.util.Timer;
import jamel.jamel.capital.StockCertificate;
import jamel.jamel.firms.factory.Factory;
import jamel.jamel.firms.managers.CapitalManager;
import jamel.jamel.firms.managers.SalesManager;
import jamel.jamel.firms.managers.PricingManager;
import jamel.jamel.firms.managers.ProductionManager;
import jamel.jamel.firms.managers.WorkforceManager;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.Supply;

import java.io.File;
import java.io.IOException;
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

	/** The name. */
	protected final String name;

	/** A flag that indicates if this firm is open or not. */
	protected boolean open;

	/** The current period. */
	protected Integer period = null;

	/** The pricing manager. */
	protected final PricingManager pricingManager;

	/** The production manager. */
	protected final ProductionManager productionManager;

	/** The random. */
	final protected Random random;

	/** The marketing manager. */
	protected final SalesManager salesManager;

	/** The sector. */
	protected final IndustrialSector sector;

	/** The timer. */
	final protected Timer timer;

	/** The employer behavior. */
	protected final WorkforceManager workforceManager;

	/**
	 * Creates a new firm.
	 * 
	 * @param name
	 *            the name.
	 * @param sector
	 *            the sector.
	 */
	public AbstractFirm(String name, IndustrialSector sector) {
		this.name = name;
		this.sector = sector;
		this.timer = this.sector.getTimer();
		this.creation = this.timer.getPeriod().intValue();
		this.random = this.sector.getRandom();
		this.account = this.sector.getNewAccount(this);
		this.factory = getNewFactory();
		this.capitalManager = getNewCapitalManager();
		this.pricingManager = getNewPricingManager();
		this.workforceManager = getNewWorkforceManager();
		this.productionManager = getNewProductionManager();
		this.salesManager = getNewSalesManager();
	}

	/**
	 * Exports agent data in a csv file.
	 * 
	 * @throws IOException
	 *             in the case of an I/O exception.
	 */
	private void exportData() throws IOException {
		if (this.exportData) {
			// TODO gerer la localisation du dossier exports, son existence
			final File outputFile = new File("exports/"
					+ sector.getSimulationID() + "-" + this.name + ".csv");
			if (!outputFile.exists()) {
				this.data.exportHeadersTo(outputFile);
			}
			this.data.exportTo(outputFile);
		}
	}

	/**
	 * Closes all the managers of this firm.
	 */
	protected void closeManagers() {
		this.salesManager.close();
		this.pricingManager.close();
		this.capitalManager.close();
		this.workforceManager.close();
		this.factory.close();
	}

	/**
	 * Returns the age of the firm (= the number of periode since its creation).
	 * 
	 * @return the age of the firm.
	 */
	protected int getAge() {
		return timer.getPeriod().intValue() - this.creation;
	}

	/**
	 * Creates and returns a new capital manager.
	 * 
	 * @return a new {@linkplain CapitalManager}.
	 */
	abstract protected CapitalManager getNewCapitalManager();

	/**
	 * Creates and returns a new agent dataset.
	 * 
	 * @return a new agent dataset.
	 */
	protected AgentDataset getNewDataset() {
		return new BasicAgentDataset(name);
	}

	/**
	 * Creates and returns a new factory.
	 * 
	 * @return a new factory.
	 */
	abstract protected Factory getNewFactory();

	/**
	 * Creates and returns a new pricing manager.
	 * 
	 * @return a new pricing manager.
	 */
	abstract protected PricingManager getNewPricingManager();

	/**
	 * Creates and returns a new {@linkplain ProductionManager}.
	 * 
	 * @return a new {@linkplain ProductionManager}.
	 */
	abstract protected ProductionManager getNewProductionManager();

	/**
	 * Creates and returns a new sales manager.
	 * 
	 * @return a new {@linkplain SalesManager}.
	 */
	protected abstract SalesManager getNewSalesManager();

	/**
	 * Returns a new {@link WorkforceManager}.
	 * 
	 * @return a new {@link WorkforceManager}.
	 */
	abstract protected WorkforceManager getNewWorkforceManager();

	/**
	 * Opens all the managers of this firm.
	 */
	protected void openManagers() {
		this.factory.open();
		this.capitalManager.open();
		this.pricingManager.open();
		this.productionManager.open();
		this.salesManager.open();
		this.workforceManager.open();
	}

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
		this.data.putAll(this.salesManager.getData());

		if (bankrupted) {
			this.data.put("bankruptcies", 1.);
		} else {
			this.data.put("bankruptcies", 0.);
		}

	}

	@Override
	public void clearOwnership() {
		this.capitalManager.clearOwnership();
	}

	@Override
	public void close() {
		if(!this.open) {
			throw new RuntimeException("Already closed.");
		}
		this.open=false;

		this.closeManagers();

		this.updateData();

		try {
			this.exportData();
		} catch (IOException e) {
			throw new RuntimeException("Error while exporting firm data", e);
		}
	}

	@Override
	public Long getBookValue() {
		return (Long) this.capitalManager.askFor("capital");
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
	public StockCertificate getNewShares(Integer nShares) {
		return this.capitalManager.getNewShares(nShares);
	}

	@Override
	public Supply getSupply() {
		return this.salesManager.getSupply();
	}

	@Override
	public long getValueOfAssets() {
		return (Long) this.capitalManager.askFor("assets");
	}

	@Override
	public long getValueOfLiabilities() {
		return (Long) this.capitalManager.askFor("liabilities");
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
	public boolean isCancelled() {
		return this.bankrupted;
	}

	@Override
	public boolean isSolvent() {
		return this.capitalManager.isSolvent();
	}

	@Override
	public void open() {
		if(this.open) {
			throw new RuntimeException("Already open.");
		}
		this.open=true;
		if (this.period==null) {
			this.period=this.timer.getPeriod().intValue();
		}
		else {
			this.period++;
			if (this.period!=timer.getPeriod().intValue()) {
				throw new AnachronismException("Bad period");
			}
		}
		this.data = getNewDataset();
		if (this.bankrupted) {
			this.capitalManager.bankrupt();
			this.workforceManager.layoff();
		} else {
			this.openManagers();
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

		this.capitalManager.secureFinancing(this.workforceManager.getPayroll());

	}

	@Override
	public void production() {
		if (this.bankrupted) {
			throw new RuntimeException("This firm is bankrupted.");
		}
		this.workforceManager.payWorkers();
		factory.process(this.workforceManager.getLaborPowers());
		this.salesManager.createSupply();
	}

}

// ***