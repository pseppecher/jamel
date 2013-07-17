package jamel.agents.firms.typeA;

import jamel.Circuit;
import jamel.agents.firms.Labels;
import jamel.agents.firms.managers.PricingManager;
import jamel.util.Blackboard;

/**
 * Un pricing manager sensible au niveau de la production.
 */
public class PricingManagerA1 extends PricingManager {

	private static final Float normalLevel = 80f;

	public PricingManagerA1(Blackboard blackboard) {
		super(blackboard);
	}
	
	/**
	 * Updates the unit price.
	 */
	@Override
	public void updatePrice() {
		//final Float priceFlexibility = (Float)this.blackBoard.get(Labels.PRICE_FLEXIBILITY); DELETE
		final Float priceFlexibility = Float.parseFloat(Circuit.getParameter("Firms.priceFlexibility"));
		final Float inventoryRatio = (Float)this.blackBoard.get(Labels.INVENTORY_LEVEL_RATIO);
		final Float productionLevel = (Float)this.blackBoard.get(Labels.PRODUCTION_LEVEL);
		final Float npl;
		if (productionLevel<normalLevel) {
			npl = 0.5f*productionLevel/normalLevel;
		}
		else {
			npl = 0.5f*(1f+(productionLevel-normalLevel)/(100f-normalLevel));			
		}
		final Double unitCost = (Double)this.blackBoard.get(Labels.UNIT_COST);
		if (this.currentPrice==0) {
			this.currentPrice = (1.+getRandom().nextFloat()/2.)*unitCost;
			if ( Double.isNaN(currentPrice) )
				throw new RuntimeException("This price is NaN.") ;
		}
		else {
			final float alpha1 = getRandom().nextFloat();
			final float alpha2 = getRandom().nextFloat();
			final float alpha3 = getRandom().nextFloat();
			if ((inventoryRatio<1-alpha1*alpha2)&&(alpha1*alpha3<npl)) {
				this.currentPrice = this.currentPrice*(1f+alpha1*priceFlexibility);
			}
			else if ((inventoryRatio>1+alpha1*alpha2)&&(alpha1*alpha3<1-npl)) {
				this.currentPrice = this.currentPrice*(1f-alpha1*priceFlexibility);
				if (this.currentPrice<1) this.currentPrice = 1;
			}
		}
		this.blackBoard.put(Labels.PRICE, this.currentPrice);
	}	

}
