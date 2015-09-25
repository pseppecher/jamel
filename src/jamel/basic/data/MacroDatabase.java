package jamel.basic.data;

import java.util.List;

import org.jfree.data.xy.XYDataItem;

import jamel.basic.util.InitializationException;

/**
 * The macro-economic database. It can provide individual and aggregate data.
 */
public interface MacroDatabase {

	/**
	 * Removes all of the mappings from this database. The database will be
	 * empty after this call returns.
	 */
	public void clear();

	/**
	 * Returns an array of double that contains the specified value for each
	 * agent selected.
	 * 
	 * @param sector
	 *            the name of the sector.
	 * @param key
	 *            the key for the values.
	 * @param t
	 *            the period.
	 * @param select
	 *            the method of selection.
	 * @return an array of double.
	 */
	public Double[] getDistributionData(String sector, String key, int t, String select);

	/**
	 * Returns an {@link Expression} the value of which will be a statistical
	 * function of this dataset.
	 * 
	 * @param query
	 *            a string that contains the definition of the function to be
	 *            returned.
	 * @return an {@link Expression}.
	 * @throws InitializationException
	 *             if something goes wrong.
	 */
	public Expression getFunction(String query) throws InitializationException;

	/**
	 * Returns a list of {@link XYDataItem} that contains the specified values for each
	 * agent selected.
	 * 
	 * @param sector
	 *            the name of the sector.
	 * @param xKey
	 *            the key for x values.
	 * @param yKey
	 *            the key for y values.
	 * @param t
	 *            the period.
	 * @param select
	 *            the method of selection.
	 * @return a list of XYDataItem.
	 */
	public List<XYDataItem> getScatterData(String sector, String xKey, String yKey, int t, String select);

	/**
	 * Returns the specified xyz data (an array with length 3, containing three arrays of
	 * equal length, the first containing the x-values, the second containing
	 * the y-values and the third containing the z-values).
	 * 
	 * @param sector
	 *            the sector.
	 * @param xKey
	 *            the x key.
	 * @param yKey
	 *            the y key.
	 * @param zKey
	 *            the z key.
	 * @param t
	 *            the period.
	 * @return the xyz data.
	 */
	public double[][] getXYZData(String sector, String xKey, String yKey, String zKey, int t);

	/**
	 * Returns a new expression that contains a query on this database.
	 * 
	 * @param query
	 *            a string that contains the query to create.
	 * @return a new expression that contains a query on this database.
	 * @throws InitializationException
	 *             if something goes wrong.
	 */
	public Expression newQuery(String query) throws InitializationException;

	/**
	 * Associates the specified {@link SectorDataset} with the specified sector in this
	 * database. If the database previously contained a dataset for the sector,
	 * the old dataset is replaced.
	 * 
	 * @param sector
	 *            the name of the sector with which the specified value is to be
	 *            associated.
	 * @param sectorDataset
	 *            {@link SectorDataset} to be associated with the specified sector.
	 */
	public void putData(String sector, SectorDataset sectorDataset);

	public String getMessage(String sector, String agent, String key, int lag);

}

// ***