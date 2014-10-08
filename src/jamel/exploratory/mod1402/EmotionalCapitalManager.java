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

import java.util.Collection;

import jamel.Circuit;
import jamel.agents.firms.Firm;
import jamel.agents.firms.Labels;
import jamel.agents.firms.managers.BasicCapitalManager;
import jamel.agents.firms.util.Mediator;

/**
 * An emotional capital manager.
 * Optimism is based on the sales.
 * (new version of the old CapitalManager131222S).
 */
public class EmotionalCapitalManager extends BasicCapitalManager {

	@SuppressWarnings("javadoc")
	protected static final String PARAM_HERDING = "Firms.confidence.herdingPropensity";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_HIGH_CAPITAL_RATIO_TARGET = "Firms.capital.highRatio";

	/** New optimism. */
	protected boolean newOptimist = true;

	/** Optimism. */
	protected boolean optimist = true;

	/**
	 * Creates a new capital manager.
	 * @param mediator  the mediator.
	 */
	public EmotionalCapitalManager(Mediator mediator) {
		super(mediator);
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.BasicCapitalManager#close()
	 */
	@Override
	protected void close() {
		super.close();
		this.optimist=this.newOptimist;
	}

	/**
	 * Returns the fundamental optimism, based on the level of sales.
	 * @return a boolean.
	 */
	protected boolean getFundamentalOptimism() {
		final float salesRatio = (Float) this.mediator.get(Labels.SALES_RATIO);
		final float salesRatioNormalLevel = (Float) this.mediator.get(Labels.SALES_RATIO_NORMAL);
		//System.out.println(salesRatio+"/"+salesRatioNormalLevel);
		return (salesRatio>salesRatioNormalLevel);
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.BasicCapitalManager#open()
	 */
	@Override
	protected void open() {
		super.open();
		this.updateConfidence();
		this.updateBehavior();
	}

	/**
	 * Changes the behavior of the manager according to its optimism.
	 */
	protected void updateBehavior() {
		if (!this.newOptimist) {
			this.capitalRatioTarget=Float.parseFloat(Circuit.getParameter(PARAM_HIGH_CAPITAL_RATIO_TARGET));
		}
	}

	/**
	 * Updates the confidence.
	 */
	protected void updateConfidence() {
			final float herding = Float.parseFloat(Circuit.getParameter(PARAM_HERDING));
			if (getRandom().nextFloat()>herding) {
				this.newOptimist=this.getFundamentalOptimism();
			}
			else {
				int optimism = 0;
				int pessimism = 0;
				@SuppressWarnings("unchecked")
				final Collection<? extends Object> friends = (Collection<? extends Object>) Circuit.getResource(Circuit.SELECT_A_LIST_OF_FIRMS+","+3); 
				for(Object f:friends) {
					if ((Boolean) ((Firm)f).getPublicInfo(Labels.OPTIMISM)) {
						optimism++;
					}
					else {
						pessimism++;
					}
				}
				this.newOptimist= (optimism>pessimism);			
			}
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.BasicStoreManager#get(java.lang.String)
	 */
	@Override
	public Object get(String key) {
		Object result=null;
		if (key.equals(Labels.OPTIMISM)) {
			result=this.optimist ;
		}
		else {
			result=super.get(key);
		}
		return result;
	}

}














