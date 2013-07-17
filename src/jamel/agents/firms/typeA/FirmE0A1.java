package jamel.agents.firms.typeA;

import jamel.agents.firms.managers.PricingManager;
import jamel.agents.firms.typeE.FirmE0;
import jamel.agents.roles.CapitalOwner;

import java.util.Map;

/**
 * Une firme qui utilise un manager de prix sensible au niveau de la production.
 */
public class FirmE0A1 extends FirmE0 {

	/**
	 * @param aName
	 * @param owner
	 * @param someParameters
	 */
	public FirmE0A1(String aName, CapitalOwner owner,Map<String, String> someParameters) {
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
