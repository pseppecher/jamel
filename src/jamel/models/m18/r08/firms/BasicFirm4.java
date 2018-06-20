package jamel.models.m18.r08.firms;

import jamel.util.Sector;

/*
 * 2018-04-12: jamel.models.m18.r07.firms.BasicFirm4
 * Pour tester les effets d'une adaptation intentionnelle, tournée vers la
 * réalisation de profits supérieurs.
 */

/**
 * An extension of {@link BasicFirm2} to test a profit oriented adaptation.
 */
public class BasicFirm4 extends BasicFirm2 {

	/**
	 * Creates a new firm.
	 * 
	 * @param sector
	 *            the sector.
	 * @param id
	 *            the identification number of the agent.
	 */
	public BasicFirm4(Sector sector, int id) {
		super(sector, id);
	}

	/**
	 * Imitates the model.
	 * 
	 * @param model
	 *            the firm to be imitated.
	 */
	private void imitation(BasicFirm4 model) {
		this.lastImitiation = this.getPeriod();
		this.imitations = 1;
		this.targetDebtRatio = model.targetDebtRatio;
		this.markup = model.markup;
		this.workforceManager.wage = model.workforceManager.wage;
		this.workforceManager.wageFactor = model.workforceManager.wageFactor;
	}

	/**
	 * Imitates an other firm after a bankruptcy.
	 */
	@Override
	protected void imitation() {
		final int now = this.getPeriod();
		if (now > cons.supervision) {
			if (now > this.lastImitiation + 12) {
				final BasicFirm4 model = (BasicFirm4) sector.select();
				imitation(model);
				/*
				 * 2018-04-09
				 * Si nécessaire, on déprécie les stock de bien finis.
				 */
				if (!this.factory.getInventories().isEmpty()) {
					final Double volume = model.agentDataset.average(keys.salesVolume, 3);
					if (volume != 0) {
						final double costs = model.agentDataset.average(keys.salesCosts, 3);
						final double unitCost = costs / volume;
						if (this.factory.getInventories().valuePerUnit() > unitCost) {
							this.factory.depreciateInventories(unitCost);
						}
					}
				}
			}
		}
	}

	@Override
	public void open() {
		super.open();
		if (this.getPeriod() > this.lastImitiation + 12 && this.getRandom().nextFloat() > 0.9) {
			final BasicFirm4 model = (BasicFirm4) sector.select();
			final Double modelAssets = model.agentDataset.getData(keys.assets, this.getPeriod() - 12);
			final Double myAssets = this.agentDataset.getData(keys.assets, this.getPeriod() - 12);
			if (modelAssets != null && modelAssets != 0 && myAssets != null && myAssets != 0) {
				final double modelNetProfit = model.agentDataset.sum(keys.netProfit, 12);
				final double modelProfitRate = modelNetProfit / modelAssets;
				final double myNetProfit = this.agentDataset.sum(keys.netProfit, 12);
				final double myProfitRate = myNetProfit / myAssets;
				if (modelProfitRate > myProfitRate) {
					this.imitation(model);
				}
			}

		}
	}

}
