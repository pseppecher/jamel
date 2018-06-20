package jamel.models.m18.r08.roles;

import jamel.models.m18.r08.util.AccountHolder;
import jamel.models.m18.r08.util.Cheque;
import jamel.models.m18.r08.util.Equity;

/**
 * A shareholder is an agent that can receives dividends.
 * 
 * 2018-02-16: m18.q04.util.Shareholder.
 * Un nouveau shareholder qui reçoit des titres de propriété.
 */
public interface Shareholder extends AccountHolder {

	/**
	 * Receives a dividend cheque.
	 * 
	 * @param cheque
	 *            the dividend cheque.
	 */
	void acceptDividendCheque(Cheque cheque);

	/**
	 * Receives an ownership title.
	 * 
	 * @param title
	 *            the title.
	 */
	void acceptTitle(Equity title);

	/**
	 * TODO rename translate / quand le shareholder est invité à participer à la
	 * recapitalisation d'une entreprise.
	 * 
	 * @param payee
	 *            l'agent qui recevra le cheque de contribution (normalement,
	 *            l'entreprise à recapitaliser).
	 * 
	 * @param contribution
	 *            la contribution demandée.
	 * @return le chèque correspondant (null si le share holder décline
	 *         l'invitation).
	 */
	Cheque contribute(AccountHolder payee, long contribution);

}
