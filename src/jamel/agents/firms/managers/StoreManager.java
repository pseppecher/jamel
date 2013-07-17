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

package jamel.agents.firms.managers;

import jamel.agents.firms.Labels;
import jamel.agents.roles.Provider;
import jamel.spheres.monetarySphere.Account;
import jamel.spheres.monetarySphere.Check;
import jamel.spheres.realSphere.Goods;
import jamel.util.Blackboard;
import jamel.util.markets.GoodsOffer;

/**
 * A basic store manager for the firms.
 */
public class StoreManager {

	/** The bank account. */
	final private Account account;

	/** The blackboard. */
	private Blackboard blackboard;

	/** The inventory. */
	private Goods inventory;

	/** The offer */
	private GoodsOffer offer ;

	/** The provider. */
	final private Provider provider;

	/** The value of the sales. */
	private Long costOfGoodsSold=null ;

	/** The value of the sales. */
	private Long salesValue=null ;

	/** The volume of the sales. */
	private Integer salesVolume=null ;

	/**
	 * Creates a new manager.
	 * @param aProvider  the firm. 
	 * @param aAccount  the bank account of the firm.
	 * @param blackboard  the blackboard. 
	 */
	public StoreManager(Provider aProvider, Account aAccount, Blackboard blackboard) {
		this.provider = aProvider;
		this.account = aAccount;
		this.blackboard = blackboard;
	}

	/**
	 * Returns the value of the inventory of the store.
	 * @return the value.
	 */
	public long getValue() {
		Long value;
		if (this.inventory==null) 
			value = 0l;
		else
			value = this.inventory.getValue();
		return value;
	}

	/**
	 * Creates a new offer and posts it on the goods market.
	 */
	public void offerCommodities() {
		double aPrice = (Double)this.blackboard.get(Labels.PRICE);
		if (this.inventory!=null) 
			throw new RuntimeException("The inventory is not null.");
		this.costOfGoodsSold=0l;
		this.salesValue=0l;
		this.salesVolume=0;
		this.blackboard.put(Labels.COST_OF_GOODS_SOLD, 0l);
		this.blackboard.put(Labels.SALES_VALUE, 0l);
		this.blackboard.put(Labels.SALES_VOLUME, 0);
		this.blackboard.put(Labels.GROSS_PROFIT, 0L);
		final Goods merchandise = (Goods) this.blackboard.get(Labels.PRODUCT_FOR_SALES);
		if ((aPrice>0)&&(merchandise!=null)) {
			this.inventory = merchandise ;
			this.offer = new GoodsOffer(provider,inventory.getVolume(),aPrice) ;
			this.blackboard.put(Labels.OFFER_OF_GOODS, this.offer);
		}
	}

	/**
	 * Opens the store.
	 */
	public void open() {
		if ((inventory!=null)&&(inventory.getVolume()>0))
			throw new RuntimeException("The inventory is not empty.");
		inventory=null;
		offer = null;
		salesVolume = null;
		salesValue = null;
		costOfGoodsSold = null;
	}

	/**
	 * Sells the specified volume of goods.
	 * @param offer - the offer.
	 * @param volume - the volume.
	 * @param check - the check.
	 * @return the goods sold.
	 */
	public Goods sell( GoodsOffer offer, int volume, Check check ) {
		if ( offer != this.offer ) 
			throw new RuntimeException("The 2 offers are not the same.");
		if ( check.getAmount() != ( long ) ( volume*this.offer.getPrice() ) ) 
			throw new RuntimeException("Bad cheque amount.");
		if ( volume == 0 ) 
			throw new RuntimeException("Volume is zero.");
		if ( volume < 0 ) 
			throw new RuntimeException("Volume is negative.");
		this.account.deposit( check ) ;
		final Goods sale = this.inventory.newGoods(volume);
		this.costOfGoodsSold += sale.getValue();
		this.blackboard.put(Labels.COST_OF_GOODS_SOLD, this.costOfGoodsSold);
		sale.setValue(check.getAmount());
		this.salesValue += sale.getValue();
		this.blackboard.put(Labels.SALES_VALUE, this.salesValue);
		this.blackboard.put(Labels.GROSS_PROFIT, this.salesValue-this.costOfGoodsSold);
		this.salesVolume += sale.getVolume();
		this.blackboard.put(Labels.SALES_VOLUME, this.salesVolume);
		this.offer.subtract(volume) ;// corrige l'offre ( il y a moins de stocks disponibles )
		if (offer.getVolume()==0)
			this.offer = null;
		return sale;
	}

}
