package jamel.basic.data;

/**
 * Represents the macroeconomic dataset.
 */
public interface MacroDataset {

	/**
	 * Returns the value to which the specified key is mapped, or <code>null</code> if this dataset contains no mapping for the key.
	 * @param key the key whose associated values is to be returned.
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
	 * Removes all of the mappings from this dataset. The dataset will be empty after this call returns.
	 */
	void clear();

}

// ***
