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
import jamel.agents.roles.CapitalOwner;
import jamel.spheres.monetarySphere.Account;

/**
 * A basic capital manager.<p>
 * The main task of the capital manager is to calculate the dividend to pay to the owner of the firm.
 * 2013-11-10: creation.
 */
public class BasicCapitalManager extends JamelObject implements CapitalManager {

	@SuppressWarnings("javadoc")
	protected static final String PARAM_CAPITAL_PROPENSITY_TO_DISTRIBUTE = "Firms.capital.propensityToDistributeExcess";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_CAPITAL_RATIO = "Firms.capital.normalRatio";

	/** The account. */
	protected Account account;

	/** The capital ratio targeted. */
	protected Float capitalRatioTarget=null;

	/** The debt targeted. */
	protected Long debtTarget;

	/** The dividend. */
	protected Long dividend;

	/** The mediator. */
	protected Mediator mediator;

	/** The firm owner */
	protected CapitalOwner owner;

	/** propensityToDistributeExcessCapital */
	protected Float propensityToDistributeExcessCapital=null;

	/**
	 * Creates a new capital manager.
	 * @param mediator  the mediator.
	 */
	public BasicCapitalManager(Mediator mediator) {
		this.mediator=mediator;
	}

	/**
	 * Executes some technical operations at the end of the period.<p>
	 * Does nothing.
	 */
	protected void close() {
	}

	/**
	 * Returns the capital of the firm.
	 * @return the capital.
	 */
	protected long getCapital() {
		final long inventoriesTotalValue = (Long) this.mediator.get(Labels.INVENTORIES_TOTAL_VALUE);
		return this.account.getAmount()+inventoriesTotalValue-this.account.getDebt();
	}

	/**
	 * Executes some technical operations at the beginning of the period.
	 */
	protected void open() {
		this.dividend=null;
		this.debtTarget=null;
		if (this.account==null) {
			this.account=(Account) this.mediator.get(Labels.ACCOUNT);
		}
		if (this.owner==null) {
			this.owner = (CapitalOwner) this.mediator.get(Labels.OWNER);
		}
		this.capitalRatioTarget=Float.parseFloat(Circuit.getParameter(PARAM_CAPITAL_RATIO));
		this.propensityToDistributeExcessCapital = Float.parseFloat(Circuit.getParameter(PARAM_CAPITAL_PROPENSITY_TO_DISTRIBUTE));		
	}

	/**
	 * Pays the dividend for the period.
	 */
	protected void payDividend() {
		final long debt = this.account.getDebt();
		final long money = this.account.getAmount();
		final long inventoriesTotalValue = (Long) this.mediator.get(Labels.INVENTORIES_TOTAL_VALUE);
		final long assets=money+inventoriesTotalValue;
		final long capital=assets-debt;
		final long capitalTarget = (long) ((assets)*this.capitalRatioTarget );
		this.debtTarget=(assets-capitalTarget);
		if (capital<=0) {
			this.dividend=0l;
		}
		else {
			if (capital<=capitalTarget) {
				this.dividend=0l;
			}
			else {
				this.dividend = (long) ((capital-capitalTarget)*this.propensityToDistributeExcessCapital);
				this.dividend = Math.min(this.dividend,money);
				if (this.dividend<0)
					throw new RuntimeException("Negative dividend.");
			}
		}
		if (this.owner==null) {
			dividend = 0l;
		}
		if (this.dividend>0) {
			this.owner.receiveDividend( this.account.newCheck( this.dividend, this.owner ) ) ;				
		}
		this.owner.infoCapital(this.account.getAmount()+inventoriesTotalValue-debt);
	}

	@Override
	public Object get(String key) {
		Object result=null;
		if (key.equals(Labels.OPENING)) {
			this.open();
		}
		else if (key.equals(Labels.DO_PAY_DIVIDEND)) {
			if (this.dividend!=null) {
				throw new RuntimeException("The dividend is already paid.");
			}
			this.payDividend();
		}
		else if (key.equals(Labels.DEBT_TARGET)) {
			result=this.debtTarget;
		}
		else if (key.equals(Labels.DIVIDEND)) {
			result=this.dividend;
		}
		else if (key.equals(Labels.CAPITAL)) {
			result=this.getCapital();
		}
		else if (key.equals(Labels.CLOSURE)) {
			this.close();
		}
		return result;
	}

}














