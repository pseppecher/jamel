package jamel.models.m18.r08.firms;

import java.util.function.Consumer;

import jamel.models.m18.r08.util.AbstractAgent;
import jamel.util.Agent;
import jamel.util.Sector;

/*
 * 2018-04-06
 * jamel/models/m18/r04/firms/AbstractFirm.java
 * Une firme abstraite, coquille vide destinée à faciliter les extensions de
 * firmes.
 * 
 * 2018-04-06
 * jamel/models/m18/r04/firms/BasicFirm4.java
 * Une firme qui limite (1) la distribution de dividendes (2) l'investissement
 * lorsqu'elle est en dessous de son objectif de fonds propres.
 * 
 * 2018-04-01
 * jamel/models/m18/r04/firms/BasicFirm3.java
 * Prend en compte l'overhead expense dans le calcul du cash flow (method
 * invest()).
 * 
 * 2018-03-30
 * jamel/models/m18/r04/firms/BasicFirm2.java
 * Une firme qui a besoin d'overheadlabour.
 * 
 * 2018-03-29
 * jamel/models/m18/r04/firms/BasicFirm.java
 * Fusion de BasicFirm et BasicFirm2.
 * C'est donc une firme qui intègre un comportement d'investissement
 * post-keynésien.
 * 
 * 2018-03-28
 * jamel/models/m18/r03/firms/BasicFirm.java
 * Intègre l'objet Technology, qui permet d'introduire un progrès techologique
 * basique.
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

/**
 * A basic firm.
 */
abstract class AbstractFirm extends AbstractAgent implements Firm {

	/**
	 * The data keys.
	 */
	protected static final FirmKeys keys = FirmKeys.getInstance();

	/**
	 * Returns the specified action.
	 * 
	 * @param phaseName
	 *            the name of the action.
	 * @return the specified action.
	 */
	static protected Consumer<? super Agent> getAction(final String phaseName) {

		final Consumer<? super Agent> action;

		switch (phaseName) {
		case "opening":
			action = (agent) -> {
				((AbstractFirm) agent).open();
			};
			break;
		case "pay_dividend":
		case "payDividends":
			action = (agent) -> {
				((AbstractFirm) agent).payDividend();
			};
			break;
		case "plan_production":
		case "planProduction":
			action = (agent) -> {
				((AbstractFirm) agent).planProduction();
			};
			break;
		case "payWages":
			action = (agent) -> {
				((AbstractFirm) agent).payWorkers();
			};
			break;
		case "production":
			action = (agent) -> {
				((AbstractFirm) agent).production();
			};
			break;
		case "closure":
			action = (agent) -> {
				((AbstractFirm) agent).close();
			};
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}

		return action;

	}

	/**
	 * Creates a new basic firm.
	 * 
	 * @param sector
	 *            the sector.
	 * @param id
	 *            the identification number of the agent.
	 */
	public AbstractFirm(Sector sector, int id) {
		super(sector, id);
	}

	/**
	 * Pays the dividend to the owners of the firm.
	 */
	abstract protected void payDividend();

	/**
	 * Pays the wages to the workers.
	 */
	abstract protected void payWorkers();

	/**
	 * Prepares the production.
	 */
	abstract protected void planProduction();

	/**
	 * Executes the production plan.
	 */
	abstract protected void production();

}
