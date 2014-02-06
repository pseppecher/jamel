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
import jamel.JamelObject;
import jamel.agents.firms.Labels;
import jamel.agents.firms.util.Mediator;

/**
 * A basic production manager.
 */
public class BasicProductionManager extends JamelObject implements ProductionManager{

	@SuppressWarnings("javadoc")
	protected static final String PARAM_UTIL_RATE_INIT_MAX = "Firms.utilizationRate.init.max";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_UTIL_RATE_INIT_MIN = "Firms.utilizationRate.init.min";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_UTIL_RATE_FLEX = "Firms.utilizationRate.flexibility";

	/** The mediator. */
	protected Mediator mediator;

	/** utilizationRateRectified */
	protected float utilizationRateRectified;

	/** The current (targeted) capacity utilization rate. */
	protected float utilizationRateTargeted;

	/**
	 * Creates a new manager.
	 * @param mediator  the mediator.
	 */
	public BasicProductionManager(Mediator mediator) {
		final int uRateMin = Integer.parseInt(Circuit.getParameter(PARAM_UTIL_RATE_INIT_MIN));
		final int uRateMax = Integer.parseInt(Circuit.getParameter(PARAM_UTIL_RATE_INIT_MAX));
		this.utilizationRateTargeted =uRateMin+getRandom().nextInt(uRateMax-uRateMin);
		this.mediator=mediator;
	}

	@Override
	public Object get(String key) {
		Object result=null;
		if (key.equals(Labels.PRODUCTION_LEVEL)) {
			result=this.utilizationRateRectified;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.ProductionManager#updateProductionLevel()
	 */
	@Override
	public void updateProductionLevel() {
		final float utilizationRateFlexibility = Float.parseFloat(Circuit.getParameter(PARAM_UTIL_RATE_FLEX));
		final float alpha1 = getRandom().nextFloat();
		final float alpha2 = getRandom().nextFloat();
		final float inventoryRatio = (Float)this.mediator.get(Labels.INVENTORY_LEVEL_RATIO);
		if (inventoryRatio<1-alpha1*alpha2) {// Low level
			final float delta = (alpha1*utilizationRateFlexibility);
			this.utilizationRateTargeted += delta;
			if (this.utilizationRateTargeted>100) {
				this.utilizationRateTargeted = 100;
			}
		}
		else if (inventoryRatio>1+alpha1*alpha2) {// High level
			final float delta = (alpha1*utilizationRateFlexibility);
			this.utilizationRateTargeted -= delta;
			if (this.utilizationRateTargeted<0) {
				this.utilizationRateTargeted = 0;
			}
		}
		final float maxUtilization = (Float) this.mediator.get(Labels.PRODUCTION_LEVEL_MAX);
		this.utilizationRateRectified = Math.min(this.utilizationRateTargeted, maxUtilization);
	}

}























