package jamel.models.m18.r08.firms;

import jamel.util.Sector;

/*
 * 2018-04-15: jamel.models.m18.r08.firms.BasicFirm6
 * Extension de BasicFirm5
 * 
 * 2018-04-12: jamel.models.m18.r07.firms.BasicFirm4
 * Pour tester les effets d'une adaptation intentionnelle, tournée vers la
 * réalisation de profits supérieurs.
 */

/**
 * An extension of {@link BasicFirm5} to test a profit oriented adaptation.
 */
public class BasicFirm6 extends BasicFirm5 {

	/**
	 * Creates a new firm.
	 * 
	 * @param sector
	 *            the sector.
	 * @param id
	 *            the identification number of the agent.
	 */
	public BasicFirm6(Sector sector, int id) {
		super(sector, id);
	}

	private void profitOrientedImitation() {
		// TODO 200 should be a parameter
		if (this.getPeriod() >200 && this.getPeriod() > this.lastImitiation + 12 && this.getRandom().nextFloat() > 0.9) {
			final BasicFirm6 model = (BasicFirm6) sector.select();
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

	@Override
	protected void planProduction() {
		this.mutation();
		this.checkSolvency();
		this.profitOrientedImitation();
		this.workforceManager.updateWage();
		this.updateCapacityUtilizationTarget();
		this.workforceManager.updateWorkforce();
	}

}
