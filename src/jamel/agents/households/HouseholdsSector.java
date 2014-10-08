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

package jamel.agents.households;

import jamel.Circuit;
import jamel.JamelObject;
import jamel.agents.roles.CapitalOwner;
import jamel.agents.roles.Consumer;
import jamel.agents.roles.Worker;
import jamel.util.data.PeriodDataset;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the sector of the households.
 * <p>
 * Encapsulates a collection of {@link Household} agents.
 */
public class HouseholdsSector extends JamelObject {

	/** The number of households created. */
	private int countHouseholds = 0 ;

	/** The list of the households. */
	private final ArrayList<Household> householdsList ;

	/** The type of the households. */
	private String householdsType;

	/**
	 * Creates a new households sector.
	 */
	public HouseholdsSector() { 
		this.householdsList = new ArrayList<Household>() ;
	}

	/**
	 * Returns a Household selected at random.
	 * @return a Household.
	 */
	protected Household getRandomHousehold() {
		final int size = householdsList.size();
		if (size==0) return null;
		return householdsList.get( getRandom().nextInt( size ) ) ;
	}

	/**
	 * Returns an array of households selected at random.
	 * Duplicate households are permitted.
	 * @param n  the number of households to select.
	 * @return an array of households.
	 */
	protected Household[] getRandomHouseholds(int n) {
		final Household[] households = new Household[n];
		final int size = householdsList.size();
		if (size!=0) {
			for (int count=0; count<n; count++) {
				households[count] = householdsList.get( getRandom().nextInt( size ) ) ;
			}
		}
		return households;
	}

	/**
	 * Closes the sector.<br>
	 * Each household is closed. 
	 * The macro data are updated.
	 * @param macroData  the macro dataset.
	 */
	public void close(PeriodDataset macroData) {
		for (Household aHousehold : householdsList) {
			aHousehold.close() ;
		}
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
	 * Returns the selected resource.
	 * @param key the key of the resource to be returned.
	 * @return the resource.
	 */
	public Object get(String key) {
		final Object result;
		if (
				key.equals(Circuit.SELECT_A_HOUSEHOLD) ||
				key.equals(Circuit.SELECT_A_CAPITAL_OWNER)				
				) {
			result = this.getRandomHousehold();
		} else {
			throw new RuntimeException("Unexpected key: "+key);
		}
		return result;
	}

	/**
	 * Returns the data for each household of the sector.
	 * @param keys  a string that contains the keys of the fields to return, separated by commas.
	 * @return an array of string, each string containing the data of one firm, each field separated by commas.
	 */
	public String[] getHouseholdsData(String keys) {
		final String[] data = new String[this.householdsList.size()];
		int id = 0;
		for(Household household:this.householdsList){
			HouseholdDataset householdData = household.getData();
			if (householdData!=null) {
				data[id]=householdData.getData(keys);
			}
			id++;
		}
		return data;
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
		for(String parameter : parameters) {
			final String[] word=parameter.split("=",2);
			if (word[0].equals("households")) {
				if (newHouseholds != null) throw new RuntimeException("Event new households : Duplicate parameter : households.");
				newHouseholds = Integer.parseInt(word[1]);
			}
		}
		if (newHouseholds==null) 
			throw new RuntimeException("Missing parameter: households.");
		for (int count = 0 ; count<newHouseholds ; count++){
			countHouseholds ++ ;
			try {
				final String name = "Household "+countHouseholds;
				Household newHousehold;
				newHousehold = (Household) Class.forName(this.householdsType,false,ClassLoader.getSystemClassLoader()).getConstructor(String.class).newInstance(name);
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
	/*public CapitalOwner selectRandomCapitalOwner() {
		final int size = householdsList.size();
		if (size==0) return null;//throw new RuntimeException("The list of households is empty.");
		return householdsList.get( getRandom().nextInt( size ) ) ;
	}*/

	/**
	 * Sets the type of the households.
	 * @param householdsType the type to set.
	 */
	public void setHouseholdsType(String householdsType) {
		this.householdsType=householdsType;
	}

}