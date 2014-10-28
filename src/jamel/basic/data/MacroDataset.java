package jamel.basic.data;

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
	 * Associates the specified SectorDataset with the specified sector in this macro dataset. 
	 * If the macro dataset previously contained a mapping for the sector, the old SectorDataset is replaced.
	 * @param sector the name of the sector with which the specified value is to be associated.
	 * @param sectorDataset SectorDataset to be associated with the specified sector.
	 */
	void putData(String sector, SectorDataset sectorDataset);

	/**
	 * Returns a list of XYDataItem that contains the specified values for each agent selected.
	 * @param target a string that contain the instructions to select the agents: the name of the sector + dot + the method of selection of the agents. 
	 * @param xKey the key for x values.
	 * @param yKey the key for y values.
	 * @return a list of XYDataItem.
	 */
	List<XYDataItem> getScatter(String target, String xKey, String yKey);

}

// ***
