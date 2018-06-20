/*
 * 2018-04-14: jamel.models.m18.r08
 * Remettre de l'ordre dans le déroulement d'une séquence. 
 * 
 * 2018-04-10: jamel/models/m18/r07/
 * On essaie d'introduire une recapitalisation des firmes après effacement des dettes.
 * Cela suppose de modifier la propriété des firmes.
 * 
 * 2018-04-10: jamel/models/m18/r06/
 * Modèle avec une firme qui :
 * - assure une meilleure gestion des stocks (lisse l'offre de marchandises),
 * - ajustement des salaires en référence au salaire moyen d'un échantillon,
 * - dépréciation des stocks en cas de faillite,
 * - ajuste les dividendes versés en fonction du niveau d'endettement.
 * Le travailleur lui aussi base son salaire de réservation en référence au
 * salaire moyen d'un échantillon.
 * 
 * On va essayer une procédure de renflouement des entreprises, pour
 * reconstituer leur fonds propres après une faillite.
 * 
 * 
 * 2018-03-29: jamel/models/m18/r04/
 * On réorganise : on abandonne la firme avec NPV pour privilégier la firme qui
 * investit sur la base de son cash flow.
 * La banque utilise une fonction de changement des taux plus réaliste.
 * 
 * 2018-03-28: jamel/models/m18/r03/
 * 
 * Modèles avec progrès technologique.
 * Le progrès est modélisé à l'aide d'un secteur d'un nouveau type, sensé
 * représenter la technologie courante.
 * 
 * 2018-03-17: jamel.models.m18.r02
 * 
 * 2018-03-10: jamel.models.m18.r01
 * From jamel.models.m18.q06
 */

/**
 * Model 2018, r07: 2 sectors.
 */
package jamel.models.m18.r08;