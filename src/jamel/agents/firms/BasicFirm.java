/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher.
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

package jamel.agents.firms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jamel.Circuit;
import jamel.JamelObject;
import jamel.agents.firms.managers.PricingManager;
import jamel.agents.firms.managers.ProductionManager;
import jamel.agents.firms.managers.PurchasingManager;
import jamel.agents.firms.managers.StoreManager;
import jamel.agents.firms.managers.WorkforceManager;
import jamel.agents.roles.CapitalOwner;
import jamel.agents.roles.Worker;
import jamel.spheres.monetarySphere.Account;
import jamel.spheres.monetarySphere.Check;
import jamel.spheres.monetarySphere.Quality;
import jamel.spheres.realSphere.Factories;
import jamel.spheres.realSphere.Factory;
import jamel.spheres.realSphere.FinalFactory;
import jamel.spheres.realSphere.Goods;
import jamel.util.Blackboard;
import jamel.util.markets.GoodsOffer;
import jamel.util.markets.JobOffer;

/**
 * Represents a firm.
 */
public class BasicFirm extends JamelObject implements Firm {

	/** The bank account of the firm. */
	protected final Account account ;

	/** A flag that indicates whether the firm is bankrupt or not. */
	private boolean bankrupt;

	/** The period when the firm was created. */
	private final int birthPeriod;

	/** The name of the firm. */
	private final String name;

	/** The owner of the firm. */
	private CapitalOwner owner ;

	/** The purchasing manager. */
	final private PurchasingManager purchasingManager;

	/** The reserve target. */
	private float reserveTarget = 0.2f;

	/** The store. */
	private final StoreManager storeManager ;

	/** A map that contains the information shared with the managers. */
	protected final Blackboard blackboard = new Blackboard();

	/** The data. */
	protected FirmDataset data;

	/** The factory. */
	protected final Factory factory ;

	/** The pricing manager. */
	protected final PricingManager pricingManager;

	/** The production manager. */
	protected final ProductionManager productionManager;

	/** The workforce manager. */
	protected final WorkforceManager workforceManager ;

	/**
	 * Creates a new firm with the given parameters.
	 * @param aName - the name. 
	 * @param owner - the owner.
	 * @param someParameters - a map that contains parameters.
	 */
	public BasicFirm( 
			String aName, 
			CapitalOwner owner,
			Map<String,String> someParameters) {
		this.defaultParameters();
		this.parseParameters(someParameters);
		this.init();
		this.name = aName ;
		this.birthPeriod = getCurrentPeriod().getValue();
		this.account = Circuit.getNewAccount(this);
		this.owner = owner ;
		this.factory = getNewFactory() ;
		this.purchasingManager = getNewPurchasingManager();
		this.workforceManager = getNewWorkforceManager();
		this.storeManager = new StoreManager(this, account,this.blackboard);
		this.productionManager = getNewProductionManager();
		this.pricingManager = getNewPricingManager();
	}

	/**
	 * Calculates the dividend.
	 * @return a long that represents the dividend.
	 */
	private long calculateDividend() {
		long retainedEarnings = this.getNetWorth();
		if (retainedEarnings<=0) return 0;
		long retainedEarningsTarget = (long) (
				(this.account.getDebt()+retainedEarnings)
				*BasicFirm.this.reserveTarget );
		if (retainedEarnings<=retainedEarningsTarget) return 0;
		return (retainedEarnings-retainedEarningsTarget)/6;
	}

	/**
	 * Sets the default parameters.
	 * TODO Cleanup
	 */
	private void defaultParameters() {
		this.blackboard.put(Labels.PARAM_FACTORY_MACHINES, 10,null);
		//this.blackboard.put(Labels.PRICE_FLEXIBILITY, 0.1f,null); DELETE
		this.blackboard.put(Labels.PARAM_FACTORY_PROD_MAX, 100,null);
		this.blackboard.put(Labels.PARAM_FACTORY_PROD_MIN, 100,null);
		this.blackboard.put(Labels.PARAM_FACTORY_PRODUCTION_TIME, 4,null);
		this.blackboard.put(Labels.PRODUCTION, ProductionType.integratedProduction,null);
		//this.blackboard.put(Labels.WAGE_DOWN_FLEX, 0.02f,null); DELETE
		//this.blackboard.put(Labels.WAGE_UP_FLEX, 0.03f,null); DELETE
	}

	/**
	 * Finances the production.
	 */
	protected void financeProduction() {
		final Long productionBudget;
		if (this.purchasingManager!=null) {
			this.purchasingManager.computeBudget();
			productionBudget = (Long)this.blackboard.get(Labels.WAGEBILL_BUDGET)+(Long)this.blackboard.get(Labels.RAW_MATERIALS_BUDGET);
		}
		else {
			productionBudget = (Long)this.blackboard.get(Labels.WAGEBILL_BUDGET);
		}
		final Long financingNeed = productionBudget-account.getAmount() ;
		if ( financingNeed>0 ) {
			account.lend( financingNeed ) ;
		}
		if ( account.getAmount() < productionBudget ) 
			throw new RuntimeException("Production is not financed.") ;
	}

	/**
	 * Returns the net worth.<br>
	 * The net worth of the firm = total assets minus total liabilities = retained earnings.
	 * @return a long.
	 */
	protected long getNetWorth() {
		return 
				this.factory.getWorth()+
				this.storeManager.getValue()+
				this.account.getAmount()-
				this.account.getDebt();
	}

	/**
	 * Returns a new factory.
	 * @return a new factory.
	 */
	protected Factory getNewFactory() {
		return Factories.getNewFactory(this.blackboard);
	}

	/**
	 * Returns a new basic pricing manager.
	 * @return a new basic pricing manager.
	 */
	protected PricingManager getNewPricingManager() {
		return new PricingManager(this.blackboard);
	}

	/**
	 * Returns a new basic production manager.
	 * @return a new basic production manager.
	 */
	protected ProductionManager getNewProductionManager() {
		return new ProductionManager(this.blackboard);
	}

	/**
	 * Returns a new purchasing manager.
	 * @return a new purchasing manager.
	 */
	protected PurchasingManager getNewPurchasingManager() {
		final PurchasingManager aPurchasingManager;
		if (FinalFactory.class.isInstance(this.factory))
			aPurchasingManager = new PurchasingManager(this.account,this.blackboard);
		else
			aPurchasingManager = null;
		return aPurchasingManager;
	}

	/**
	 * Returns a new workforce manager.
	 * @return the new manager.
	 */
	protected WorkforceManager getNewWorkforceManager() {
		return new WorkforceManager(this, this.account,this.blackboard);
	}

	/**
	 * Does nothing.
	 */
	protected void init() {
	}

	/**
	 * Parses the given parameters.
	 * Then records the parsed parameters in the black board.
	 * @param params  the parameters to parse.
	 * TODO CLEANUP
	 */
	protected void parseParameters(Map<String, String> params) {
		final Set<String> integers = new HashSet<String>();
		final Set<String> floats = new HashSet<String>();
		integers.add(Labels.PARAM_FACTORY_MACHINES);
		//floats.add(Labels.PRICE_FLEXIBILITY); DELETE
		integers.add(Labels.PARAM_FACTORY_PROD_MAX);
		integers.add(Labels.PARAM_FACTORY_PROD_MIN);
		integers.add(Labels.PARAM_FACTORY_PRODUCTION_TIME);
		floats.add(Labels.TECH_COEFF);
		//floats.add(Labels.WAGE_DOWN_FLEX); DELETE
		//floats.add(Labels.WAGE_UP_FLEX); DELETE
		for(Entry<String, String> entry : params.entrySet()) {
			final String key = entry.getKey();
			final String value = entry.getValue();
			if (integers.contains(key)) {
				this.blackboard.put(key, Integer.parseInt(value),null);
			}
			else if (floats.contains(key)) {
				this.blackboard.put(key, Float.parseFloat(value),null);				
			}
			else if (key.equals(Labels.PRODUCTION)) {
				this.blackboard.put(key, ProductionType.valueOf(value),null);				
			}
			else if (key.equals("type")) {// FIXME c'est moche, réfléchir à ça.
			}
			else  {
				throw new RuntimeException("Unknown parameter: "+key+"="+value);
			}
		}
	}

	/**
	 * Updates the data.
	 */
	protected void updateData() {
		data.name = this.name;
		data.age = getCurrentPeriod().getValue()-this.birthPeriod;
		data.grossProfit=(Long)this.blackboard.get(Labels.GROSS_PROFIT);
		data.bankrupt=this.bankrupt;
		data.maxProduction=(Integer)this.blackboard.get(Labels.PRODUCTION_MAX);
		data.anticipatedWorkforce=(Integer)this.blackboard.get(Labels.WORKFORCE_TARGET);
		data.deposit=this.account.getAmount();
		data.debt=this.account.getDebt();
		data.doubtDebt=0;
		if (this.account.getDebtorStatus().equals(Quality.DOUBTFUL)) data.doubtDebt=this.account.getDebt();;
		data.capital=this.getNetWorth();
		data.invUnfVal=(Long)this.blackboard.get(Labels.INVENTORY_UG_VALUE);
		data.invVal=(Long)this.blackboard.get(Labels.INVENTORY_FG_VALUE);
		data.invVol=(Integer)this.blackboard.get(Labels.INVENTORY_FG_VOLUME);
		data.jobOffers=(Integer)this.blackboard.get(Labels.JOBS_OFFERED);
		data.machinery=(Integer)this.blackboard.get(Labels.MACHINERY);
		data.prodVol=(Integer)this.blackboard.get(Labels.PRODUCTION_VOLUME);
		data.prodVal=(Long)this.blackboard.get(Labels.PRODUCTION_VALUE);
		data.reserveTarget=this.reserveTarget;
		data.salesPVal=(Long) this.blackboard.get(Labels.SALES_VALUE);
		data.salesCVal=(Long) this.blackboard.get(Labels.COST_OF_GOODS_SOLD);
		data.salesVol=(Integer) this.blackboard.get(Labels.SALES_VOLUME);
		data.vacancies=(Integer) this.blackboard.get(Labels.VACANCIES);
		data.wageBill=(Long)this.blackboard.get(Labels.WAGEBILL);
		data.workforce=(Integer)this.blackboard.get(Labels.WORKFORCE);
		data.price=(Double)this.blackboard.get(Labels.PRICE);
		data.factory=this.factory.getClass().getName();
		data.production=this.getProduction();
		if (FinalFactory.class.isInstance(this.factory)) {
			data.intermediateNeedsVolume=(Integer)this.blackboard.get(Labels.RAW_MATERIALS_NEEDS);
			data.intermediateNeedsBudget=(Long)this.blackboard.get(Labels.RAW_MATERIALS_BUDGET);
			data.rawMaterialEffectiveVolume=(Integer)this.blackboard.get(Labels.RAW_MATERIALS_VOLUME);
		}
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
			((FinalFactory) this.factory).takeRawMaterials();
		}
	}

	/**
	 * Closes the firm.<br>
	 * Completes some technical operations at the end of the period.
	 */
	@Override
	public void close() {
		this.factory.close();
		this.updateData();
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
		GoodsOffer offer = (GoodsOffer) this.blackboard.get(Labels.OFFER_OF_GOODS);
		if ((offer!=null)&&(offer.getVolume()==0)) {
			this.blackboard.remove(Labels.OFFER_OF_GOODS);
			offer=null;
		}
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
		JobOffer offer = (JobOffer) this.blackboard.get(Labels.OFFER_OF_JOB);
		if ((offer!=null)&&(offer.getVolume()==0)){
			offer=null;
			this.blackboard.remove(Labels.OFFER_OF_JOB);
		}
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
	 * Returns a map that contains the parameters of the firm.
	 * @return a map that contains the parameters of the firm.
	 * TODO cleanup
	 */
	/*public Map<String, String> getParameters() {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("type",this.getClass().getName());
		params.put(Labels.PARAM_FACTORY_MACHINES,this.blackboard.get(Labels.PARAM_FACTORY_MACHINES).toString());
		params.put(Labels.PRICE_FLEXIBILITY,this.blackboard.get(Labels.PRICE_FLEXIBILITY).toString());
		params.put(Labels.PARAM_FACTORY_PROD_MAX,this.blackboard.get(Labels.PARAM_FACTORY_PROD_MAX).toString());
		params.put(Labels.PARAM_FACTORY_PROD_MIN,this.blackboard.get(Labels.PARAM_FACTORY_PROD_MIN).toString());
		params.put(Labels.PARAM_FACTORY_PRODUCTION_TIME,this.blackboard.get(Labels.PARAM_FACTORY_PRODUCTION_TIME).toString());
		params.put(Labels.PRODUCTION,this.blackboard.get(Labels.PRODUCTION).toString());
		params.put(Labels.WAGE_DOWN_FLEX,this.blackboard.get(Labels.WAGE_DOWN_FLEX).toString());
		params.put(Labels.WAGE_UP_FLEX,this.blackboard.get(Labels.WAGE_UP_FLEX).toString());
		if (this.blackboard.get(Labels.TECH_COEFF)!=null)
			params.put(Labels.TECH_COEFF,this.blackboard.get(Labels.TECH_COEFF).toString());
		return params;
	}*/

	@Override
	public String getParametersString() {// TODO utiliser ici getParameters(); CLEANUP !
		String string = 
				"type="+this.getClass().getName()+
				","+Labels.PARAM_FACTORY_MACHINES+"="+this.blackboard.get(Labels.PARAM_FACTORY_MACHINES).toString()+
				//","+Labels.PRICE_FLEXIBILITY+"="+this.blackboard.get(Labels.PRICE_FLEXIBILITY).toString()+DELETE
				","+Labels.PARAM_FACTORY_PROD_MAX+"="+this.blackboard.get(Labels.PARAM_FACTORY_PROD_MAX).toString()+
				","+Labels.PARAM_FACTORY_PROD_MIN+"="+this.blackboard.get(Labels.PARAM_FACTORY_PROD_MIN).toString()+
				","+Labels.PARAM_FACTORY_PRODUCTION_TIME+"="+this.blackboard.get(Labels.PARAM_FACTORY_PRODUCTION_TIME).toString()+
				","+Labels.PRODUCTION+"="+this.blackboard.get(Labels.PRODUCTION).toString();
				//","+Labels.WAGE_DOWN_FLEX+"="+this.blackboard.get(Labels.WAGE_DOWN_FLEX).toString()+DELETE
				//","+Labels.WAGE_UP_FLEX+"="+this.blackboard.get(Labels.WAGE_UP_FLEX).toString();DELETE
		if (this.blackboard.get(Labels.TECH_COEFF)!=null)
			string = string+","+Labels.TECH_COEFF+"="+this.blackboard.get(Labels.TECH_COEFF).toString();
		return string;
	}

	/**
	 * Returns the type of production of the firm.
	 * @return a type of production.
	 */
	@Override
	public ProductionType getProduction() {
		return (ProductionType) this.blackboard.get(Labels.PRODUCTION);// FIXME à revoir, c'est trop souvent appelé pour être stocké seulement dans le blackboard.
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
	 * Initializes data and executes events.
	 * @param eList - a list of strings that describes the events for the current period. 
	 */
	@Override
	public void open(LinkedList<String> eList) {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		this.blackboard.cleanUp();
		this.data = new FirmDataset();
		for (String string: eList){
			String[] word = string.split("\\)",2);
			String[] event = word[0].split("\\(",2);
			if (event[0].equals("set")) {
				final String[] parameters = event[1].split(",");
				final Map<String, String> params = new HashMap<String, String>();
				for (String p:parameters) {
					String[] words = p.split("=", 2);
					params.put(words[0], words[1]);
				}
			}
			else 
				throw new RuntimeException("Unknown event \""+event[0]+"\".");
		}
		if (this.purchasingManager!=null) this.purchasingManager.open();
		this.workforceManager.open();
		this.factory.open();
	}

	/**
	 * Pays the dividend.
	 */
	@Override
	public void payDividend() {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		long dividend = calculateDividend();
		if (dividend<0) 
			throw new IllegalArgumentException("Negative dividend.");
		dividend = Math.min(dividend,this.account.getAmount());
		if ( dividend!=0 ) {
			if ( this.account.getDebtorStatus()!=Quality.GOOD ) 
				throw new RuntimeException( ) ;
			if (owner==null) 
				owner=Circuit.getRandomCapitalOwner();
			if (owner==null) {
				dividend = 0;
			}
			else {
				this.owner.receiveDividend( this.account.newCheck( dividend, owner ) ) ;				
			}
		}
		this.data.dividend=dividend;
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
		this.financeProduction();
		this.workforceManager.newJobOffer();
	}

	/**
	 * Produces.
	 */
	public void production() {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		this.workforceManager.payWorkers() ;
		storeManager.open() ;
		factory.production() ;
		this.storeManager.offerCommodities();
	}

	/**
	 * Sells some commodities to an other agent.
	 * @param offer - the offer to which the buyer responds.
	 * @param volume - the volume of goods the buyer wants to buy.
	 * @param check - the payment.
	 * @return the goods sold.
	 */
	public Goods sell( GoodsOffer offer, int volume, Check check ) {
		if (bankrupt)
			throw new RuntimeException("Bankrupted.");
		Goods sale = this.storeManager.sell( offer, volume, check );
		return sale;
	}
	
	/**
	 * Extracts the value associated with the given key in the blackboard and records it in the map.
	 * @param params  the map.
	 * @param key  the key.
	 */
	private void putParam(Map<String, Object> params, String key) {
		if (!this.blackboard.containsKey(key))
			throw new RuntimeException("Key not found: "+key);
		params.put(key,this.blackboard.get(key));		
	}

	/**
	 * Returns a map that contains the parameters of the firm.
	 * @return a map that contains the parameters of the firm.
	 * TODO CLEANUP
	 */
	public Map<String, Object> getParameters() {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("type",this.getClass().getName());
		putParam(params,Labels.PARAM_FACTORY_MACHINES);
		//putParam(params,Labels.PRICE_FLEXIBILITY); DELETE
		putParam(params,Labels.PARAM_FACTORY_PROD_MAX);
		putParam(params,Labels.PARAM_FACTORY_PROD_MIN);
		putParam(params,Labels.PARAM_FACTORY_PRODUCTION_TIME);
		putParam(params,Labels.PRODUCTION);
		//putParam(params,Labels.WAGE_DOWN_FLEX); DELETE
		//putParam(params,Labels.WAGE_UP_FLEX); DELETE
		if (((ProductionType)params.get(Labels.PRODUCTION)).equals(ProductionType.finalProduction))
			putParam(params,Labels.TECH_COEFF);
		return params;
	}

}



























