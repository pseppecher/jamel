package jamel.models.m18.r02.firms;

import jamel.models.util.Commodities;
import jamel.models.util.Supply;
import jamel.util.Sector;

/* 
 * 2018-03-12
 * jamel/models/m18/r01/firms/BasicFirm2.java
 * Extension de BasicFirm.java
 * Utilise une fonction d'investissement à base de taux d'utilisation et de cash
 * flow. 
 */

/**
 * A firm with a post-Keynesian investment behavior.
 */ 
public class BasicFirm2 extends BasicFirm {

	/**
	 * The normal rate of utilization of capacities TODO should be a parameter.
	 */
	protected float normalUtilizationRate = 0.85f;

	/**
	 * Creates a new basic firm.
	 * 
	 * @param sector
	 *            the sector.
	 * @param id
	 *            the identification number of the agent.
	 */
	public BasicFirm2(Sector sector, int id) {
		super(sector, id, new BasicFactory2(sector.getParameters().get("production"), sector.getSimulation()));
	}

	/**
	 * Updates the data.
	 */
	@Override
	protected void updateData() {
		super.updateData();
		this.periodDataset.put(keys.normalUtilizationRate, this.normalUtilizationRate);
	}

	@Override
	public void open() {
		super.open();
		if (this.factory.getCapacity() == 0) {
			this.imitation();
		}
	}

	@Override
	public void invest() {

		/*
		 * 2018-03-10 -> public
		 * Permet au marché des biens d'investissement d'appeler cette methode.
		 * Permet de brasser les firmes de différents secteurs dans la phase d'investissement.
		 */

		final Commodities purchase;
		final Supply[] supplies = getSupplies();

		if (supplies.length > 0) {

			final int capacity = this.factory.getCapacity();

			if (this.getPeriod() < cons.supervision) {
				if (capacity < this.cons.initialCapacity) {
					purchase = buy(this.cons.initialCapacity - capacity, supplies);
				} else {
					purchase = new BasicGoods(this.factory.getQualityOfInputForTheCreationOfANewMachine());
				}
			} else {
				if (capacity == 0) {
					purchase = buy(1, supplies);
				} else {

					purchase = new BasicGoods(this.factory.getQualityOfInputForTheCreationOfANewMachine());

					// on détermine le budget

					final double manpower = this.agentDataset.sum(keys.workforce, 12);
					final double machines = this.agentDataset.sum(keys.capacity, 12);
					final long budget;
					if (machines == 0) {
						budget = 0;
					} else {
						final float utilizationRate = (float) (manpower / machines);
						if (utilizationRate > normalUtilizationRate) {
							final double sales = this.agentDataset.sum(keys.salesValue, 12);
							final double costs = this.agentDataset.sum(keys.salesCosts, 12);
							final double interests = this.agentDataset.sum(keys.interests, 12);
							final double cashFlow = sales - (costs + interests);
							if (cashFlow > 0) {
								budget = (long) ((utilizationRate - normalUtilizationRate) * cashFlow);
							} else {
								budget = 0;
							}
						} else {
							budget = 0;
						}
					}

					// puis on achète.

					if (budget > 0) {
						for (Supply supply : supplies) {
							if (budget - purchase.getValue() < supply.getPrice()) {
								break;
							}
							final long value;
							final long volume;
							if (supply.getValue() < budget - purchase.getValue()) {
								volume = supply.getVolume();
								value = supply.getValue();
							} else {
								volume = (long) ((budget - purchase.getValue()) / supply.getPrice());
								value = supply.getPrice(volume);
							}
							if (this.account.getAmount() < value) {
								this.account.borrow(value - this.account.getAmount(), 0, false);
							}
							purchase.add(supply.purchase(volume, account.issueCheque(supply.getSupplier(), value)));
						}
					}
				}
			}

		} else {
			purchase = new BasicGoods(this.factory.getQualityOfInputForTheCreationOfANewMachine());
		}

		if (purchase.getVolume() > 0) {

			// C'est le moment de rectifier le financement

			final long purchaseValue = purchase.getValue();
			final long longTerm = (long) (purchaseValue * this.targetDebtRatio);
			final long shortTerm = purchaseValue - longTerm;

			if (longTerm > 0) {
				this.account.borrow(longTerm, cons.longTerm, true);
			}
			if (shortTerm > 0) {
				this.account.borrow(shortTerm, cons.shortTerm, true);
			}

			// TODO IMPLEMENT this.periodDataset.put(keys.investmentSize, 0);
			this.periodDataset.put(keys.investmentVolume, purchase.getVolume());
			this.periodDataset.put(keys.investmentValue, purchase.getValue());

			// On envoie le stuff à la factory.

			((BasicFactory2) this.factory).expandCapacity(purchase);
		} else {
			this.periodDataset.put(keys.investmentVolume, 0);
			this.periodDataset.put(keys.investmentValue, 0);
		}

		this.periodDataset.put(keys.inputVolume, ((BasicFactory2) this.factory).getInputVolume());
		// TODO : vérifier ici que purchase est bien vide

	}

	/**
	 * Buys the investment goods required for the creation of specified number
	 * of machines.
	 * 
	 * @param targetMachines
	 *            the number of machines to be created.
	 * @param supplies
	 *            the suplies of investment goods.
	 * @return the goods purchased.
	 */
	protected Commodities buy(final int targetMachines, Supply[] supplies) {

		if (targetMachines == 0) {
			throw new IllegalArgumentException();
		}

		final Commodities purchase = new BasicGoods(this.factory.getQualityOfInputForTheCreationOfANewMachine());

		// Ici on doit renouveller exactement les machines
		// manquantes.
		// L'investissement est donc déterminé en termes réels

		final int targetVolume = (int) (targetMachines * this.factory.getInputVolumeForANewMachine()
				- ((BasicFactory2) this.factory).getInputVolume());
		this.periodDataset.put(keys.targetVolume, targetVolume);
		// Maintenant il s'agit d'acheter ce volume, et de financer cet
		// achat.

		for (Supply supply : supplies) {
			final long value;
			final long volume;
			if (supply.getVolume() < targetVolume - purchase.getVolume()) {
				volume = supply.getVolume();
				value = supply.getValue();
			} else {
				volume = targetVolume - purchase.getVolume();
				value = supply.getPrice(volume);
			}
			if (this.account.getAmount() < value) {
				this.account.borrow(value - this.account.getAmount(), 0, false);
			}
			purchase.add(supply.purchase(volume, account.issueCheque(supply.getSupplier(), value)));
			if (purchase.getVolume() == targetVolume) {
				break;
			}
			if (purchase.getVolume() > targetVolume) {
				throw new RuntimeException("Inconsistency");
			}
		}

		return purchase;

	}

}
