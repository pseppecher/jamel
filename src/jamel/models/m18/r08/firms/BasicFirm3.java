package jamel.models.m18.r08.firms;

import jamel.util.Sector;

public class BasicFirm3 extends BasicFirm2 {

	private float animalSpirit = 1;

	public BasicFirm3(Sector sector, int id) {
		super(sector, id);
		this.normalUtilizationRate = super.normalUtilizationRate;
	}

	protected long computesInvestmentBudget(float utilizationRate) {
		long budget;
		final double sales = this.agentDataset.sum(keys.salesValue, 12);
		final double costs = this.agentDataset.sum(keys.salesCosts, 12);
		final double overhead = this.agentDataset.sum(keys.overheadExpense, 12);
		final double interests = this.agentDataset.sum(keys.interests, 12);
		final double cashFlow = sales - (costs + interests + overhead);
		if (cashFlow > 0) {
			budget = (long) ((utilizationRate - normalUtilizationRate) * cashFlow * this.animalSpirit);
			final double capital = this.getCapital();
			final double capitalTarget = this.getCapitalTarget();
			if (capital < capitalTarget) {
				budget *= capital / capitalTarget;
			}
		} else {
			budget = 0;
		}
		return budget;
	}

	/**
	 * Imitates an other firm (copies its target debt ratio).
	 */
	@Override
	protected void imitation() {
		final int now = this.getPeriod();
		if (now > cons.supervision) {
			if (now > this.lastImitiation + 12) {
				BasicFirm3 firm = (BasicFirm3) sector.select();
				this.lastImitiation = now;
				this.imitations = 1;
				this.targetDebtRatio = firm.targetDebtRatio;
				this.markup = firm.markup;
				this.workforceManager.wage = firm.workforceManager.wage;
				this.workforceManager.wageFactor = firm.workforceManager.wageFactor;
				this.normalUtilizationRate = firm.normalUtilizationRate;
				this.animalSpirit = firm.animalSpirit;

				/*
				 * 2018-04-09
				 * Si nécessaire, on déprécie les stock de bien finis.
				 */
				if (!this.factory.getInventories().isEmpty()) {
					final Double volume = firm.agentDataset.average(keys.salesVolume, 3);
					if (volume != 0) {
						final double costs = firm.agentDataset.average(keys.salesCosts, 3);
						final double unitCost = costs / volume;
						if (this.factory.getInventories().valuePerUnit() > unitCost) {
							this.factory.depreciateInventories(unitCost);
						}
					}
				}
			}
		}
	}

	/**
	 * Mutates.
	 */
	@Override
	protected void mutation() {
		if (this.getPeriod() > cons.supervision) {
			this.animalSpirit += (float) (cons.mutation * getRandom().nextGaussian());
			if (this.animalSpirit < 0) {
				this.normalUtilizationRate = 0f;
			}
			this.putData(keys.animalSpirit, animalSpirit);
			this.normalUtilizationRate += (float) (cons.mutation * getRandom().nextGaussian());
			if (this.normalUtilizationRate > 1) {
				this.normalUtilizationRate = 1f;
			} else if (this.normalUtilizationRate < 0) {
				this.normalUtilizationRate = 0f;
			}
			this.markup += (float) (cons.mutation * getRandom().nextGaussian());
			this.targetDebtRatio += (float) (cons.mutation * getRandom().nextGaussian());
			if (this.targetDebtRatio > 1) {
				this.targetDebtRatio = 1f;
			} else if (this.targetDebtRatio < 0) {
				this.targetDebtRatio = 0f;
			}
		}
	}

}
