/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2014, Pascal Seppecher and contributors.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/javadoc/index.html>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JAMEL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.]
 * [JAMEL uses JFreeChart, copyright by Object Refinery Limited and Contributors. 
 * JFreeChart is distributed under the terms of the GNU Lesser General Public Licence (LGPL). 
 * See <http://www.jfree.org>.]
 */

package jamel.exploratory.mod1402;

import java.util.LinkedList;

import jamel.Circuit;
import jamel.agents.households.BasicHousehold;

/**
 * An emotional household that uses different saving propensities according to its optimism.
 * (New version of the old Household131201)
 */
public class EmotionalHousehold extends BasicHousehold {

	/** The maximum number of friends of a household. */
	final static private int maxFriends = 3;

	/** The parameter label for the herding propensity. */
	private static final String PARAM_HERDING = "Households.confidence.herdingPropensity";

	/** The parameter label for the high propensity to save. */
	private static final String PARAM_SAVING_PROPENSITY2 = "Households.savings.propensityToSave2";

	/** The parameter label for the high saving ratio target. */
	private static final String PARAM_SAVING_TARGET2 = "Households.savings.ratioTarget2";
	
	/** The friends of the household. */
	final private LinkedList<EmotionalHousehold> friends = new LinkedList<EmotionalHousehold>();
	
	/** A flag that indicates whether the household is optimist or not. */
	private boolean optimist=getRandom().nextBoolean();

	/** The new optimism state of the household. */
	protected boolean newOptimist=getRandom().nextBoolean();

	/**
	 * Creates a new household.
	 * @param aName  the name of the household.
	 */
	public EmotionalHousehold(String aName) {
		super(aName);
	}

	/**
	 * Updates the confidence of the household.
	 */
	private void updateConfidence() {
		final float herding = Float.parseFloat(Circuit.getParameter(PARAM_HERDING));
		if (this.unemploymentDuration>0) {
			this.newOptimist= false;			
		}
		else if (getRandom().nextFloat()>(herding)) {
			this.newOptimist= true;			
		}
		else {
			int optimism = 0;
			int pessimism = 0;
			for(EmotionalHousehold h:friends) {
				if (h.isOptimist()) {
					optimism++;
				}
				else {
					pessimism++;
				}
				this.newOptimist= (optimism>pessimism);			
			}
		}
	}

	/**
	 * Updates the list of friends.
	 */
	private void updateFriends() {
		this.friends.clear();
		while (this.friends.size()<maxFriends ) {
			final EmotionalHousehold h = (EmotionalHousehold) Circuit.getResource(Circuit.SELECT_A_HOUSEHOLD);
			if (!this.friends.contains(h)) {
				this.friends.add(h);
			}
		}
	}

	/**
	 * Updates the saving behavior according to its optimism.
	 */
	private void updateSavingBehavior() {
		if (!this.newOptimist) {
			this.savingPropensity = Float.parseFloat(Circuit.getParameter(PARAM_SAVING_PROPENSITY2));
			this.savingRatioTarget = Float.parseFloat(Circuit.getParameter(PARAM_SAVING_TARGET2));;
		}
	}

	@Override
	public void close() {
		super.close();
		this.optimist=this.newOptimist;
		this.data.setOptimism(this.optimist);
	}

	/**
	 * Returns the optimism of the household.
	 * @return  <code>true</code> if the household is optimist, <code>false</code> otherwise.
	 */
	public Boolean isOptimist() {
		return this.optimist;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.households.BasicHousehold#open()
	 */
	@Override
	public void open() {
		super.open();
		this.updateFriends();
		this.updateConfidence();
		this.updateSavingBehavior();
	}

}

















