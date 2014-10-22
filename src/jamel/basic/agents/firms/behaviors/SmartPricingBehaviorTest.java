package jamel.basic.agents.firms.behaviors;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class SmartPricingBehaviorTest {
	
	private SmartPricingBehavior pricingBehavior;
	

	@Before
	public void setup() {
		this.pricingBehavior = new SmartPricingBehavior();
	}

	@After
	public void tearDown() throws Exception {
		this.pricingBehavior=null;
	}
	
	@Test
	public void test1() {
		float priceFlexibility = 0.005f;
		int unitCost = 100;
		float inventoryRatio = 0.9f;
		double salesRatio = 1f;
		pricingBehavior.updatePrice(inventoryRatio, salesRatio, priceFlexibility, unitCost);
		assertTrue("Price = "+pricingBehavior.getPrice(),pricingBehavior.getPrice()>100);
	}

	@Test
	public void test2() {
		float priceFlexibility = 0.005f;
		int unitCost = 100;
		float inventoryRatio = 1.1f;
		double salesRatio = 0.9f;
		pricingBehavior.updatePrice(inventoryRatio, salesRatio, priceFlexibility, unitCost);
		assertTrue("Price = "+pricingBehavior.getPrice(),pricingBehavior.getPrice()<100);
	}

	@Test
	public void test3() {
		float priceFlexibility = 0.005f;
		int unitCost = 100;
		float inventoryRatio = 1.1f;
		double salesRatio = 1f;
		pricingBehavior.updatePrice(inventoryRatio, salesRatio, priceFlexibility, unitCost);
		assertEquals("Price",100,pricingBehavior.getPrice(),0);
	}

	@Test
	public void test4() {
		float priceFlexibility = 0.005f;
		int unitCost = 100;
		float inventoryRatio = 0.9f;
		double salesRatio = 0.9f;
		pricingBehavior.updatePrice(inventoryRatio, salesRatio, priceFlexibility, unitCost);
		assertEquals("Price",100,pricingBehavior.getPrice(),0);
	}

}

// ***
