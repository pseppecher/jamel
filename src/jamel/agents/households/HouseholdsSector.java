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

package jamel.agents.households;

import jamel.JamelObject;
import jamel.agents.roles.CapitalOwner;
import jamel.agents.roles.Consumer;
import jamel.agents.roles.Worker;
import jamel.util.data.PeriodDataset;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Represents the sector of the households.
 * <p>
 * Encapsulates a collection of {@link Household} agents.
 */
public class HouseholdsSector extends JamelObject {

	/** The number of households created. */
	private int countHouseholds = 0 ;

	/** The list of the households. */
	private final LinkedList<Household> householdsList ;

	/**
	 * Creates a new households sector.
	 */
	public HouseholdsSector() { 
		this.householdsList = new LinkedList<Household>() ;
	}

	/**
	 * Closes the sector.<br>
	 * Each household is closed. 
	 * The macro data are updated.
	 * @param macroData - the macro dataset.
	 */
	public void close(PeriodDataset macroData) {
		for (Household aHousehold : householdsList) 
			aHousehold.close() ;
		macroData.compileHouseholdsData(householdsList);
	}

	/**
	 * Calls up each consumer to consume. 
	 */
	public void consume() {
		for (Consumer selectedConsumer : householdsList)
			selectedConsumer.consume() ;		
	}

	/**
	 * Calls up each worker to job search. 
	 */
	public void jobSearch() {
		for (Worker selectedWorker : householdsList)
			selectedWorker.jobSearch() ;		
	}

	/**
	 * Creates new households.
	 * @param parameters  an array of strings that must contain :
	 * the number of households to create, 
	 * the type of household to create,
	 * and possibly other parameters.
	 */
	public void newHouseholds(String[] parameters) {
		Integer newHouseholds = null;
		String householdClassName = null;
		for(String parameter : parameters) {
			final String[] word=parameter.split("=",2);
			if (word[0].equals("households")) {
				if (newHouseholds != null) throw new RuntimeException("Event new households : Duplicate parameter : households.");
				newHouseholds = Integer.parseInt(word[1]);
			}
			else if (word[0].equals("type")) {
				if (householdClassName != null) throw new RuntimeException("Event new households : Duplicate parameter : type.");
				householdClassName = word[1];				
			}
		}
		if (newHouseholds==null) 
			throw new RuntimeException("Missing parameter: households.");
		if (householdClassName==null) 
			throw new RuntimeException("Missing parameter: type.");
		for (int count = 0 ; count<newHouseholds ; count++){
			countHouseholds ++ ;
			try {
				final String name = "Household "+countHouseholds;
				Household newHousehold;
				newHousehold = (Household) Class.forName(householdClassName,false,ClassLoader.getSystemClassLoader()).getConstructor(String.class).newInstance(name);
				householdsList.add(newHousehold) ;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Household creation failure"); 
			}
		}
	}

	/**
	 * Opens the firms sector.<br>
	 * Each firm is opened.
	 */
	public void open() {
		Collections.shuffle(householdsList, getRandom());
		for (Household selectedHousehold : householdsList) {
			selectedHousehold.open() ;
		}
	}

	/**
	 * Selects a capital owner at random.
	 * @return the selected capital owner.
	 */
	public CapitalOwner selectRandomCapitalOwner() {
		final int size = householdsList.size();
		if (size==0) return null;//throw new RuntimeException("The list of households is empty.");
		return householdsList.get( getRandom().nextInt( size ) ) ;
	}

}