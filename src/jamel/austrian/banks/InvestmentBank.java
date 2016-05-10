/*
 * =========================================================
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * aint with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.austrian.banks;

import jamel.austrian.roles.Shareholder;
import jamel.austrian.roles.Seller;
import jamel.austrian.sfc.SFCSector;
import jamel.austrian.widgets.AbstractCheque;
import jamel.austrian.widgets.InvestmentProject;
import jamel.austrian.widgets.Offer;
import jamel.austrian.widgets.StartupDetails;
import jamel.basic.Circuit;
import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicSectorDataset;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.Phase;
import jamel.basic.data.SectorDataset;

import java.util.HashMap;
import java.util.LinkedList;

import org.w3c.dom.Element;

/**
 * Represents an investment bank. There is only one such bank in the economy.
 * <p>
 * The investment bank operates free of charge. There is no ownership and no
 * profits.
 * <p>
 */
public class InvestmentBank extends SFCSector implements Seller {

	/** The funds acquired from investors in one time period. */
	private int acquiredFunds;

	/**
	 * The bank which manages the payments of the investment bank.
	 * The investment bank's account is something like an escrow account.
	 */
	private CommercialBank bank;

	/** The marker for newly created banks. */
	private int bankID;

	/** The funds invested in new banks in one time period. */
	private int bankUnderwriting;

	/** The funds to be acquired in a time period. */
	private int capitalRequirement;

	/** The list of completed investment projects. */
	private final HashMap<Integer, InvestmentProject> completedProjects;

	/** The asked price for one share. */
	private float currentPrice;

	/** The data. */
	private BasicSectorDataset dataset;

	/**
	 * The standard size of firms in case of large market volumes.
	 * Is adjusted depending on whether the economy uses fixed capital or not.
	 */
	private int extendedFirmSize;

	/** The marker for newly created firms. */
	private int firmID;

	/** The funds invested in new firms in one time period. */
	private int firmUnderwriting;

	/** The marker for projects in general. */
	private int ID;

	/** The list of investment projects. */
	private final LinkedList<InvestmentProject> investmentProjects;

	/** The share price that will be offered in the next time period. */
	private float nextPrice;

	/** The bank's offer in the equity market. */
	private Offer offer;

	/** The number of offered shares. */
	private int offeredShares;

	/** The number of shares sold per time period. */
	private int sales;

	/** The funds invested in new firms and banks in one time period. */
	private int underwriting;

	/**
	 * Creates a new investment bank.
	 */
	public InvestmentBank(String name, Circuit aCircuit) {
		super(name, aCircuit);
		investmentProjects = new LinkedList<InvestmentProject>();
		completedProjects = new HashMap<Integer, InvestmentProject>();
		nextPrice = parameters.timeDepositSize;
	}

	/**
	 * Updates data at the end of a period.
	 */
	private void updateData() {

		dataset.putSectorialValue("moneyHoldings", (double) bank.getBalance(this));
		dataset.putSectorialValue("equityInvestment", (double) acquiredFunds);
		dataset.putSectorialValue("offeredShares", (double) offeredShares);
		dataset.putSectorialValue("sharesSold", (double) sales);
		dataset.putSectorialValue("pricePerShare", (double) currentPrice);
		dataset.putSectorialValue("underwriting", (double) underwriting);
		dataset.putSectorialValue("firmUnderwriting", (double) firmUnderwriting);
		dataset.putSectorialValue("bankUnderwriting", (double) bankUnderwriting);

	}

	/**
	 * Updates the price.
	 */
	private void updatePrice() {

		if (offeredShares == 0)
			return;
		int remainingShares = offeredShares - sales;

		float alpha = getRandom().nextFloat();
		if (remainingShares < parameters.sigma1 * offeredShares)
			nextPrice += parameters.pFlex * alpha;
		else if (remainingShares > parameters.sigma2 * offeredShares)
			nextPrice -= parameters.pFlex * alpha;

		int smallestProjectSize = 0;
		for (InvestmentProject project : investmentProjects) {
			int size = project.getCapitalRequirement();
			if (size > smallestProjectSize)
				smallestProjectSize = size;
		}

		if (nextPrice > smallestProjectSize)
			nextPrice = smallestProjectSize;
		if (nextPrice > 25000)
			nextPrice = 25000f;
		if (nextPrice < 5000)
			nextPrice = 5000f;

		nextPrice = parameters.timeDepositSize;

	}

	@Override
	public Object askFor(String key) {
		throw new RuntimeException("Not used");
	}

	@Override
	public void doEvent(Element event) {
		final String eventType = event.getAttribute("event");
		if (eventType.equals("initialize")) {
			this.bank = getBankingSector().selectRandomBank();
			this.bank.getNewAccount(this, 0);
			if (getFirmsSector().getParam("typeMin") == 0)
				extendedFirmSize = parameters.extendedFirmSize;
			else
				extendedFirmSize = parameters.standardFirmSize;
		} else {
			throw new RuntimeException(
					"Unknown event or not yet implemented: " + event.getNodeName() + ": " + eventType);
		}
	}

	@Override
	public AgentDataset getData() {
		throw new RuntimeException("Never called.");
	}

	@Override
	public SectorDataset getDataset() {
		return dataset;
	}

	/**
	 * Transfers the investment bank's checking account to another commercial
	 * bank.
	 */
	public void getNewBank(CommercialBank newBank) {
		this.bank = newBank;
	}

	/**
	 * Returns the new firm creations to be executed.
	 * 
	 * @return a linked list containing the investment projects of the new
	 *         banks.
	 */
	public LinkedList<InvestmentProject> getNewBankProjects() {
		LinkedList<InvestmentProject> newBankProjects = new LinkedList<InvestmentProject>();
		for (InvestmentProject project : completedProjects.values()) {
			if (project.getOverallType().equals("Bank"))
				newBankProjects.add(project);
		}
		return newBankProjects;
	}

	/**
	 * Returns the new firm creations to be executed.
	 * 
	 * @return a linked list containing the investment projects of the new
	 *         firms.
	 */
	public LinkedList<InvestmentProject> getNewFirmProjects() {
		LinkedList<InvestmentProject> newFirmProjects = new LinkedList<InvestmentProject>();
		for (InvestmentProject project : completedProjects.values()) {
			if (project.getOverallType().equals("Firm"))
				newFirmProjects.add(project);
		}
		return newFirmProjects;
	}

	@Override
	public Phase getPhase(String name) {
		Phase result = null;

		if (name.equals("opening")) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					acquiredFunds = 0;
					underwriting = 0;
					firmUnderwriting = 0;
					bankUnderwriting = 0;
					sales = 0;
					currentPrice = nextPrice;
					dataset = new BasicSectorDataset();
				}
			};
		}

		else if (name.equals("offering")) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					offeredShares = (int) (capitalRequirement / currentPrice);
					if (offeredShares == 0)
						return;
					offer = new Offer(InvestmentBank.this, offeredShares, currentPrice);
					getMarket("equity").newOffer(offer);
				}
			};
		}

		else if (name.equals("closure")) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					updatePrice();
					updateData();
				}
			};
		}

		return result;
	}

	/**
	 * Launches a project.
	 */
	public InvestmentProject invest(int ID) {
		return completedProjects.get(ID);
	}

	@Override
	public void makeOffer() {
		// Implemented above
	}

	/**
	 * Adds a new investment project.
	 */
	public void newProject(String project) {

		ID++;

		if (project.equals("Firm")) {
			firmID++;
			StartupDetails firmDetails = getFirmsSector().getStartupDetails();
			String type = firmDetails.getType();
			int stage = firmDetails.getStage();
			int firmSize;
			boolean physicalCapital = firmDetails.getProductionForm();
			int prevailingWage = (int) (getMarketSector().getPrevailingWage());
			int capitalTarget;
			if (!physicalCapital) {
				firmSize = parameters.standardFirmSize;
				if (type.equals("A"))
					capitalTarget = (int) (prevailingWage * parameters.muK * (1 + getParam("equityBuffer")));
				else
					capitalTarget = (int) (prevailingWage * firmSize * (1 + getParam("equityBuffer")));
			} else {
				float inputPrice = getMarket(type + stage).getPriceLevel();
				int nextLowerStage = stage - 1;
				int marketVolume = getMarket(type + nextLowerStage).getMarketVolume();
				if (marketVolume > parameters.Q)
					firmSize = extendedFirmSize;
				else
					firmSize = parameters.standardFirmSize;
				if (Float.isNaN(inputPrice)) {
					capitalTarget = (int) (prevailingWage * firmSize * (1 + getParam("equityBuffer")));
				} else
					capitalTarget = (int) ((prevailingWage + inputPrice * parameters.capL) * firmSize
							* (1 + getParam("equityBuffer")));
			}

			investmentProjects.add(
					new InvestmentProject("Firm", ID, firmID, type, stage, firmSize, capitalTarget, physicalCapital));
			capitalRequirement += capitalTarget;
		}

		if (project.equals("Bank")) {
			bankID++;
			investmentProjects.add(
					new InvestmentProject("Bank", ID, bankID, null, 0, 0, getParam("initialMoney").intValue(), false));
			capitalRequirement += getParam("initialMoney").intValue();
		}
	}

	/**
	 * Sells a share to an investor.
	 */
	public void sell(AbstractCheque cheque) {

		bank.deposit(this, cheque);
		InvestmentProject project = investmentProjects.getFirst();

		int newFunds = cheque.getAmount();
		int requiredFunds = project.getCapitalRequirement() - project.getFunding();

		if (newFunds < requiredFunds) {
			project.newInvestor((Shareholder) cheque.getIssuer(), cheque.getAmount());
		} else if (newFunds == requiredFunds) {
			project.newInvestor((Shareholder) cheque.getIssuer(), requiredFunds);
			investmentProjects.remove(project);
			completedProjects.put(project.getID(), project);
		} else {
			while (newFunds > requiredFunds) {
				project.newInvestor((Shareholder) cheque.getIssuer(), requiredFunds);
				investmentProjects.remove(project);
				completedProjects.put(project.getID(), project);
				project = investmentProjects.getFirst();
				newFunds -= requiredFunds;
				requiredFunds = project.getCapitalRequirement() - project.getFunding();
			}
			project.newInvestor((Shareholder) cheque.getIssuer(), newFunds);
		}

		getMarket("equity").updateOffer(this.offer, 1);
		getMarket("equity").registerSale(this.offer, 1, false);
		sales += 1;
		acquiredFunds += cheque.getAmount();
		capitalRequirement -= cheque.getAmount();

	}

	public void setBankID(int bankID) {
		this.bankID = bankID;
	}

	public void setFirmID(int firmID) {
		this.firmID = firmID;
	}

	/**
	 * Transfers a cheque to the newly created firm/bank.
	 */
	public AbstractCheque underwrite(int ID) {
		AbstractCheque cheque = bank.newCheque(this, completedProjects.get(ID).getFunding());
		underwriting += cheque.getAmount();
		completedProjects.remove(ID);
		return cheque;
	}
}