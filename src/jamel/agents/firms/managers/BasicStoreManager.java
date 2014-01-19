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

import java.util.LinkedList;
import java.util.ListIterator;

import jamel.Circuit;
import jamel.JamelObject;
import jamel.agents.firms.Labels;
import jamel.agents.firms.util.FirmComponent;
import jamel.agents.firms.util.Mediator;
import jamel.agents.roles.Provider;
import jamel.spheres.monetarySphere.Account;
import jamel.spheres.monetarySphere.Check;
import jamel.spheres.realSphere.Goods;
import jamel.util.markets.GoodsOffer;

/**
 * A basic store manager for the firms.
 */
public class BasicStoreManager extends JamelObject implements FirmComponent{

	/** The limit of the memory of the manager. */
	final static private int recordLim = 12;

	/** The bank account. */
	protected Account account;

	/** The value of the sales. */
	protected Long costOfGoodsSold=null ;

	/** The inventory. */
	protected Goods inventory;

	/** The mediator */
	protected Mediator mediator;

	/** The offer */
	protected GoodsOffer offer ;

	/** The volume of commodities offered at the beginning of the market phase. */
	protected int offerVolume;

	/** The provider. */
	protected Provider provider;

	/** The record of the latest sales in volume. */
	final protected LinkedList<Integer> recordSalesVol=new LinkedList<Integer>();

	/** The value of the sales. */
	protected Long salesValue=null ;

	/** The volume of the sales. */
	protected Integer salesVolume=null ;

	/** The variation of sales (halfyear2 - halfyear1, volume) */
	private Integer salesVariation;

	/**
	 * Creates a new manager.
	 * @param mediator  the mediator. 
	 */
	public BasicStoreManager(Mediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * Closes the manager.
	 */
	protected void close() {
		this.recordSalesVol.add(this.salesVolume);
		if (this.recordSalesVol.size()>recordLim) {
			this.recordSalesVol.removeFirst();
		}
	}

	/**
	 * Returns the average of the latest sales (in volume).
	 * @return the average of the latest sales.
	 */
	protected float getAverageSales() {
		float average = 0;
		int sum=0;
		int count=0;
		for(int vol:this.recordSalesVol) {
			sum+=vol;
			count++;
		}
		if (count>0) {
			average=sum/count;
		}
		return average;
	}

	/**
	 * Returns the sales to maximum capacity of production ratio (in volume) 
	 * @return the ratio.
	 */
	protected float getSalesRatio() {
		final int productionMax = (Integer)this.mediator.get(Labels.PRODUCTION_MAX);
		return this.getAverageSales()/productionMax;
	}

	/**
	 * Returns the variation of the sales (= 6 latest months - 6 previous months, in volume). 
	 * @return the volume of the variation.
	 */
	protected Integer getSalesVariation() {
		if (this.salesVariation==null) {
			ListIterator<Integer> iterator = recordSalesVol.listIterator(recordSalesVol.size());
			int count=0;
			int halfYear1 = 0;
			int halfYear2 = 0;		
			while (iterator.hasPrevious()) {
				if (count<6) {
					halfYear2 += iterator.previous();
				} else {
					if (count>=12) {
						throw new RuntimeException("Too many data in this data set.");
					}
					halfYear1 += iterator.previous();
				}
				count++;
			}
			this.salesVariation = halfYear2-halfYear1;
		}
		return this.salesVariation;
	}

	/**
	 * Opens the store.
	 */
	protected void open() {
		inventory=null;
		offer = null;
		salesVolume = null;
		salesValue = null;
		salesVariation = null;
		costOfGoodsSold = null;
		if (this.account==null) {
			this.account = (Account) this.mediator.get(Labels.ACCOUNT);
		}
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.util.FirmComponent#get(java.lang.String)
	 */
	@Override
	public Object get(String key) {
		Object result=null;
		if (key.equals(Labels.OPENING)) {
			this.open();
		}
		else if (key.equals(Labels.CLOSURE)) {
			this.close();
		}
		else if (key.equals(Labels.SALES_VOLUME)) {
			result=this.salesVolume;
		}
		else if (key.equals(Labels.SALES_VOLUME)) {
			result=this.salesVolume;
		}
		else if (key.equals(Labels.SALES_VARIATION)) {
			result=this.getSalesVariation();
		}
		else if (key.equals(Labels.OFFERED_VOLUME)) {
			result=this.offerVolume;
		}
		else if (key.equals(Labels.COST_OF_GOODS_SOLD)) {
			result=this.costOfGoodsSold;
		}
		else if (key.equals(Labels.SALES_VALUE)) {
			result=this.salesValue;
		}
		else if (key.equals(Labels.OFFER_OF_GOODS)) {
			result=this.offer;			
		}
		else if (key.equals(Labels.SALES_RATIO)) {
			result=this.getSalesRatio();			
		}
		else if (key.equals(Labels.SALES_VARIATION)) {
			result=this.getSalesVariation();			
		}
		return result;
	}

	/**
	 * Creates a new offer and posts it on the goods market.
	 */
	public void offerCommodities() {
		if (this.provider==null) {
			this.provider=(Provider)this.mediator.get(Labels.FIRM);
		}
		double aPrice = (Double)this.mediator.get(Labels.PRICE);
		if (this.inventory!=null) 
			throw new RuntimeException("The inventory is not null.");
		this.costOfGoodsSold=0l;
		this.salesValue=0l;
		this.salesVolume=0;
		this.offerVolume=0;
		final Goods merchandise = (Goods) this.mediator.get(Labels.INVENTORIES_OF_FINISHED_GOODS);
		if ((aPrice>0)&&(merchandise!=null)) {
			this.inventory = merchandise ;
			final int vol1 = (int)(this.inventory.getVolume()*Float.parseFloat(Circuit.getParameter("Firms."+Labels.INVENTORIES_PROPENSITY_TO_SELL)));
			final int vol2 = (Integer)this.mediator.get(Labels.PRODUCTION_MAX)*2;
			this.offerVolume = Math.min(vol1,vol2);
			if (this.offerVolume>0) {
				this.offer = new GoodsOffer(provider,this.offerVolume,aPrice) ;
			}
		}
	}

	/**
	 * Sells the specified volume of goods.
	 * @param offer  the offer.
	 * @param volume  the volume.
	 * @param check  the check.
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
		sale.setValue(check.getAmount());
		this.salesValue += sale.getValue();
		this.salesVolume += sale.getVolume();
		this.offer.subtract(volume) ;// corrige l'offre ( il y a moins de stocks disponibles )
		if (offer.getVolume()==0)
			this.offer = null;
		return sale;
	}

}
