package jamel.basic.agents.firms.behaviors;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit test case for {@link jamel.basic.agents.firms.behaviors.BasicDividendBehavior}.
 */
public class BasicDividendBehaviorTest {

	/**
	 * Test method for {@link jamel.basic.agents.firms.behaviors.BasicDividendBehavior#getDividend(long, long, long, float, float)}.
	 */
	@Test
	public void test01GetDividend() {
		final float capitalRatioTarget = 0.5f;
		final long cash = 100;
		final long inventories = 1000;
		final long liabilities = 500;
		final float propensityToDistributeCapital = 0.5f;
		assertEquals(25,BasicDividendBehavior.getDividend(cash,inventories,liabilities,capitalRatioTarget,propensityToDistributeCapital));
	}
	
	/**
	 * Test method for {@link jamel.basic.agents.firms.behaviors.BasicDividendBehavior#getDividend(long, long, long, float, float)}.<p>
	 * What if propensityToDistributeCapital = 1.
	 */
	@Test
	public void test02GetDividend() {
		final float capitalRatioTarget = 0.5f;
		final long cash = 100;
		final long inventories = 1000;
		final long liabilities = 500;
		final float propensityToDistributeCapital = 1f;
		assertEquals(50,BasicDividendBehavior.getDividend(cash,inventories,liabilities,capitalRatioTarget,propensityToDistributeCapital));
	}

	/**
	 * Test method for {@link jamel.basic.agents.firms.behaviors.BasicDividendBehavior#getDividend(long, long, long, float, float)}.<p>
	 * 	What if not enough capital.
	 */
	@Test
	public void test03GetDividend() {
		final float capitalRatioTarget = 0.5f;
		final long cash = 100;
		final long inventories = 900;
		final long liabilities = 500;
		final float propensityToDistributeCapital = 0.5f;
		assertEquals(0,BasicDividendBehavior.getDividend(cash,inventories,liabilities,capitalRatioTarget,propensityToDistributeCapital));
	}

}

// ***
