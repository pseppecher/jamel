package jamel.jamel.firms.factory;

import jamel.jamel.widgets.Asset;

/**
 * An interface for the work-in-progress materials.
 * <p>
 * "Material that has entered the production process but is not yet a
 * finished product. Work in progress (WIP) therefore refers to all
 * materials and partly finished products that are at various stages of the
 * production process. WIP excludes inventory of raw materials at the start
 * of the production cycle and finished products inventory at the end of the
 * production cycle." (ref:
 * <a href="http://www.investopedia.com/terms/w/workinprogress.asp">www.
 * investopedia.com</a>)
 */
interface WorkInProgress extends Asset {

	/**
	 * Deletes the content of this WIP.
	 */
	void delete();

	/**
	 * Depreciates this WIP.
	 * @param d the rate of depreciation.
	 */
	void depreciate(double d);

	/**
	 * Returns a heap of materials of the specified completion.
	 * 
	 * @param completion
	 *            the completion of the materials to be returned.
	 * @return a heap of materials of the specified completion.
	 */
	Materials get(Rational completion);

	/**
	 * Returns an array of materials, sorted by completion in descending
	 * order.
	 * 
	 * @param maxCompletion
	 *            the completion of the most advanced materials to be
	 *            returned.
	 * @return an array of materials, sorted by completion in descending
	 *         order.
	 */
	Materials[] getStuff(int maxCompletion);

	/**
	 * Puts all materials of the specified WIP into this WIP.
	 * 
	 * @param workInProgress
	 *            the materials to be added.
	 */
	void put(WorkInProgress workInProgress);

	/**
	 * Puts the specified stuff into this WIP.
	 * 
	 * @param stuff
	 *            the materials to be added.
	 */
	void putStuff(Materials stuff);

}