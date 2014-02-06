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

package jamel;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

import jamel.agents.firms.BasicFirmSector;
import jamel.agents.firms.FirmSector;
import jamel.gui.JamelWindow;
import jamel.gui.charts.ChartFactory;

/**
 * A basic implementation of the macro-economic circuit.
 */
public class BasicCircuit extends Circuit {

	/**
	 * Creates a new circuit.
	 * @param aSimulator  the simulator.
	 */
	public BasicCircuit(Jamel aSimulator) {
		super(aSimulator);
	}

	/**
	 * Returns the money panel.
	 * @return the money panel.
	 */
	protected Component getConfidencePanel() {
		final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
		panel.setName("Confidence");
		panel.add(new ChartPanel(ChartFactory.getHPessimism(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getHHoarding(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getMoneyVelocity(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getFPessimism(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getFSelfFinancing(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getPrecautionarySavings(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getGPessimism(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getHouseholdsLiquidSaving(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getFLeverage(timeSeries)));
		return panel;
	}

	/**
	 * Returns the industry panel.
	 * @return the industry panel.
	 */
	protected Component getIndustryPanel() {
		final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
		panel.setName("Industry");
		panel.add(new ChartPanel(ChartFactory.getBankruptcyRate(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getCapacityUtilization(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getMarkup(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getInventoryLevel(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getRelativePrices(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getVacancyRates(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getFirms(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getProfits(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getWorkers(this.timeSeries)));
		return panel;
	}

	/**
	 * Returns the labor panel.
	 * @return the labor panel.
	 */
	protected Component getLaborPanel() {
		final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
		panel.setName("Labor");
		panel.add(new ChartPanel(ChartFactory.getLabourMarket(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getUnemploymentRate(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getBeveridgeCurve(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getJobVacancies(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getUnemploymentDuration(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getJobSeekers(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getUnemployment(this.timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getVacancyRate(this.timeSeries)));
		panel.add(new ChartPanel(null));
		return panel;
	}

	/**
	 * Returns the main panel.
	 * @return a list of <code>ChartPanels</code>.
	 */
	protected JPanel getMainPanel() {
		final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
		panel.setName("Main");
		panel.add(new ChartPanel(ChartFactory.getPrices(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getFinalGoodsMarket(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getInventoryVolumeFinal(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getWages(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getBeveridgeCurve(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getBankRatios(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getInflationUnemployment(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getPhillipsCurve(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getDistribution(timeSeries)));
		return panel;
	}

	/**
	 * Returns the money panel.
	 * @return the money panel.
	 */
	protected Component getMoneyPanel() {
		final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
		panel.setName("Money");
		panel.add(new ChartPanel(ChartFactory.getBankRatios(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getBankDividend(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getBankruptcies(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getLiabilitiesAndBankCapital(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getMoneyVelocity(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getMoney(timeSeries)));
		panel.add(new ChartPanel(ChartFactory.getDebt(timeSeries)));
		panel.add(new ChartPanel(null));
		panel.add(new ChartPanel(null));
		return panel;
	}

	/**
	 * Returns a new basic sector for the firms.
	 * @return a <code>BasicFirmsSector</code>.
	 */
	@Override
	protected FirmSector getNewFirmsSector() {
		return new BasicFirmSector();
	}

	/* (non-Javadoc)
	 * @see jamel.Circuit#get(java.lang.String)
	 */
	@Override
	protected Object getResource2(String key) {
		final Object result;
		if (
				key.equals(Circuit.SELECT_A_CAPITAL_OWNER) ||
				key.equals(Circuit.SELECT_A_HOUSEHOLD)
				) {
			result = this.households.get(key);
		} else if (
				key.startsWith(Circuit.SELECT_A_LIST_OF_FIRMS) ||
				key.equals(Circuit.SELECT_AN_EMPLOYER) ||
				key.equals(Circuit.SELECT_A_WAGE) ||
				key.equals(Circuit.SELECT_A_PROVIDER_OF_FINAL_GOODS)				
				) {
			result = this.firms.get(key);
		} else if (key.startsWith(Circuit.GET_HTML_MATRIX)) {
			result = this.matrix.toHtml();
		} else {
			throw new RuntimeException("Unexpected key: "+key);
		}
		return result;
	}

	@Override
	protected void initWindow() {
		this.simulator.doEvent(JamelWindow.COMMAND_ADD_PANEL,this.getConfidencePanel());
		this.simulator.doEvent(JamelWindow.COMMAND_ADD_PANEL,this.getMoneyPanel());
		this.simulator.doEvent(JamelWindow.COMMAND_ADD_PANEL,this.getLaborPanel());
		this.simulator.doEvent(JamelWindow.COMMAND_ADD_PANEL,this.getIndustryPanel());
		this.simulator.doEvent(JamelWindow.COMMAND_ADD_PANEL,this.getMainPanel());
	}

	/* (non-Javadoc)
	 * @see jamel.Circuit#doPeriod()
	 */
	@Override
	public void doPeriod() {
		this.nextPeriod();
		this.open();
		this.bank.payDividend();
		this.firms.payDividend();
		this.firms.planProduction();
		this.households.jobSearch();	
		this.firms.production();
		this.households.consume();
		this.bank.debtRecovery();
		this.close();
	}

}