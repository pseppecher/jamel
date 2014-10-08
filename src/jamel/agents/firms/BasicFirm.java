/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2014, Pascal Seppecher and contributors.
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
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.]
 * [JAMEL uses JFreeChart, copyright by Object Refinery Limited and Contributors. 
 * JFreeChart is distributed under the terms of the GNU Lesser General Public Licence (LGPL). 
 * See <http://www.jfree.org>.]
 */

package jamel.agents.firms;

import jamel.Circuit;
import jamel.JamelObject;
import jamel.agents.firms.managers.BasicCapitalManager;
import jamel.agents.firms.managers.BasicPricingManager;
import jamel.agents.firms.managers.BasicPublicRelationManager;
import jamel.agents.firms.managers.CapitalManager;
import jamel.agents.firms.managers.PricingManager;
import jamel.agents.firms.managers.BasicProductionManager;
import jamel.agents.firms.managers.ProductionManager;
import jamel.agents.firms.managers.PublicRelationManager;
import jamel.agents.firms.managers.PurchasingManager;
import jamel.agents.firms.managers.BasicStoreManager;
import jamel.agents.firms.managers.BasicWorkforceManager;
import jamel.agents.firms.managers.StoreManager;
import jamel.agents.firms.managers.WorkforceManager;
import jamel.agents.firms.util.BasicMediator;
import jamel.agents.firms.util.FirmComponent;
import jamel.agents.firms.util.Mediator;
import jamel.agents.firms.util.ProductionType;
import jamel.agents.roles.CapitalOwner;
import jamel.agents.roles.Worker;
import jamel.spheres.monetarySphere.Account;
import jamel.spheres.monetarySphere.Check;
import jamel.spheres.monetarySphere.Quality;
import jamel.spheres.realSphere.Factories;
import jamel.spheres.realSphere.Factory;
import jamel.spheres.realSphere.FinalFactory;
import jamel.spheres.realSphere.Goods;
import jamel.util.markets.GoodsOffer;
import jamel.util.markets.JobOffer;

/**
 * Represents a firm.
 */
public class BasicFirm extends JamelObject implements Firm, FirmComponent {

	/** A flag that indicates whether the firm is bankrupt or not. */
	private boolean bankrupt;

	/** The store. */
	private final StoreManager storeManager ;

	/** The period when the firm was created. */
	private final int birthPeriod;

	/** The capital manager. */
	private CapitalManager capitalManager;

	/** The name of the firm. */
	private final String name;

	/** The owner of the firm. */
	private CapitalOwner owner ;

	/** The type of production */
	final private ProductionType productionType = ProductionType.integratedProduction;

	/** The purchasing manager. */
	final private PurchasingManager purchasingManager;

	/** The verbosity of the firm; */
	private boolean verbose=false;

	/** The bank account of the firm. */
	protected final Account account ;

	/** The data. */
	protected FirmDataset data;

	/** The factory. */
	protected final Factory factory ;

	/** The mediator between the different components of the firm. */
	protected final Mediator mediator;

	/** The pricing manager. */
	protected final PricingManager pricingManager;

	/** The production manager. */
	protected final ProductionManager productionManager;

	/** The public relation manager. */
	protected final PublicRelationManager publicRelationManager;

	/** The workforce manager. */
	protected final WorkforceManager workforceManager ;

	/**
	 * Creates a new firm with the given parameters.
	 * @param aName  the name. 
	 * @param owner  the owner.
	 */
	public BasicFirm( 
			String aName, 
			CapitalOwner owner) {
		this.init();
		this.name = aName ;
		this.mediator = new BasicMediator();
		this.birthPeriod = getCurrentPeriod().getValue();
		this.account = Circuit.getNewAccount(this);
		this.owner = owner ;
		this.factory = getNewFactory() ;
		this.purchasingManager = getNewPurchasingManager();
		this.workforceManager = getNewWorkforceManager();
		this.storeManager = getNewStoreManager();
		this.productionManager = getNewProductionManager();
		this.pricingManager = getNewPricingManager();
		this.capitalManager = getNewCapitalManager();
		this.publicRelationManager = getNewPublicRelationManager();
		this.mediator.add(this.factory);
		this.mediator.add(this.account);
		this.mediator.add(this.pricingManager);
		this.mediator.add(this.storeManager);
		this.mediator.add(this.productionManager);
		this.mediator.add(this.workforceManager);
		this.mediator.add(this.purchasingManager);
		this.mediator.add(this.capitalManager);
		this.mediator.add(this);
	}

	/**
	 * Returns the owner.
	 * @return the owner.
	 */
	private CapitalOwner getOwner() {
		if (this.owner==null) {
			this.owner=(CapitalOwner) Circuit.getResource(Circuit.SELECT_A_CAPITAL_OWNER);
		}
		return this.owner;
	}

	/**
	 * Finances the production.
	 */
	protected void financeProduction() {
		final Long productionBudget;
		if (this.purchasingManager!=null) {
			this.purchasingManager.computeBudget();
			productionBudget = (Long)this.mediator.get(Labels.WAGEBILL_BUDGET)+(Long)this.mediator.get(Labels.RAW_MATERIALS_BUDGET);
		}
		else {
			productionBudget = (Long)this.mediator.get(Labels.WAGEBILL_BUDGET);
		}
		final Long financingNeed = productionBudget-account.getAmount() ;
		if ( financingNeed>0 ) {
			account.lend( financingNeed ) ;
		}
		if ( account.getAmount() < productionBudget ) 
			throw new RuntimeException("Production is not financed.") ;
	}

	/**
	 * Returns a new capital manager.
	 * @return the new manager.
	 */
	protected CapitalManager getNewCapitalManager() {
		return new BasicCapitalManager(this.mediator);
	}

	/**
	 * Returns a new factory.
	 * @return a new factory.
	 */
	protected Factory getNewFactory() {
		return Factories.getNewFactory(this.getProduction(),this.mediator);
	}

	/**
	 * Returns a new basic pricing manager.
	 * @return a new basic pricing manager.
	 */
	protected PricingManager getNewPricingManager() {
		return new BasicPricingManager(this.mediator);
	}

	/**
	 * Returns a new basic production manager.
	 * @return a new basic production manager.
	 */
	protected ProductionManager getNewProductionManager() {
		return new BasicProductionManager(this.mediator);
	}

	/**
	 * Returns a new public relation manager.
	 * @return the new manager.
	 */
	protected PublicRelationManager getNewPublicRelationManager() {
		return new BasicPublicRelationManager(this.mediator);
	}

	/**
	 * Returns a new purchasing manager.
	 * @return a new purchasing manager.
	 */
	protected PurchasingManager getNewPurchasingManager() {
		final PurchasingManager aPurchasingManager;
		if (FinalFactory.class.isInstance(this.factory)){
			aPurchasingManager = new PurchasingManager(this.mediator);
		}
		else
			aPurchasingManager = null;
		return aPurchasingManager;
	}

	/**
	 * Returns a new store manager.
	 * @return a new store manager.
	 */
	protected BasicStoreManager getNewStoreManager() {
		return new BasicStoreManager(this.mediator);
	}

	/**
	 * Returns a new workforce manager.
	 * @return the new manager.
	 */
	protected WorkforceManager getNewWorkforceManager() {
		return new BasicWorkforceManager(this.mediator);
	}

	/**
	 * Does nothing.
	 */
	protected void init() {
	}

	/**
	 * Updates the data.
	 */
	protected void updateData() {
		data.period = getCurrentPeriod().getValue();
		data.date = getCurrentPeriod().toString();
		data.name = this.name;
		data.age = getCurrentPeriod().getValue()-this.birthPeriod;
		data.bankrupt=this.bankrupt;
		data.maxProduction=(Integer)this.mediator.get(Labels.PRODUCTION_MAX);
		data.workforceTarget=(Integer)this.mediator.get(Labels.WORKFORCE_TARGET);
		data.deposit=this.account.getAmount();
		data.debt=this.account.getDebt();
		data.debtTarget=(Long)this.mediator.get(Labels.DEBT_TARGET);
		//System.out.println(data.debt);// DELETE
		data.doubtDebt=0;
		data.dividend=(Long)this.mediator.get(Labels.DIVIDEND);
		if (this.account.getDebtorStatus().equals(Quality.DOUBTFUL)) data.doubtDebt=this.account.getDebt();;
		data.capital=(Long) this.mediator.get(Labels.CAPITAL);
		data.inventoryNormalVolume=(Float)this.mediator.get(Labels.INVENTORIES_NORMAL_VOLUME);
		data.inventoryRatio=(Float)this.mediator.get(Labels.INVENTORY_LEVEL_RATIO);
		data.invUnVal=(Long)this.mediator.get(Labels.INVENTORY_UG_VALUE);
		data.invFiVal=(Long)this.mediator.get(Labels.INVENTORY_FG_VALUE);
		data.invFiVol=(Integer)this.mediator.get(Labels.INVENTORY_FG_VOLUME);
		data.jobOffers=(Integer)this.mediator.get(Labels.JOBS_OFFERED);
		data.machinery=(Integer)this.mediator.get(Labels.MACHINERY);
		data.prodVol=(Integer)this.mediator.get(Labels.PRODUCTION_VOLUME);
		data.prodVal=(Long)this.mediator.get(Labels.PRODUCTION_VALUE);
		data.salesPVal=(Long) this.mediator.get(Labels.SALES_VALUE);
		data.salesCVal=(Long) this.mediator.get(Labels.COST_OF_GOODS_SOLD);
		data.grossProfit=data.salesPVal-data.salesCVal;
		data.salesVol=(Integer) this.mediator.get(Labels.SALES_VOLUME);
		data.salesVariation=(Integer) this.mediator.get(Labels.SALES_VARIATION);
		data.offeredVol=(Integer) this.mediator.get(Labels.OFFERED_VOLUME);
		data.vacancies=(Integer) this.mediator.get(Labels.VACANCIES);
		data.wageBill=(Long)this.mediator.get(Labels.WAGEBILL);
		data.workforce=(Integer)this.mediator.get(Labels.WORKFORCE);
		data.wage=(Double)this.mediator.get(Labels.WAGE);
		data.price=(Double)this.mediator.get(Labels.PRICE);
		data.factory=this.factory.getClass().getName();
		data.production=this.getProduction();
		data.utilizationTarget=(Float)this.mediator.get(Labels.PRODUCTION_LEVEL);
		if (FinalFactory.class.isInstance(this.factory)) {
			data.intermediateNeedsVolume=(Integer)this.mediator.get(Labels.RAW_MATERIALS_NEEDS);
			data.intermediateNeedsBudget=(Long)this.mediator.get(Labels.RAW_MATERIALS_BUDGET);
			data.rawMaterialEffectiveVolume=(Integer)this.mediator.get(Labels.RAW_MATERIALS_VOLUME);
		}
		data.optimism=(Boolean)this.mediator.get(Labels.OPTIMISM);
	}

	/**
	 * Calls the purchasing manager to buy the raw materials.
	 */
	@Override
	public void buyRawMaterials() {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		if (this.purchasingManager!=null) {
			this.purchasingManager.buyRawMaterials();
			((FinalFactory) this.factory).takeRawMaterials();// TODO change: the factory must take the raw materiel itself
		}
	}

	/**
	 * Closes the firm.<br>
	 * Completes some technical operations at the end of the period.
	 */
	@Override
	public void close() {
		this.mediator.get(Labels.CLOSURE);
		//this.pricingManager.close();
		this.factory.close();
		this.updateData();
	}

	@Override
	public Object get(String key) {
		Object result = null;
		if (key.equals(Labels.ACCOUNT)) {
			result = this.account;
		}
		else if (key.equals(Labels.FIRM)) {
			result = this;
		}
		else if (key.equals(Labels.OWNER)) {
			result = this.getOwner();
		}
		return result;
	}

	/**
	 * Returns the data.
	 * @return the data.
	 */
	@Override
	public FirmDataset getData() {
		return data;
	}

	/**
	 * Returns the offer of the firm on the goods market.
	 * @return the offer.
	 */
	@Override
	public GoodsOffer getGoodsOffer() {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		GoodsOffer offer = (GoodsOffer) this.mediator.get(Labels.OFFER_OF_GOODS);
		return offer;
	}

	/**
	 * Returns the offer of the firm on the labor market.
	 * @return the offer.
	 */
	@Override
	public JobOffer getJobOffer() {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		JobOffer offer = (JobOffer) this.mediator.get(Labels.OFFER_OF_JOB);
		return offer;
	}

	/**
	 * Returns the name of the firm.
	 * @return the name.
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the type of production of the firm.
	 * @return a type of production.
	 */
	@Override
	public ProductionType getProduction() {
		return this.productionType;
	}

	@Override
	public <extend> Object getPublicInfo(String key) {
		return this.publicRelationManager.getPublicInfo(key);
	}

	/**
	 * Goes bankrupt.
	 */
	@Override
	public void goBankrupt() {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		this.bankrupt = true;
		this.workforceManager.layoffAll();
		this.factory.kill() ;
	}

	/**
	 * Returns a flag that indicates if the firm is bankrupt or not.
	 * @return <code>true</code> if the firm is bankrupt.
	 */
	@Override
	public boolean isBankrupt() {
		return bankrupt;
	}

	/**
	 * Receives a job application.
	 * @param worker - the job seeker.
	 * @param offer - the related job offer.
	 */
	@Override
	public void jobApplication( Worker worker, JobOffer offer ) {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		this.workforceManager.jobApplication( worker, offer );
	}

	/**
	 * Kills the firm.
	 */
	@Override
	public void kill() {
		this.factory.kill();
		this.workforceManager.kill();
	}

	/** 
	 * Opens the firm for a new period.<br>
	 * Initializes data.
	 */
	@Override
	public void open() {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		this.data = new FirmDataset();
		this.mediator.get(Labels.OPENING); // Opens each component.
		if (this.verbose) {
			this.mediator.get(Labels.VERBOSE);
		}
	}

	/**
	 * Pays the dividend.
	 */
	@Override
	public void payDividend() {
		if (bankrupt) {
			throw new RuntimeException("Bankrupted.");
		}
		this.mediator.get(Labels.DO_PAY_DIVIDEND);
	}

	/**
	 * Prepares the production.
	 */
	public void prepareProduction() {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		this.productionManager.updateProductionLevel();
		this.pricingManager.updatePrice();
		this.workforceManager.updateWorkforce();
		this.workforceManager.newJobOffer();
		this.financeProduction();
	}

	/**
	 * Produces.
	 */
	public void production() {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		this.workforceManager.payWorkers() ;
		factory.production() ;
		this.storeManager.offerCommodities();
	}

	/**
	 * Sells some commodities to an other agent.
	 * @param offer  the offer to which the buyer responds.
	 * @param volume  the volume of goods the buyer wants to buy.
	 * @param check  the payment.
	 * @return the goods sold.
	 */
	public Goods sell( GoodsOffer offer, int volume, Check check ) {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		Goods sale = this.storeManager.sell( offer, volume, check );
		return sale;
	}

	/**
	 * Sets the verbosity of the firm.
	 * @param b  a boolean.
	 */
	@Override
	public void setVerbose(boolean b) {
		this.verbose=true;
	}

}



























