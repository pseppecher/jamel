/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher and contributors.
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
 */

package jamel.agents.firms.managers;

import jamel.Circuit;
import jamel.agents.firms.Labels;
import jamel.agents.firms.util.FirmComponent;
import jamel.agents.firms.util.Mediator;
import jamel.agents.roles.Provider;
import jamel.spheres.monetarySphere.Account;
import jamel.spheres.realSphere.IntermediateGoods;
import jamel.util.markets.GoodsOffer;
import jamel.util.markets.ProviderComparator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * The purchasing manager.<p>
 * This manager is in charge of the purchase of raw materials (intermediate goods) for a firm producing final goods.
 * 2013-11-10: refactoring.
 */
public class PurchasingManager implements FirmComponent {

	/** The number of providers the manager can memorize. TODO: Should be a parameter. */
	private static final int memory = 5;

	/** The provider comparator. */
	private static final ProviderComparator providerComparator = new ProviderComparator();

	/** The initial price of raw materials.  TODO: Should be a parameter. */
	private static final double rawMaterialsInitialPrice = 10;

	/**
	 * Returns a collection of providers selected at random.
	 * @param num - the number of providers to select.
	 * @return a collection of providers.
	 */
	private static Collection<Provider> getMoreProviders(int num) {
		final LinkedList<Provider> providers = new LinkedList<Provider>();
		for (int count=0; count<num; count++) {
			final Provider provider = (Provider) Circuit.getResource(Circuit.SELECT_A_PROVIDER_OF_RAW_MATERIAL);
			if (provider==null) {
				return providers;
			}
			if ((provider.getGoodsOffer()!=null)&&(!providers.contains(provider)))
				providers.add(provider);
		}
		Collections.sort(providers, providerComparator);
		return providers;
	}
	
	/** The current account of the firm. */
	private Account account;

	/** The initial budget of the purchasing manager for the current period. */
	private Long maxBudget;

	/** The max volume to purchase. */
	private Integer maxVolume;

	/** The mediator. */
	final private Mediator mediator;

	/** The list of usual providers. */
	final private LinkedList<Provider> providersList ;

	/** The unit price of the raw materials. */
	private double rawMaterialsPrice = rawMaterialsInitialPrice;

	/** The goods purchased by the manager. */
	private IntermediateGoods totalPurchase;

	/**
	 * Creates a new purchasing manager.
	 * @param mediator  the mediator.
	 */
	public PurchasingManager(Mediator mediator) {
		this.providersList=new LinkedList<Provider>();
		this.mediator=mediator;
	}

	/**
	 * Returns a list of providers, sorted by price.
	 * @return a list of providers.
	 */
	private LinkedList<Provider> getProviders() {
		final LinkedList<Provider> newProvidersList = new LinkedList<Provider>();
		for (Provider provider: this.providersList) {
			if ((!provider.isBankrupt())&&(provider.getGoodsOffer()!=null))
				newProvidersList.add(provider);
		}
		Collections.sort(newProvidersList, providerComparator);		
		while(newProvidersList.size()>memory)
			newProvidersList.removeLast();
		final Collection<Provider> moreProviders = PurchasingManager.getMoreProviders(memory);
		for (Provider provider: moreProviders) {
			if (!newProvidersList.contains(provider))
				newProvidersList.add(provider);
		}
		Collections.sort(newProvidersList, providerComparator);
		this.providersList.clear();
		this.providersList.addAll(newProvidersList);
		return newProvidersList;
	}

	/**
	 * Purchases raw materials.
	 * @param providers - a list of providers.
	 * @param maxVolume - the maximum volume of raw materials to purchase.
	 * @param maxBudget - the maximum amount of money to spend.
	 * @return a heap of raw materials.
	 */
	private IntermediateGoods purchase(Collection<Provider> providers, int maxVolume, long maxBudget) {
		final IntermediateGoods totalPurchase = new IntermediateGoods();
		for (Provider provider: providers) {
			final GoodsOffer offer = provider.getGoodsOffer();
			final int needVolume=maxVolume-totalPurchase.getVolume();
			final long needValue=maxBudget-totalPurchase.getValue();
			if (offer.getVolume()==0) 
				throw new RuntimeException("Volume is zero.");
			if (needValue>offer.getPrice()){
				int dealVolume = (int) (needValue/offer.getPrice());
				if (dealVolume==0)
					throw new RuntimeException("The volume equals 0.");
				dealVolume = Math.min(dealVolume, offer.getVolume()); // The volume of the deal can't be higher than the volume of the offer. 
				dealVolume = Math.min(dealVolume, needVolume); // The volume of the deal can't be higher than the volume of the offer. 
				long dealValue = (long) (dealVolume*offer.getPrice());
				IntermediateGoods purchase = (IntermediateGoods) provider.sell(offer,dealVolume,this.account.newCheck(dealValue, provider)) ;
				if (purchase.getVolume()!=dealVolume) 
					throw new RuntimeException("Bad volume.");
				if (purchase.getValue()!=dealValue) 
					throw new RuntimeException("Bad value.");
				if (purchase.getValue()>needValue) 
					throw new RuntimeException("Budget overrun.");
				if (purchase.getVolume()>needVolume) 
					throw new RuntimeException("Too many widgets.");
				totalPurchase.add(purchase);
				if ((totalPurchase.getVolume()==maxVolume)||(totalPurchase.getValue()==maxBudget)) {
					return totalPurchase;
				}
			}
			else {
				this.rawMaterialsPrice = offer.getPrice() ;
			}
		}
		return totalPurchase;
	}

	/**
	 * Buys the raw materials the firm needs for the production.
	 */
	public void buyRawMaterials() {

		if (this.totalPurchase.getVolume()>0) {
			throw new RuntimeException("The volume must be 0."); 
		}
		this.totalPurchase = new IntermediateGoods();

		if (maxBudget!=0) {

			int needVolume=maxVolume;
			long needValue=maxBudget;
			final Collection<Provider> providers = getProviders();
			final IntermediateGoods purchase=purchase(providers,needVolume,needValue); 
			if (purchase.getValue()>needValue)
				throw new RuntimeException("Budget overrun.");
			if (purchase.getVolume()>needVolume)
				throw new RuntimeException("Too many merchandise.");
			totalPurchase.add(purchase);

			//***

			if ((totalPurchase.getVolume()<maxVolume*0.95)&&(totalPurchase.getValue()<totalPurchase.getValue()*0.95)) { 
				// We observe a shortage, then we search for other providers.
				needVolume=maxVolume-totalPurchase.getVolume();
				needValue=maxBudget-totalPurchase.getValue();
				final Collection<Provider> moreProviders = getMoreProviders(2*memory);
				final IntermediateGoods morePurchase=purchase(moreProviders,needVolume,needValue); 
				if (morePurchase.getValue()>needValue)
					throw new RuntimeException("Budget overrun.");
				if (morePurchase.getVolume()>needVolume)
					throw new RuntimeException("Too many merchandise.");
				totalPurchase.add(morePurchase);
				for (Provider provider: moreProviders) {
					// The new providers are added to the providers list.
					if (!this.providersList.contains(provider))
						this.providersList.add(provider);
				}

			}

			//***

			if (totalPurchase.getVolume()>0)
				this.rawMaterialsPrice = totalPurchase.getUnitCost(); // Updates the price.
			
		}
				
	}

	/**
	 * 
	 */
	public void computeBudget() {
		this.maxVolume = (Integer)this.mediator.get(Labels.RAW_MATERIALS_NEEDS);
		this.maxBudget = (long) (this.maxVolume*this.rawMaterialsPrice*1.0);
	}

	@Override
	public Object get(String key) {
		Object result = null;
		if (key.equals(Labels.RAW_MATERIALS_BUDGET)) {
			result = this.maxBudget;
		}
		else if (key.equals(Labels.RAW_MATERIALS)) {
			result = this.totalPurchase;
		}
		else if (key.equals(Labels.OPENING)) {
			this.open();
		}
		return result;
	}

	/**
	 * Opens the managers.
	 */
	public void open() {
		if (this.account==null) {
			this.account=(Account) this.mediator.get(Labels.ACCOUNT);
		}
		this.maxBudget=null;
		this.maxVolume=null;
	}
	
}



















