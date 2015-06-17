package jamelV3.basic.data;

import jamelV3.basic.sector.SectorDataset;

import java.util.List;

import org.jfree.data.xy.XYDataItem;

/**
 * Represents the macroeconomic dataset.
 */
public interface MacroDataset {

	/**
	 * Removes all of the mappings from this dataset. The dataset will be empty after this call returns.
	 */
	void clear();

	/**
	 * Returns the value to which the specified key is mapped, or <code>null</code> if this dataset contains no mapping for the key.
	 * @param key the key whose associated value is to be returned.
	 * @return the value to which the specified key is mapped, or <code>null</code> if this dataset contains no mapping for the key.
	 */
	Double get(String key);
	
	/**
	 * Returns a list of XYDataItem that contains the specified values for each agent selected.
	 * @param sector  the sector.
	 * @param xKey  the key for x values.
	 * @param yKey  the key for y values.
	 * @param select  the method of selection.
	 * @return a list of XYDataItem.
	 */
	List<XYDataItem> getScatterData(String sector, String xKey, String yKey, String select);
	
	/**
	 * Returns the xyz data (an array with length 3, containing three arrays of equal length, the first containing the x-values, the second containing the y-values and the third containing the z-values).
	 * @param sector  the sector.
	 * @param xKey  the x key.
	 * @param yKey  the y key.
	 * @param zKey  the z key.
	 * @return the xyz data.
	 */
	double[][] getXYZData(String sector, String xKey, String yKey, String zKey);

	/**
	 * Associates the specified SectorDataset with the specified sector in this macro dataset. 
	 * If the macro dataset previously contained a mapping for the sector, the old SectorDataset is replaced.
	 * @param sector the name of the sector with which the specified value is to be associated.
	 * @param sectorDataset SectorDataset to be associated with the specified sector.
	 */
	void putData(String sector, SectorDataset sectorDataset);

}

// ***
