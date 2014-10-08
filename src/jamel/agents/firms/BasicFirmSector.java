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
import jamel.JamelObject;
import jamel.agents.roles.CapitalOwner;
import jamel.agents.roles.Employer;
import jamel.agents.roles.Provider;
import jamel.util.data.PeriodDataset;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents the firms sector.
 * <p>
 * Encapsulates a collection of {@link Firm} agents.
 */
public class BasicFirmSector extends JamelObject implements FirmSector {

	@SuppressWarnings("javadoc")
	protected static final String PARAM_REGENERATION_SPAN_MIN = "Firms.regenerationTime.min";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_REGENERATION_SPAN_MAX = "Firms.regenerationTime.max";

	/**
	 * Receive notification of a bankruptcy.<br>
	 * A new firm will be created some months later.
	 * @param aBankruptFirm the bankrupt firm.
	 */
	private static void failure(Firm aBankruptFirm) {
		if (!aBankruptFirm.isBankrupt())
			throw new RuntimeException("Not bankrupt.");
		final String currentDate = getCurrentPeriod().toString();
		final int minTimeBeforeReCreation = Integer.parseInt(Circuit.getParameter(PARAM_REGENERATION_SPAN_MIN));
		final int maxTimeBeforeReCreation = Integer.parseInt(Circuit.getParameter(PARAM_REGENERATION_SPAN_MAX));
		if (minTimeBeforeReCreation>maxTimeBeforeReCreation) {
			throw new RuntimeException("Min and max regeneration time are inconsistent.");
		}
		Circuit.println(currentDate+" Firm Failure ("+aBankruptFirm.getName()+")");
		final String someMonthsLater;
		if (minTimeBeforeReCreation==maxTimeBeforeReCreation) {
			someMonthsLater = getCurrentPeriod().getFuturePeriod(minTimeBeforeReCreation).toString();
		}
		else {
			someMonthsLater = getCurrentPeriod().getFuturePeriod(minTimeBeforeReCreation+getRandom().nextInt(maxTimeBeforeReCreation-minTimeBeforeReCreation)).toString();
		}
		Circuit.newEvent(someMonthsLater+".newFirms(firms=1)");
		return ;
	}

	/**
	 * Returns a hash map that contains a list of parameters and their values.
	 * @param paramString - a string that contains parameters and their values.
	 * @return a hash map.
	 */
	private static Map<String,String> getParamHashMap(String paramString) {
		final Map<String,String> parametersMap = new HashMap<String,String>();
		final String[] parameters = paramString.split(",");
		for(final String line:parameters) {
			final String[] parameter = line.split("=",2);
			parametersMap.put(parameter[0], parameter[1]);
		}
		return parametersMap;
	}

	/** The counter of firms created. */
	private int countFirms ;

	/** The list of the firms. */
	private final ArrayList<Firm> firmsList ;

	/** The type of the firms. */
	private String firmType;

	/** The list of wages offered by firms at the previous period. */
	final private ArrayList<Double> wages = new ArrayList<Double>();

	/**
	 * Creates a new firms sector.
	 */
	public BasicFirmSector() {
		this.firmsList = new ArrayList<Firm>() ;
	}

	/**
	 * Returns a wage selected at random in the list of the wages offered during the previous period.
	 * @return a Long.
	 */
	private Double getRandomWage() {
		final int size = this.wages.size();
		final Double wage;
		if (size>0) {
			 wage =  this.wages.get(getRandom().nextInt(size));
		} else {
			wage = null;
		}
		return wage;
	}

	/**
	 * Returns an employer selected at random.
	 * @return a employer.
	 */
	protected Employer getRandomEmployer() {
		final int size = this.firmsList.size();
		if (size>0)
			return this.firmsList.get(getRandom().nextInt(size));
		return null;
	}

	/**
	 * Returns a firm selected at random.
	 * @return a firm.
	 */
	protected Firm getRandomFirm() {
		final Firm result;
		final int size = this.firmsList.size();
		if (size>0) {
			result =  this.firmsList.get(getRandom().nextInt(size));
		} else {
			result=null;
		}
		return result;
	}

	/**
	 * Returns a list of firms selected at random.
	 * @param size the number of firms to select.
	 * @return a list of firms.
	 */
	protected Collection<Firm> getRandomFirms(int size) {
		final LinkedList<Firm> list = new LinkedList<Firm>();
		final int max = this.firmsList.size(); 
		if (size>max) {
			throw new RuntimeException("Not enough firms.");
		}
		while(list.size()<size) {
			final int id = getRandom().nextInt(max);
			final Firm f = this.firmsList.get(id);
			if (!list.contains(f)) {
				list.add(f);
			}
		}
		return list;
	}

	/**
	 * Returns a provider selected at random in the list of providers of final goods.
	 * @return a provider (<code>null</code> if the list is empty).
	 */
	protected Provider getRandomProviderOfFinalGoods() {
		return getRandomFirm();
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.FirmSector#close(jamel.util.data.PeriodDataset)
	 */
	@Override
	public void close(PeriodDataset macroDataset) {
		this.wages.clear();
		for (Firm selectedFirm : firmsList) {
			selectedFirm.close() ;
			this.wages.add(selectedFirm.getData().wage);
		}
		macroDataset.compileFirmsData(this.firmsList);
		final Collection<Firm> newList = new LinkedList<Firm>();
		for (Firm selectedFirm : firmsList) {
			if (!selectedFirm.isBankrupt()) {
				newList.add(selectedFirm);
			}
			else {
				failure(selectedFirm);
			}
		}
		firmsList.clear();
		firmsList.addAll(newList);
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.FirmSector#get(java.lang.String)
	 */
	@Override
	public Object get(String key) {
		final Object result;
		if (key.startsWith(Circuit.SELECT_A_LIST_OF_FIRMS)) {
			final int n = Integer.parseInt(key.split(",")[1]);
			result = this.getRandomFirms(n);
		} else if (key.equals(Circuit.SELECT_A_WAGE)) {
			result = this.getRandomWage();
		} else if (key.equals(Circuit.SELECT_AN_EMPLOYER)) {
			result = this.getRandomEmployer();
		} else if (key.equals(Circuit.SELECT_A_PROVIDER_OF_FINAL_GOODS)) {
			result = this.getRandomProviderOfFinalGoods();
		} else {
			throw new RuntimeException("Unexpected key: "+key);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.FirmSector#getFirmData(java.lang.String, java.lang.String)
	 */
	@Override
	public String getFirmData(String name, String keys) {
		String data = null;
		for(Firm firm:this.firmsList){
			if (firm.getName().equals(name)) {
				FirmDataset firmData = firm.getData();
				if (firmData!=null) {
					data=firmData.getData(keys);
				}
			}
		}
		return data;
	}							

	/* (non-Javadoc)
	 * @see jamel.agents.firms.FirmSector#getFirmsData(java.lang.String)
	 */
	@Override
	public String[] getFirmsData(String keys) {
		final String[] data = new String[this.firmsList.size()];
		int id = 0;
		for(Firm firm:this.firmsList){
			FirmDataset firmData = firm.getData();
			if (firmData!=null) {
				data[id]=firmData.getData(keys);
			}
			id++;
		}
		return data;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.FirmSector#kill()
	 */
	@Override
	public void kill() {
		for (Firm selectedFirm : firmsList) selectedFirm.kill() ;
		this.firmsList.clear();
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.FirmSector#newFirms(java.lang.String)
	 */
	@Override
	public void newFirms(String parameters) {
		final Map<String, String> parametersMap = getParamHashMap(parameters);
		Integer newFirms = null;
		for(Entry<String, String> entry : parametersMap.entrySet()) {
			final String key = entry.getKey();
			final String value = entry.getValue();
			if (key.equals("firms")) {
				if (newFirms != null) throw new RuntimeException("Event new firms : Duplicate parameter : firms.");
				newFirms = Integer.parseInt(value);
			}
			else {
				throw new RuntimeException("Unknown parameter: "+key);
			}
		}
		if (newFirms==null) 
			throw new RuntimeException("Missing parameter: firms.");
		for (int count = 0 ; count<newFirms ; count++){
			countFirms ++ ;
			final CapitalOwner owner = (CapitalOwner) Circuit.getResource(Circuit.SELECT_A_CAPITAL_OWNER);
			try {
				final String name = "Company."+countFirms;
				final Firm newFirm = (Firm) Class.forName(this.firmType,false,ClassLoader.getSystemClassLoader()).getConstructor(String.class,CapitalOwner.class).newInstance(name,owner);
				firmsList.add(newFirm) ;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("Firm creation failure"); 
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new RuntimeException("Firm creation failure"); 
			} catch (SecurityException e) {
				e.printStackTrace();
				throw new RuntimeException("Firm creation failure"); 
			} catch (InstantiationException e) {
				e.printStackTrace();
				throw new RuntimeException("Firm creation failure"); 
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Firm creation failure"); 
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Firm creation failure"); 
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				throw new RuntimeException("Firm creation failure"); 
			}
		}
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.FirmSector#open()
	 */
	@Override
	public void open() {
		Collections.shuffle(firmsList,getRandom());
		for (Firm selectedFirm : firmsList) {
			selectedFirm.open() ;
		}
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.FirmSector#payDividend()
	 */
	@Override
	public void payDividend() {
		for (Firm selectedFirm : firmsList) {			
			if (!selectedFirm.isBankrupt()) {
				selectedFirm.payDividend() ;			
			}
		}
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.FirmSector#planProduction()
	 */
	@Override
	public void planProduction() {
		for (Firm selectedFirm : firmsList) {
			if (!selectedFirm.isBankrupt()) {
				selectedFirm.prepareProduction() ;
			}
		}
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.FirmSector#production()
	 */
	@Override
	public void production() {
		for (Firm selectedFirm : firmsList) {
			if (!selectedFirm.isBankrupt()) {
				selectedFirm.production() ; 
			}
		}
	}

	@Override
	public void setFirmType(String firmType) {
		this.firmType=firmType;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.FirmSector#setVerbose(java.lang.String)
	 */
	@Override
	public void setVerbose(String name) {
		for (Firm selectedFirm : firmsList) {
			if (selectedFirm.getName().equals(name)) {
				selectedFirm.setVerbose(true);
			}
		}
	}


}