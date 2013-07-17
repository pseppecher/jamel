/* =========================================================
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.agents.firms;

import java.io.FileWriter;
import java.io.IOException;

/**
 *
 */
public class FirmDataset {
	
	/** The line separator. */
	final private static String rc = System.getProperty("line.separator");

	/** The number of period since the creation of the firm. */
	public int age;

	/** anticipatedWorkforce */
	public long anticipatedWorkforce;

	/** bankrupt */
	public boolean bankrupt;

	/** capital */
	public long capital;

	/** debt */
	public long debt;

	/** deposit */
	public long deposit;

	/** dividend */
	public long dividend;

	/** doubtDebt */
	public long doubtDebt;

	/** The name of the class of the factory. */
	public String factory;

	/** grossProfit */
	public long grossProfit;
	
	/** The budget for the purchase of intermediate needs. */
	public long intermediateNeedsBudget;
	
	/** intermediatesNeedsVolume */
	public int intermediateNeedsVolume;

	/** inventoriesValues */
	public long invUnfVal;

	/** inventoriesValues */
	public long invVal;

	/** The volume of finished goods in inventories. */
	public int invVol;

	/** jobOffers */
	public long jobOffers;

	/** machinerySize */
	public long machinery;

	/** The markup target. */
	public float markupTarget;

	/** maxProduction */
	public int maxProduction;

	/** name */
	public String name;

	/** price */
	public double price;

	/** production */
	public ProductionType production;

	/** The value of the production. */
	public long prodVal;

	/** productionVolume */
	public long prodVol;

	/** rawMaterialEffectiveVolume */
	public long rawMaterialEffectiveVolume;

	/** rawMaterialNormalVolume */
	public long rawMaterialNormalVolume;

	/** reserveTarget */
	public double reserveTarget;

	/** salesCostValue */
	public long salesCVal;

	/** salesPriceValue */
	public long salesPVal;

	/** salesVolume */
	public long salesVol;

	/** utilizationTarget */
	public float utilizationTarget;

	/** vacancies */
	public long vacancies;

	/** wageBill */
	public long wageBill;

	/** workforce */
	public long workforce;

	/**
	 * Returns the object for the given field. 
	 * @param field  the name of the field. 
	 * @return  an object.
	 * @throws NoSuchFieldException  if the field is not found.
	 */
	private Object getFieldValue(String field) throws NoSuchFieldException {
		Object value = null;
		try {
			value = this.getClass().getField(field).get(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		return value;
	}

	/**
	 * Writes the data in an output file.
	 * @param writer  the writer to use.
	 * @param keys  an array of string with the name of the fields to write.
	 */
	public void write(FileWriter writer, String[] keys) {
		try {
			for (String key:keys) {
				String value = null;
				try {
					value=this.getFieldValue(key).toString();
				} catch (NoSuchFieldException e) {
					value="No Such Field: "+key;
				}
				writer.write(value+",");
			}
			writer.write(rc);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error while writing data in the output file.");
		}		
	}

}