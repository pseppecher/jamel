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
import jamel.agents.firms.managers.BasicStoreManager;
import jamel.agents.firms.managers.CapitalManager;
import jamel.agents.firms.managers.PricingManager;
import jamel.agents.firms.managers.ProductionManager;
import jamel.agents.firms.managers.PublicRelationManager;
import jamel.agents.firms.managers.BasicWorkforceManager;
import jamel.agents.firms.managers.WorkforceManager;
import jamel.agents.firms.util.Mediator;
import jamel.agents.roles.CapitalOwner;

/**
 * An experimental firm : more flexible = the managers are chosen when the firm is created.
 * Created : 2014-01-26
 */
public class ModularFirm extends BasicFirm {

	@SuppressWarnings("javadoc")
	protected static final String PARAM_CAPITAL_MANAGER = "Firms.capitalManager";
	
	@SuppressWarnings("javadoc")
	protected static final String PARAM_PRICING_MANAGER = "Firms.pricingManager";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_PRODUCTION_MANAGER = "Firms.productionManager";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_PUBLIC_RELATION_MANAGER = "Firms.publicRelationManager";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_STORE_MANAGER = "Firms.storeManager";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_WORKFORCE_MANAGER = "Firms.workforceManager";

	/**
	 * Creates a new firm with the given parameters.
	 * @param aName  the name. 
	 * @param owner  the owner.
	 */
	public ModularFirm(String aName, CapitalOwner owner) {
		super(aName, owner);
	}

	/**
	 * Returns a new store manager.
	 * @return a new store manager.
	 */
	@Override
	protected CapitalManager getNewCapitalManager() {
		final String managerType = Circuit.getParameter(PARAM_CAPITAL_MANAGER);
		CapitalManager manager = null;
		try {
			manager = (CapitalManager) Class.forName(managerType,false,ClassLoader.getSystemClassLoader()).getConstructor(Mediator.class).newInstance(this.mediator);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Instantiation failed ("+managerType+")");
		}
		return manager;
	}

	/**
	 * Returns a new pricing manager.
	 * @return a new pricing manager.
	 */
	@Override
	protected PricingManager getNewPricingManager() {
		final String managerType = Circuit.getParameter(PARAM_PRICING_MANAGER);
		PricingManager manager = null;
		try {
			manager = (PricingManager) Class.forName(managerType,false,ClassLoader.getSystemClassLoader()).getConstructor(Mediator.class).newInstance(this.mediator);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Instantiation failed ("+managerType+")");
		}
		return manager;
	}

	/**
	 * Returns a new production manager.
	 * @return a new production manager.
	 */
	@Override
	protected ProductionManager getNewProductionManager() {
		final String managerType = Circuit.getParameter(PARAM_PRODUCTION_MANAGER);
		ProductionManager manager = null;
		try {
			manager = (ProductionManager) Class.forName(managerType,false,ClassLoader.getSystemClassLoader()).getConstructor(Mediator.class).newInstance(this.mediator);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Instantiation failed ("+managerType+")");
		}
		return manager;
	}

	/**
	 * Returns a new public relation manager.
	 * @return a new public relation manager.
	 */
	@Override
	protected PublicRelationManager getNewPublicRelationManager() {
		final String managerType = Circuit.getParameter(PARAM_PUBLIC_RELATION_MANAGER);
		PublicRelationManager manager = null;
		try {
			manager = (PublicRelationManager) Class.forName(managerType,false,ClassLoader.getSystemClassLoader()).getConstructor(Mediator.class).newInstance(this.mediator);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Instantiation failed ("+managerType+")");
		}
		return manager;
	}

	/**
	 * Returns a new store manager.
	 * @return a new store manager.
	 */
	@Override
	protected BasicStoreManager getNewStoreManager() {
		final String managerType = Circuit.getParameter(PARAM_STORE_MANAGER);
		BasicStoreManager manager = null;
		try {
			manager = (BasicStoreManager) Class.forName(managerType,false,ClassLoader.getSystemClassLoader()).getConstructor(Mediator.class).newInstance(this.mediator);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Instantiation failed ("+managerType+")");
		}
		return manager;
	}

	/**
	 * Returns a new workforce manager.
	 * @return a new workforce manager.
	 */
	@Override
	protected WorkforceManager getNewWorkforceManager() {
		final String managerType = Circuit.getParameter(PARAM_WORKFORCE_MANAGER);
		BasicWorkforceManager manager = null;
		try {
			manager = (BasicWorkforceManager) Class.forName(managerType,false,ClassLoader.getSystemClassLoader()).getConstructor(Mediator.class).newInstance(this.mediator);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Instantiation failed ("+managerType+")");
		}
		return manager;
	}

}















