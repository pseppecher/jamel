package jamel.models.m18.r01.firms;

import jamel.util.Sector;

/**
 * 
 * 2018-03-13
 * jamel/models/m18/r01/firms/BasicFirm3.java
 * Extension de BasicFirm2.java
 * Apprend a fixer son taux d'utilisation normal selon le BSP. 
 * 
 * Extension de BasicFirm.java
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
public class BasicFirm3 extends BasicFirm2 {

	/**
	 * Creates a new basic firm.
	 * 
	 * @param name
	 *            the name.
	 * @param sector
	 *            the sector.
	 */
	public BasicFirm3(Sector sector, int id) {
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
				BasicFirm3 firm = (BasicFirm3) sector.select();
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
	 * 
	 * @param mut
	 *            the size of the mutation.
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
