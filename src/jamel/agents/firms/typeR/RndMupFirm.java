package jamel.agents.firms.typeR;

import jamel.agents.firms.BasicFirm;
import jamel.agents.firms.managers.PricingManager;
import jamel.agents.roles.CapitalOwner;

import java.util.Map;

public class RndMupFirm extends BasicFirm {

	public RndMupFirm(String aName, CapitalOwner owner, Map<String, String> someParameters) {
		super(aName, owner, someParameters);
	}

	/**
	 * Returns a new basic pricing manager.
	 * @return a new basic pricing manager.
	 */
	@Override
	protected PricingManager getNewPricingManager() {
		return new RandomMarkupPricingManager(this.blackboard);
	}

}
