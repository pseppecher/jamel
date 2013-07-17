package jamel.agents.firms.typeR;

import jamel.agents.firms.Labels;
import jamel.agents.firms.managers.PricingManager;
import jamel.util.Blackboard;

public class RandomMarkupPricingManager extends PricingManager {

	private Double markup = 1.3;

	public RandomMarkupPricingManager(Blackboard blackboard) {
		super(blackboard);
	}

	/**
	 * Updates the unit price.
	 */
	public void updatePrice() {
		final Double unitCost = (Double)this.blackBoard.get(Labels.UNIT_COST);
		this.currentPrice = this.markup *unitCost;
		this.blackBoard.put(Labels.PRICE, this.currentPrice);
	}

}
