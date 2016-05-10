package jamel.jamel.firms;

import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;
import jamel.basic.util.Timer;
import jamel.jamel.capital.StockCertificate;
import jamel.jamel.firms.factory.Factory;
import jamel.jamel.firms.managers.CapitalManager;
import jamel.jamel.firms.managers.PricingManager;
import jamel.jamel.firms.managers.ProductionManager;
import jamel.jamel.firms.managers.SalesManager;
import jamel.jamel.firms.managers.WorkforceManager;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.Supply;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
	public final static String INVENTORY_NORMAL_LEVEL = "inventories.normalLevel";

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
	public final static String PROPENSITY2SELL = "inventories.propensity2sell";

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

	/** The capital manager. */
	protected CapitalManager capitalManager;

	/** A flag that indicates if the data of the firm is to be exported. */
	private boolean exportData;

	/** The factory. */
	private Factory factory;

	/** The pricing manager. */
	private PricingManager pricingManager;

	/** The production manager. */
	private ProductionManager productionManager;

	/** The marketing manager. */
	private SalesManager salesManager;

	/**
	 * The type of production.
	 */
	private String typeOfProduction = null;

	/** The employer behavior. */
	private WorkforceManager workforceManager;

	/** The account. */
	protected final BankAccount account;

	/** A flag that indicates if the firm is bankrupted. */
	protected boolean bankrupted = false;

	/** Date of creation. */
	protected final int creation;

	/** The data of the agent. */
	protected AgentDataset data;

	/** The name of this firm. */
	protected final String name;

	/** A flag that indicates if this firm is open or not. */
	protected boolean open;

	/** The current period. */
	protected Integer period = null;

	/** The random. */
	final protected Random random;

	/** The sector. */
	protected final IndustrialSector sector;

	/** The timer. */
	final protected Timer timer;

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
			final File outputFile = new File("exports/" + sector.getSimulationID() + "-" + this.name + ".csv");
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
	 * @return the capitalManager
	 */
	protected CapitalManager getCapitalManager() {
		return capitalManager;
	}

	/**
	 * @return the factory
	 */
	protected Factory getFactory() {
		return factory;
	}

	/**
	 * Creates and returns a new agent dataset.
	 * 
	 * @return a new agent dataset.
	 */
	protected AgentDataset getNewDataset() {
		return new BasicAgentDataset(name);
	}

	/**
	 * @return the pricingManager
	 */
	protected PricingManager getPricingManager() {
		return pricingManager;
	}

	/**
	 * @return the productionManager
	 */
	protected ProductionManager getProductionManager() {
		return productionManager;
	}

	/**
	 * @return the salesManager
	 */
	protected SalesManager getSalesManager() {
		return salesManager;
	}

	/**
	 * @return the workforceManager
	 */
	protected WorkforceManager getWorkforceManager() {
		return workforceManager;
	}

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
	 * @param capitalManager
	 *            the capitalManager to set
	 */
	protected void setCapitalManager(CapitalManager capitalManager) {
		if (this.capitalManager != null) {
			throw new RuntimeException("Not null");
		}
		this.capitalManager = capitalManager;
	}

	/**
	 * @param factory
	 *            the factory to set
	 */
	protected void setFactory(Factory factory) {
		if (this.factory != null) {
			throw new RuntimeException("Not null");
		}
		this.factory = factory;
	}

	/**
	 * @param pricingManager
	 *            the pricingManager to set
	 */
	protected void setPricingManager(PricingManager pricingManager) {
		if (this.pricingManager != null) {
			throw new RuntimeException("Not null");
		}
		this.pricingManager = pricingManager;
	}

	/**
	 * @param productionManager
	 *            the productionManager to set
	 */
	protected void setProductionManager(ProductionManager productionManager) {
		if (this.productionManager != null) {
			throw new RuntimeException("Not null");
		}
		this.productionManager = productionManager;
	}

	/**
	 * @param salesManager
	 *            the salesManager to set
	 */
	protected void setSalesManager(SalesManager salesManager) {
		if (this.salesManager != null) {
			throw new RuntimeException("Not null");
		}
		this.salesManager = salesManager;
	}

	/**
	 * Sets the type of production.
	 * 
	 * @param typeOfProduction
	 *            the type of production to set.
	 */
	protected void setTypeOfProduction(String typeOfProduction) {
		if (this.typeOfProduction != null) {
			throw new RuntimeException("Type of production already set.");
		}
		this.typeOfProduction = typeOfProduction;
	}

	/**
	 * @param workforceManager
	 *            the workforceManager to set
	 */
	protected void setWorkforceManager(WorkforceManager workforceManager) {
		if (this.workforceManager != null) {
			throw new RuntimeException("Not null");
		}
		this.workforceManager = workforceManager;
	}

	/**
	 * Updates the data.
	 */
	protected void updateData() {

		this.data.put("firms", 1);
		this.data.put("age", getAge());

		this.data.putAll(this.workforceManager.getData());
		this.data.putAll(this.pricingManager.getData());
		this.data.putAll(this.factory.getData());
		this.data.putAll(this.capitalManager.getData());
		this.data.putAll(this.salesManager.getData());

		if (bankrupted) {
			this.data.put("bankruptcies", 1);
		} else {
			this.data.put("bankruptcies", 0);
		}

		this.data.putInfo("name", this.name);
		this.data.putInfo("account", this.account.getInfo());

	}

	@Override
	public void close() {
		if (!this.open) {
			throw new RuntimeException("Already closed.");
		}
		this.open = false;

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

	/**
	 * Issues the specified number of new shares.
	 * 
	 * @param shares
	 *            the number of new shares to be issued.
	 * @return a {@link StockCertificate} that encapsulates the new shares.
	 */
	@Override
	public StockCertificate[] getNewShares(List<Integer> shares) {
		return this.capitalManager.getNewShares(shares);
	}

	@Override
	public Supply getSupply() {
		return this.salesManager.getSupply();
	}

	/**
	 * @return the typeOfProduction
	 */
	public String getTypeOfProduction() {
		return this.typeOfProduction;
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
		this.factory.delete();
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
		if (this.open) {
			throw new RuntimeException("Already open.");
		}
		this.open = true;
		if (this.period == null) {
			this.period = this.timer.getPeriod().intValue();
		} else {
			this.period++;
			if (this.period != timer.getPeriod().intValue()) {
				throw new AnachronismException("Bad period");
			}
		}
		this.data = getNewDataset();
		this.openManagers();
		getFactory().setInventoryNormalLevel(sector.getParam(INVENTORY_NORMAL_LEVEL));
		if (this.bankrupted) {
			this.capitalManager.bankrupt();
			this.workforceManager.layoff();
			this.factory.cancel();
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