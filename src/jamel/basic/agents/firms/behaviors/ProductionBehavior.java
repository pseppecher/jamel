package jamel.basic.agents.firms.behaviors;

import jamel.util.Circuit;

/**
 * The production behavior.
 */
public class ProductionBehavior {
	
	/** The capacity utilization rate targeted.<p>
	 * Capacity utilization rate: "A metric used to measure the rate at which 
	 * potential output levels are being met or used. Displayed as a percentage, 
	 * capacity utilization levels give insight into the overall slack that is 
	 * in the economy or a firm at a given point in time. If a company is running 
	 * at a 70% capacity utilization rate, it has room to increase production up 
	 * to a 100% utilization rate without incurring the expensive costs of 
	 * building a new plant or facility.
	 * Also known as "operating rate".
	 * (<a href="http://www.investopedia.com/terms/c/capacityutilizationrate.asp">Investopedia</a>)  
	 */
	private Float utilizationRateTargeted;
	
	/**
	 * Creates a new behavior.
	 * @param target the initial level.
	 */
	public ProductionBehavior(float target) {
		this.utilizationRateTargeted = target;
	}

	/**
	 * Returns the capacity utilization targeted.
	 * @return a float in [0,1].
	 */
	public float getTarget() {
		return this.utilizationRateTargeted;
	}

	/**
	 * Updates the target of capacity utilization.
	 * @param flexibility the flexibility of the capacity utilization.
	 * @param inventoryRatio the inventory level.
	 */
	public void update(final float flexibility, final double inventoryRatio) {
		final float alpha1 = Circuit.getRandom().nextFloat();
		final float alpha2 = Circuit.getRandom().nextFloat();
		final float delta = (alpha1*flexibility);
		if (inventoryRatio<1-alpha1*alpha2) { // Low level
			this.utilizationRateTargeted += delta;
			if (this.utilizationRateTargeted>1) {
				this.utilizationRateTargeted = 1f;
			}
		}
		else if (inventoryRatio>1+alpha1*alpha2) { // High level
			this.utilizationRateTargeted -= delta;
			if (this.utilizationRateTargeted<0) {
				this.utilizationRateTargeted = 0f;
			}
		}
	}

}

//***
