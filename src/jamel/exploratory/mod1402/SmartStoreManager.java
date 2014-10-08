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

package jamel.exploratory.mod1402;

import jamel.Circuit;
import jamel.agents.firms.Labels;
import jamel.agents.firms.managers.BasicStoreManager;
import jamel.agents.firms.util.Mediator;
import jamel.spheres.realSphere.Goods;
import jamel.util.markets.GoodsOffer;

/**
 * A store manager less stupid than the basic one.<p>
 * (new version of the old StoreManager131201).
 */
public class SmartStoreManager extends BasicStoreManager {

	@SuppressWarnings("javadoc")
	protected static final String PARAM_SALES_RATIO_NORMAL = "Firms.sales.normalRatio";

	/**
	 * Creates a new manager.
	 * @param mediator  the mediator.
	 */
	public SmartStoreManager(Mediator mediator) {
		super(mediator);
	}

	/**
	 * Returns the normal level of the sales ratio.
	 * @return a Float.
	 */
	protected Float getNormalSalesRatio() {
		return Float.parseFloat(Circuit.getParameter(PARAM_SALES_RATIO_NORMAL));
	}
	/*protected float getNormalSalesRatio() { DELETE
		final int productionMax = (Integer)this.mediator.get(Labels.PRODUCTION_MAX);
		return this.getAverageSales(1200)/productionMax;
	}*/
	
	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.StoreManager#get(java.lang.String)
	 */
	@Override
	public Object get(String key) {
		Object result=super.get(key);
		if (key.equals(Labels.SALES_RATIO_NORMAL)) {
			result=getNormalSalesRatio();
		}
		return result;
	}

	/**
	 * Creates a new offer and posts it on the goods market.
	 */
	@Override
	public void offerCommodities() {
		double aPrice = (Double)this.mediator.get(Labels.PRICE);
		if (this.inventory!=null) 
			throw new RuntimeException("The inventory is not null.");
		this.costOfGoodsSold=0l;
		this.salesValue=0l;
		this.salesVolume=0;
		final Goods merchandise = (Goods) this.mediator.get(Labels.INVENTORIES_OF_FINISHED_GOODS);
		if ((aPrice>0)&&(merchandise!=null)) {
			this.inventory = merchandise ;
			final float normalVolume = (Float)this.mediator.get(Labels.INVENTORIES_NORMAL_VOLUME);
			final int productionMax = (Integer)this.mediator.get(Labels.PRODUCTION_MAX);
			final float productionLevel = 0.01f*(Float)this.mediator.get(Labels.PRODUCTION_LEVEL);
			if (this.inventory.getVolume()<normalVolume) {
				this.offerVolume = (int) (productionLevel*productionMax*this.inventory.getVolume()/normalVolume);
			}
			else {
				this.offerVolume = (int) (this.inventory.getVolume()-normalVolume+productionLevel*productionMax);
			}
			if (this.offerVolume<0) throw new RuntimeException("The volume should not be negative.");
			if (this.offerVolume>this.inventory.getVolume()) throw new RuntimeException("Sales cannot exceed inventories.");
			if (this.offerVolume>0){
				this.offer = new GoodsOffer(provider,this.offerVolume,aPrice) ;
			}
		}
	}

}
