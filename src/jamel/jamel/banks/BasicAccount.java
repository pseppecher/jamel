package jamel.jamel.banks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jamel.util.Agent;
import jamel.util.JamelObject;

/**
 * A basic account for the basic bank.
 */
class BasicAccount extends JamelObject implements Account {
	
	/*
	 * TODO
	 * Il manque un traitement statistique des opérations du compte, un recueil des données.
	 * Il faut une ouverture du compte, avec remise à 0 des compteurs,
	 * Il faut une fermeture du compte, avec compilation des compteurs dans un dataset à disposition du holder.
	 */

	/**
	 * The bank.
	 */
	private final BasicBank bank;

	/**
	 * The deposit.
	 */
	private final Amount deposit = new Amount();

	/**
	 * The account holder.
	 */
	final private AccountHolder holder;

	/**
	 * The outstanding loans.
	 */
	private final List<Loan> loans = new LinkedList<>();

	/**
	 * The overdue loans.
	 */
	private final Amount overdueLoans = new Amount();

	/**
	 * Creates a basic account.
	 * 
	 * @param bank
	 *            the bank
	 * @param accountHolder
	 *            the account holder
	 * 
	 */
	public BasicAccount(final BasicBank bank, final AccountHolder accountHolder) {
		super(bank.getSimulation());
		this.bank = bank;
		this.holder = accountHolder;
	}

	/**
	 * Creates a new "overdue" loan.
	 * 
	 * @param amount
	 *            the amount of overdue debt.
	 */
	private void newOverdueLoan(long amount) {
		if (amount <= 0) {
			throw new RuntimeException("Bad amount: " + amount);
		}
		this.overdueLoans.plus(amount);
		this.deposit.plus(amount);
		this.bank.newLoan(amount);
	}

	/**
	 * Adds the specified amount to this account.
	 * 
	 * @param credit
	 *            the amount to be added.
	 * @param payer
	 *            the payer or the creditor.
	 * @param reason
	 *            the reason of the credit.
	 */
	void credit(final long credit, final Agent payer, final String reason) {
		this.deposit.plus(credit);
		this.holder.creditNotification(credit, payer, reason);
	}

	/**
	 * Removes the specified amount from this account.
	 * 
	 * @param debit
	 *            the amount to be removed.
	 */
	void debit(long debit) {
		this.deposit.minus(debit);
	}

	/**
	 * Pays back the debt.
	 */
	void payBack() {

		// Ces opérations devraient comptabilisées au niveau du compte pour
		// informer le holder

		// Paiement des intérêts sur la dette "overdue".

		{
			final long interest = (long) (this.bank.getPenaltyRate() * this.overdueLoans.getAmount());
			if (interest > this.deposit.getAmount()) {
				final long newLoan = interest - this.deposit.getAmount();
				this.overdueLoans.plus(newLoan);
				this.deposit.plus(newLoan);
				this.bank.newLoan(newLoan);
			}
			this.deposit.minus(interest);
			this.bank.newInterest(interest);

		}

		// Remboursement de la dette "overdue".

		{
			final long installment = Math.min(this.overdueLoans.getAmount(), this.deposit.getAmount());
			this.deposit.minus(installment);
			this.overdueLoans.minus(installment);
			this.bank.debtRepayment(installment);
		}

		// Puis on s'occupe des crédits ordinaires

		final Iterator<Loan> it = this.loans.iterator();
		while (it.hasNext()) {
			final Loan loan = it.next();
			if (loan.getPrincipal() == 0) {
				throw new RuntimeException("This loan is empty.");
			}
			final long interest = loan.getInterest();
			final long installment = loan.getInstallment();
			if (interest + installment >= 0) {
				if (interest + installment > this.deposit.getAmount()) {
					this.newOverdueLoan(interest + installment - this.deposit.getAmount());
				}
				this.deposit.minus(interest + installment);
				loan.cancel(installment);
				this.bank.newInterest(interest);
				this.bank.debtRepayment(installment);
				if (loan.getPrincipal() == 0) {
					it.remove();
				}
			}
		}

	}

	@Override
	public void borrow(long amount, int term, boolean amortizing) {
		if (amount <= 0) {
			throw new RuntimeException("Bad amount: " + amount);
		}
		if (term <= 0) {
			throw new RuntimeException("Bad term: " + amount);
		}
		final Loan loan = new BasicLoan(this, amount, this.bank.getRate(), this.getPeriod() + term, amortizing);
		this.loans.add(loan);
		this.bank.newLoan(amount);
	}

	@Override
	public void transfer(final long amount, final AccountHolder payee, final String reason) {
		this.bank.transfer(this, payee, amount, reason);
	}

}
