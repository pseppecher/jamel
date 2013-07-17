package jamel.agents.firms.typeA;

import jamel.agents.firms.managers.WorkforceManager;
import jamel.agents.roles.CapitalOwner;

import java.util.Map;

/**
 * Une firme qui tient compte du niveau de la production quand elle fixe les prix et les salaires.
 */
public class FirmE0A2 extends FirmE0A1 {

	public FirmE0A2(String aName, CapitalOwner owner, Map<String, String> someParameters) {
		super(aName, owner, someParameters);
	}
	
	/**
	 * Returns a new workforce manager.
	 * @return the new manager.
	 */
	protected WorkforceManager getNewWorkforceManager() {
		return new WorkforceManagerA1(this, this.account,this.blackboard);
	}

}
