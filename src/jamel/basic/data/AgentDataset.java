package jamel.basic.data;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * An interface for the data of one individual agent.
 */
public interface AgentDataset {

	/**
	 * Returns true if this dataset contains a value for the specified key.
	 * 
	 * @param key
	 *            key whose presence in this dataset is to be tested.
	 * @return <code>true</code> if this map contains a value for the specified
	 *         key.
	 */
	public boolean containsKey(String key);

	/**
	 * Exports the headers.
	 * 
	 * @param outputFile
	 *            the ouput file.
	 * @throws IOException
	 *             If something goes wrong.
	 */
	public void exportHeadersTo(File outputFile) throws IOException;

	/**
	 * Exports the data.
	 * 
	 * @param outputFile
	 *            the ouput file.
	 * @throws IOException
	 *             If something goes wrong.
	 */
	public void exportTo(File outputFile) throws IOException;

	/**
	 * Returns the value to which the specified key is mapped, or
	 * <code>null</code> if this dataset contains no mapping for the key.
	 * 
	 * @param key
	 *            the key whose associated value is to be returned.
	 * @return the value to which the specified key is mapped, or
	 *         <code>null</code> if this dataset contains no mapping for the
	 *         key.
	 */
	public Double get(String key);

	/**
	 * Returns the info to which the specified key is mapped, or
	 * <code>null</code> if this dataset contains no info for the key.
	 * 
	 * @param key
	 *            the key whose associated info is to be returned.
	 * @return the info to which the specified key is mapped, or
	 *         <code>null</code> if this dataset contains no info for the key.
	 */
	public String getInfo(String key);

	/**
	 * Returns the name of the agent.
	 * 
	 * @return the name of the agent.
	 */
	public String getName();

	/**
	 * Returns a <code>Set</code> view of the keys contained in this dataset.
	 * 
	 * @return a <code>Set</code> view of the keys contained in this dataset.
	 */
	public Set<String> keySet();

	/**
	 * Associates the specified value with the specified key in this dataset. If
	 * the dataset previously contained a mapping for the key, the old value is
	 * replaced.
	 * 
	 * @param key
	 *            a String with which the specified value is to be associated
	 * @param value
	 *            a Long value to be associated with the specified key
	 * @return the previous value associated with <code>key</code>, or
	 *         <code>null</code> if there was no mapping for <code>key</code>.
	 */
	public Double put(String key, Number value);

	/**
	 * Copies all of the data from the specified dataset to this dataset. These
	 * data replace any data that this dataset had for any of the keys currently
	 * in the specified dataset.
	 * 
	 * @param agentDataset
	 *            data to be stored in this dataset.
	 */
	public void putAll(AgentDataset agentDataset);

	/**
	 * Puts the specified info in this dataset.
	 * 
	 * @param key
	 *            key with which the specified info is to be associated.
	 * @param info
	 *            info to be associated with the specified key.
	 */
	public void putInfo(String key, String info);

}

// ***
