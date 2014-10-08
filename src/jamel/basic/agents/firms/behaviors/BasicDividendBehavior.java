package jamel.basic.agents.firms.behaviors;

/**
 * Encapsulates the dividend behavior of the firm.
 */
public class BasicDividendBehavior {
	
	/**
	 * Computes and returns the dividend.
	 * @param cash  the cash 
	 * (= "an item on the balance sheet that reports the value of a company's assets that are cash or 
	 * can be converted into cash immediately" 
	 * <a href="http://www.investopedia.com/terms/i/inventory.asp">investopedia.com</a>)
	 * @param inventories  the inventories
	 * (= "the raw materials, work-in-process goods and completely finished goods that are considered 
	 * to be the portion of a business's assets that are ready or will be ready for sale" 
	 * <a href="http://www.investopedia.com/articles/04/031004.asp">investopedia.com</a>)
	 * @param liabilities  the liabilities
	 * ( = "the financial obligations a company owes to outside parties"
	 * <a href="http://www.investopedia.com/articles/04/031004.asp">investopedia.com</a>)
	 * @param capitalRatioTarget  the capital ratio targeted by the firm.
	 * @param propensityToDistributeCapital the propensity to distribute the capital.
	 * @return the dividend. 
	 */
	public static long getDividend(long cash, long inventories, long liabilities, float capitalRatioTarget, float propensityToDistributeCapital) {
		if (cash<0) {
			throw new IllegalArgumentException();
		}
		if (inventories<0) {
			throw new IllegalArgumentException();
		}
		if (liabilities<0) {
			throw new IllegalArgumentException();
		}
		if (capitalRatioTarget<0 || capitalRatioTarget>1) {
			throw new IllegalArgumentException();
		}
		if (propensityToDistributeCapital<0 || propensityToDistributeCapital>1) {
			throw new IllegalArgumentException();
		}
		final long assets=cash+inventories;
		final long capital=assets-liabilities;
		final long capitalTarget = (long) ((assets)*capitalRatioTarget );
		final long dividend;
		if (capital<=0) {
			dividend=0l;
		}
		else {
			if (capital<=capitalTarget) {
				dividend=0l;
			}
			else {
				dividend = Math.min((long) ((capital-capitalTarget)*propensityToDistributeCapital),cash);
			}
		}
		return dividend;
	}
	
}

// ***
