package jamel.agents.firms.typeA;

import jamel.agents.firms.BasicFirm;
import jamel.agents.firms.managers.PricingManager;
import jamel.agents.roles.CapitalOwner;

import java.util.Map;

/**
 * Une firme qui utilise un manager de prix sensible au niveau de la production.
 */
public class FirmA1 extends BasicFirm {

	public FirmA1(String aName, CapitalOwner owner, Map<String, String> someParameters) {
		super(aName, owner, someParameters);
	}

	/**
	 * Returns a new basic pricing manager.
	 * @return a new basic pricing manager.
	 */
	@Override
	protected PricingManager getNewPricingManager() {
		return new PricingManagerA1(this.blackboard);
	}

}
