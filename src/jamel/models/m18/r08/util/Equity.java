package jamel.models.m18.r08.util;

import jamel.models.m18.r08.roles.Shareholder;

/**
 * Represent an equity or ownership title.
 * 
 * 2018-02-16: Equity
 * pour que les shareholders puissent savoir la richesse qu'ils
 * détiennent sous la forme d'entreprises.
 */
public interface Equity {

	/**
	 * Returns the name of the company.
	 * 
	 * @return the name of the company.
	 */
	String getCompanyName();

	/**
	 * Returns the owner.
	 * 
	 * @return the owner.
	 */
	Shareholder getOwner();

	/**
	 * Returns the value of the title.
	 * 
	 * @return the value of the title.
	 */
	long getValue();

	/**
	 * Returns {@code true} if this equity is canceled.
	 * 
	 * @return {@code true} if this equity is canceled.
	 */
	boolean isCanceled();

}
