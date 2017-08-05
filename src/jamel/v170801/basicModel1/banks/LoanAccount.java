package jamel.v170801.basicModel1.banks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a loan account.
 */
class LoanAccount {

	/**
	 * The associated deposit account.
	 */
	final private DepositAccount depositAccount;

	/**
	 * The list of the loans.
	 */
	final private List<Loan> list = new LinkedList<>();

	/**
	 * The total amount of the loans (overdue excepted).
	 */
	final private Amount loans = new Amount();

	/**
	 * The total amount of the loans at the bank level.
	 */
	final private Amount outstandingLoans;

	/**
	 * The amount of overdue debt.
	 */
	final private Amount overdueDebt = new Amount();

	/**
	 * Creates a new loan account.
	 * 
	 * @param depositAccount
	 *            the associated deposit account.
	 * @param outstandingLoans
	 *            the amount of loans at the bank level.
	 */
	LoanAccount(final DepositAccount depositAccount, final Amount outstandingLoans) {
		this.depositAccount = depositAccount;
		this.outstandingLoans = outstandingLoans;
	}

	/**
	 * Records a new overdue debt.
	 * 
	 * @param amount
	 *            the amount of the new overdue debt.
	 */
	private void newOverdueDebt(long amount) {
		if (amount <= 0) {
			throw new RuntimeException("Bad amount: " + amount);
		}
		this.overdueDebt.plus(amount);
		this.outstandingLoans.plus(amount);
		this.depositAccount.newDeposit(amount);
	}

	/**
	 * Adds a new loan to this account.
	 * 
	 * @param loan
	 *            the new loan to be added.
	 */
	void add(final Loan loan) {
		this.list.add(loan);
		this.loans.plus(loan.getPrincipal());
		this.outstandingLoans.plus(loan.getPrincipal());
		this.depositAccount.newDeposit(loan.getPrincipal());
	}

	/**
	 * Recovers due debts.
	 * 
	 * @param penaltyRate
	 *            the penalty rate on overdue loans.
	 */
	void debtRecovery(final double penaltyRate) {

		// TODO Vérifier qu'il n'est appelé qu'une fois par période

		// Paiement des intérêts sur la dette "overdue".

		{
			final long interest = (long) (penaltyRate * this.overdueDebt.getAmount());
			if (interest > this.depositAccount.getAmount()) {
				final long newLoan = interest - this.depositAccount.getAmount();
				this.newOverdueDebt(newLoan);
			}
			this.depositAccount.newWithdrawal(interest);
		}

		// Remboursement de la dette "overdue".

		{
			final long installment = Math.min(this.overdueDebt.getAmount(), this.depositAccount.getAmount());
			this.overdueDebt.minus(installment);
			this.outstandingLoans.minus(installment);
			this.depositAccount.newWithdrawal(installment);
		}

		// Puis on s'occupe des crédits ordinaires

		final Iterator<Loan> it = this.list.iterator();
		while (it.hasNext()) {
			final Loan loan = it.next();
			if (loan.getPrincipal() == 0) {
				throw new RuntimeException("This loan is empty.");
			}
			final long interest = loan.getInterest();
			final long installment = loan.getInstallment();
			if (interest + installment >= 0) {
				if (interest + installment > this.depositAccount.getAmount()) {
					this.newOverdueDebt(interest + installment - this.depositAccount.getAmount());
				}
				loan.cancel(installment);
				this.loans.minus(installment);
				this.outstandingLoans.minus(installment);
				this.depositAccount.newWithdrawal(installment + interest);
				if (loan.getPrincipal() == 0) {
					it.remove();
				}
			}
		}

	}

	/**
	 * Returns the amount of debts.
	 * 
	 * @return the amount of debts.
	 */
	long getAmount() {
		return this.loans.getAmount() + this.overdueDebt.getAmount();
	}

}
