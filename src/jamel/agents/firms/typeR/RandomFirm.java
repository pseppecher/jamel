package jamel.agents.firms.typeR;

import jamel.Circuit;
import jamel.agents.firms.BasicFirm;
import jamel.agents.roles.CapitalOwner;

import java.util.Map;
import java.util.Map.Entry;

/**
 * A firm 
 */
public class RandomFirm extends BasicFirm {

	/**
	 * Creates a new firm with the given parameters.
	 * @param aName the name. 
	 * @param owner the owner.
	 * @param someParameters a map that contains parameters.
	 */
	public RandomFirm(String aName, CapitalOwner owner, Map<String, String> someParameters) {
		super(aName, owner, someParameters);
	}

	/**
	 * Initializes the new firm.
	 */
	@Override
	protected void init() {
		super.init();
		if (getCurrentPeriod().getValue()>60) {
			final Map<String, Object> parameters = Circuit.getRandomFirm().getParameters();
			for(Entry<String, Object> entry : parameters.entrySet()) {
				this.blackboard.put(entry.getKey(), entry.getValue(), null);
			}
			return;
		}
	}
		
}
