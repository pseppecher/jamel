package jamel.models.m18.r08.households;

import jamel.util.Sector;

/*
 * 2018-04-13: jamel.models.m18.r07.households.BasicShareholder2
 * 
 * Il s'agit d'expérimenter un comportement de consommation basé sur :
 * - une représentation différente du revenu : ne prend en compte que les
 * dividendes
 * - une représentation différente de la richesse : ne prend en compte que
 * l'épargne liquide
 */

/**
 * An extension of {@link BasicShareholder}
 */
public class BasicShareholder2 extends BasicShareholder {

	/**
	 * Creates a new shareholder.
	 * 
	 * @param sector
	 *            the sector of this shareholder.
	 * @param id
	 *            the id of this shareholder.
	 */
	public BasicShareholder2(Sector sector, int id) {
		super(sector, id);
	}

	/**
	 * Computes and returns the consumption budget.
	 * 
	 * @return the consumption budget.
	 */
	@Override
	protected long consumptionBudget() {
		final double averageIncome = this.agentDataset.sum(keys.dividends, 12) / 12;
		final double savingsTarget = 12. * averageIncome * this.consts.savingsRatioTarget;
		final double savings = this.account.getAmount() - averageIncome;

		this.putData(keys.savingsTarget, savingsTarget);
		this.putData(keys.savings, savings);

		long budget;
		if (getPeriod() < this.consts.supervision) {
			budget = this.account.getAmount();
		} else {
			final long budget2;
			if (savings < savingsTarget) {
				budget2 = (long) ((1. - this.consts.savingsPropensityToSave) * averageIncome);
			} else {
				budget2 = (long) (averageIncome
						+ (savings - savingsTarget) * this.consts.savingsPropensityToConsumeExcess);
			}
			budget = Math.min(this.account.getAmount(), budget2);
			this.putData(keys.consumptionBudget2, budget2);
		}
		return budget;
	}

}
