package jamel.models.m18.r01.firms;

import jamel.models.util.Commodities;
import jamel.models.util.Supply;
import jamel.util.Sector;

/**
 * 
 * 2018-03-12
 * jamel/models/m18/r01/firms/BasicFirm2.java
 * Extension de BasicFirm.java
 * Utilise une fonction d'investissement à base de taux d'utilisation et de cash
 * flow.
 * 
 * 2018-03-10
 * jamel/models/m18/r01/firms/BasicFirm.java
 * 
 * 2018-03-05
 * jamel/models/m18/q06/firms/BasicFirm5.java
 * Une firme faite pour travailler avec BasicLaborMarket3 :
 * Elle connait son marché du travail, elle fait appel à lui quand elle veut
 * copier les salaires,
 * ce qui lui permet de copier les salaires d'une firme d'un autre secteur.
 * 
 * 2018-03-04
 * jamel/models/m18/q06/firms/BasicFirm4.java
 * On désactive la supervision des salaires.
 * But: comprendre la dynamique de la courbe de Beveridge.
 * 
 * 2018-03-02
 * jamel/models/m18/q05/firms/BasicFirm3.java
 * Modification du comportement de fixation des salaires.
 * On tente de supprimer le taux normal d'emplois vacants.
 * 
 * 2018-02-16
 * jamel/models/m18/q04/firms/BasicFirm2.java
 * On essaie de mettre les actionnaires à contribution après les faillites.
 * Introduction de titres de propriété.
 * 
 * 2018-02-09
 * jamel/models/m18/q03/firms/BasicFirm2.java
 * Copie de la firme précédente dans laquelle on va essayer d'insérer des
 * éléments de la firme du modèle icc.
 * 
 * 2018-02-09
 * jamel/models/m18/q03/firms/BasicFirm.java
 * Nouveau nom de la firme précédente.
 * 
 * 2018-02-02
 * jamel/models/m18/q02/firms/BasicFirm32.java
 * Ajout d'une mémoire des suppliers de biens capitaux.
 * 
 * 2018-01-27 jamel/models/m18/q01/firms/BasicFirm3.java
 * Mutation de BasicFirm.
 * On essaie les effets d'un markup évolutionniste.
 * 
 * 2018-01-19 jamel/models/m18/q01/firms/BasicFirm.java
 * 
 * 2017-11-08: refactoring
 * 
 * 2017-11-06: Mutation de BasicFirm5
 * Procède à la dépréciation dans la phase d'ouverture.
 * 
 * 2017-10-16: Mutation de BasicFirm3.
 * Pour permettre une mise sous tutelle pendant les première périodes.
 */
@SuppressWarnings("javadoc")
public class BasicFirm2 extends BasicFirm {

	protected float normalUtilizationRate = 0.85f;

	/**
	 * Creates a new basic firm.
	 * 
	 * @param name
	 *            the name.
	 * @param sector
	 *            the sector.
	 */
	public BasicFirm2(Sector sector, int id) {
		super(sector, id, new BasicFactory2(sector.getParameters().get("production"), sector.getSimulation()));
	}

	private long getBudget() {
		final double manpower = this.agentDataset.sum(keys.workforce, 12);
		final double machines = this.agentDataset.sum(keys.capacity, 12);
		final long budget;
		if (machines == 0) {
			throw new RuntimeException("Inconsistency");
		}
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

		return budget;
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

		final Commodities purchase = new BasicGoods(this.factory.getQualityOfInputForTheCreationOfANewMachine());
		final Supply[] supplies = getSupplies();

		if (supplies.length > 0) {

			final int capacity = this.factory.getCapacity();

			if ((this.getPeriod() < cons.supervision && capacity < this.cons.initialCapacity) || capacity == 0) {

				// Ici on doit renouveller exactement les machines
				// manquantes.
				// L'investissement est donc déterminé en termes réels

				final int targetMachines = (this.getPeriod() < cons.supervision && capacity < this.cons.initialCapacity)
						? this.cons.initialCapacity - capacity
						: 1;
				final int targetVolume = (int) (targetMachines * this.factory.getInputVolumeForANewMachine()
						- ((BasicFactory2) this.factory).getInputVolume());
				this.periodDataset.put(keys.targetVolume, targetVolume);
				// Maintenant il s'agit d'acheter ce volume, et de financer cet
				// achat.
				if (targetVolume > 0) {
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
				}
			} else {
				// ici on détermine le budget
				final long budget = getBudget();
				// puis on achète. Mais le financement ?
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

}
