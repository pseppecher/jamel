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
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.austrian.households;

import jamel.austrian.roles.Shareholder;
import jamel.basic.Circuit;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.AgentSet;
import jamel.basic.sector.BasicAgentSet;
import jamel.basic.sector.Phase;
import jamel.basic.data.SectorDataset;
import jamel.austrian.households.Household;
import jamel.austrian.households.HouseholdSector;
import jamel.austrian.sfc.SFCSector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Represents the sector of the households.
 */
public class HouseholdSector extends SFCSector {

	/**
	 * The list of the households with unsaturated demand (as updated during a
	 * time period).
	 */
	protected final AgentSet<Household> concurrentHouseholds;

	/** The collection of agents. */
	protected final AgentSet<Household> households;

	/**
	 * Creates a new sector for households.
	 * 
	 * @param name
	 *            the name of the sector.
	 * @param circuit
	 *            the circuit.
	 */
	public HouseholdSector(String name, Circuit aCircuit) {
		super(name, aCircuit);
		this.households = new BasicAgentSet<Household>(this.random);
		this.concurrentHouseholds = new BasicAgentSet<Household>(this.random);
	}

	/**
	 * Creates households.
	 * 
	 * @param type
	 *            the type of households to create.
	 * @param lim
	 *            the number of households to create.
	 * @return a list containing the new households.
	 */
	protected List<Household> createHouseholds(String type, int lim) {
		final List<Household> list = new ArrayList<Household>(lim);
		for (int index = 0; index < lim; index++) {
			this.countAgents++;
			final String name = "Household " + this.countAgents;
			try {
				list.add((Household) Class.forName(type, false, ClassLoader.getSystemClassLoader())
						.getConstructor(String.class, Circuit.class, SFCSector.class)
						.newInstance(name, circuit, HouseholdSector.this));
			} catch (Exception e) {
				throw new RuntimeException("Something goes wrong while creating households.", e);
			}
		}
		return list;
	}

	@Override
	public Object askFor(String key) {
		throw new RuntimeException("Not used");
	}

	/**
	 * Each household allocates his budget.<br>
	 */
	public void budgetAllocation() {
		do {
			Household household = concurrentHouseholds.getRandomAgent();
			if (!household.budgetAllocation())
				concurrentHouseholds.remove(household);
		} while (concurrentHouseholds.getList().size() > 0);
	}

	@Override
	public void doEvent(Element event) {
		final String eventType = event.getAttribute("event");
		if (eventType.equals("Create new households")) {
			final int size = Integer.parseInt(event.getAttribute("size"));
			this.households.putAll(this.createHouseholds(this.agentType, size));
		} else if (eventType.equals("preferenceChange")) {
			for (final Household household : households.getList())
				household.changePreference(event);
		} else {
			throw new RuntimeException("Unknown event or not yet implemented: "+event.getNodeName()+": "+eventType);
		}
	}

	@Override
	public SectorDataset getDataset() {
		SectorDataset data = households.collectData();
		data.putSectorialValue("households", (double) households.getList().size());
		return data;
	}

	/**
	 * @return the number of households with unsaturated demand;
	 */
	public int getDemanders() {
		return concurrentHouseholds.getList().size();
	}

	@Override
	public Phase getPhase(String name) {
		Phase result = null;

		if (name.equals("opening")) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					for (final Household household : households.getList()) {
						household.open();
					}
					concurrentHouseholds.putAll(households.getList());
				}
			};
		}

		else if (name.equals("setReservationWage")) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					for (final Household household : households.getShuffledList()) {
						household.jobSearch();
					}
				}
			};
		}

		else if (name.equals("job_search")) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					for (final Household household : households.getList()) {
						household.setReservationWage();
					}
				}
			};
		}

		else if (name.equals("registerExpenditure")) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					for (final Household household : households.getList()) {
						household.registerExpenditure();
					}
				}
			};
		}

		else if (name.equals("closure")) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					if (concurrentHouseholds.getList().size() != 0)
						throw new RuntimeException("Bad handling of demand.");
					for (Household selectedHousehold : households.getList())
						selectedHousehold.close();
				}
			};
		}

		return result;
	}

	/**
	 * Adds a household to the list of households with unsaturated demand (after
	 * a wage payment has been received).
	 */
	public void registerNewDemand(BasicHousehold household) {
		if (!concurrentHouseholds.contains(household))
			concurrentHouseholds.put(household);
	}

	/**
	 * Returns an ownership structure of a firm or bank.
	 * Shareholders are associated with numbers that reflect percentages of
	 * ownership.
	 * All shareholders get equal shares.
	 * 
	 * @param the
	 *            number of shareholders.
	 * @return a HashMap of type <code>Shareholder</code> / <code>Number</code>.
	 */
	public HashMap<Shareholder, Number> selectRandomShareholders(int number) {
		List<Shareholder> selection = new ArrayList<Shareholder>(number);
		while (selection.size() < number) {
			Shareholder randomHousehold = households.getRandomAgent();
			if (!selection.contains(randomHousehold))
				selection.add(randomHousehold);
		}
		HashMap<Shareholder, Number> ownershipStructure = new HashMap<Shareholder, Number>();
		for (Shareholder owner : selection) {
			ownershipStructure.put(owner, 1f / number);
		}
		if (ownershipStructure.size() != number)
			throw new RuntimeException("False definition of ownership.");
		return ownershipStructure;
	}

}