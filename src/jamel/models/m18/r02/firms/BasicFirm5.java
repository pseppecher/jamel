package jamel.models.m18.r02.firms;

import jamel.util.Sector;

/*
 * 2018-03-17
 * jamel/models/m18/r01/firms/BasicFirm5.java
 * Extension de BasicFirm4.java
 * Apprend a fixer son taux d'utilisation normal selon le BSP.
 */

/**
 * A firm with a post-Keynesian/evolutionary investment behavior.
 */ 
public class BasicFirm5 extends BasicFirm4 {

	/**
	 * Creates a new basic firm.
	 * 
	 * @param sector
	 *            the sector.
	 * @param id
	 *            the identification number of the agent.
	 */
	public BasicFirm5(Sector sector, int id) {
		super(sector, id);
		this.normalUtilizationRate = this.getRandom().nextFloat() * 0.5f + 0.5f;
	}

	/**
	 * Imitates an other firm (copies its target debt ratio).
	 */
	@Override
	protected void imitation() {
		final int now = this.getPeriod();
		if (now > cons.supervision) {
			if (now > this.lastImitiation + 12) {
				BasicFirm5 firm = (BasicFirm5) sector.select();
				this.lastImitiation = now;
				this.imitations = 1;
				this.workforceManager.setWage(firm.workforceManager.wage);
				this.targetDebtRatio = firm.targetDebtRatio;
				this.markup = firm.markup;
				this.normalUtilizationRate = firm.normalUtilizationRate;
			}
		}
	}

	/**
	 * Mutates
	 */
	@Override
	protected void mutation() {
		if (this.getPeriod() > cons.supervision) {
			this.targetDebtRatio += (float) (cons.mutation * getRandom().nextGaussian());
			this.markup += (float) (cons.mutation * getRandom().nextGaussian());
			this.normalUtilizationRate += (float) (cons.mutation * getRandom().nextGaussian());
			if (this.targetDebtRatio > 1) {
				this.targetDebtRatio = 1f;
			} else if (this.targetDebtRatio < 0) {
				this.targetDebtRatio = 0f;
			}
			if (this.normalUtilizationRate > 1) {
				this.normalUtilizationRate = 1f;
			} else if (this.normalUtilizationRate < 0) {
				this.normalUtilizationRate = 0f;
			}
		}
	}

}
