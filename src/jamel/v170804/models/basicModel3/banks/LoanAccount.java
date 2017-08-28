package jamel.v170804.models.basicModel3.banks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jamel.util.ArgChecks;

/**
 * Represents a loan account.
 */
class LoanAccount {

	/**
	 * The associated deposit account.
	 */
	final private BasicAccount parentAccount;

	private long interestService = 0;

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

	private long principalService = 0;

	/**
	 * Creates a new loan account.
	 * 
	 * @param parentAccount.getDeposit()
	 *            the associated deposit account.
	 * @param outstandingLoans
	 *            the amount of loans at the bank level.
	 */
	LoanAccount(final BasicAccount parentAccount, final Amount outstandingLoans) {
		this.parentAccount = parentAccount;
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
		this.parentAccount.getDeposit().newDeposit(amount);
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
		this.parentAccount.getDeposit().newDeposit(loan.getPrincipal());
	}

	/**
	 * Recovers due debts.
	 * 
	 * @param penaltyRate
	 *            the penalty rate on overdue loans.
	 */
	void debtRecovery(final double penaltyRate) {

		if (this.principalService != 0 || this.interestService != 0) {
			throw new RuntimeException("Should be 0");
		}

		// Paiement des intérêts sur la dette "overdue".

		{
			final long interest = (long) (penaltyRate * this.overdueDebt.getAmount());
			if (interest > this.parentAccount.getDeposit().getAmount()) {
				final long newLoan = interest - this.parentAccount.getDeposit().getAmount();
				this.newOverdueDebt(newLoan);
			}
			this.parentAccount.getDeposit().newWithdrawal(interest);
			this.interestService += interest;
		}

		// Remboursement de la dette "overdue".

		{
			this.principalService += this.overdueDebt.getAmount();
			final long installment = Math.min(this.overdueDebt.getAmount(),
					this.parentAccount.getDeposit().getAmount());
			this.overdueDebt.minus(installment);
			this.outstandingLoans.minus(installment);
			this.parentAccount.getDeposit().newWithdrawal(installment);
		}

		// Puis on s'occupe des crédits ordinaires

		final Iterator<Loan> it = this.list.iterator();
		while (it.hasNext()) {
			final Loan loan = it.next();
			if (loan.getPrincipal() == 0) {
				throw new RuntimeException("This loan is empty.");
			}
			final long interest = loan.getInterest();
			this.interestService += interest;
			final long installment = loan.getInstallment();
			this.principalService += installment;
			if (interest + installment >= 0) {
				if (interest + installment > this.parentAccount.getDeposit().getAmount()) {
					this.newOverdueDebt(interest + installment - this.parentAccount.getDeposit().getAmount());
				}
				loan.cancel(installment);
				this.loans.minus(installment);
				this.outstandingLoans.minus(installment);
				this.parentAccount.getDeposit().newWithdrawal(installment + interest);
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

	void open() {
		this.interestService = 0;
		this.principalService = 0;
	}

	long getDebtService() {
		return this.interestService + this.principalService;
	}

	long getInterests() {
		return this.interestService;
	}

	boolean isSolvent() {
		return parentAccount.isSolvent();
	}

	void bankrupt() {
		this.parentAccount.bankrupt();
	}

	void cancel(final long excess) {
		// TODO to ça est bien mal fichu.
		ArgChecks.negativeOr0NotPermitted(excess, "excess");
		if (this.overdueDebt.getAmount() >= excess) {
			this.overdueDebt.minus(excess);
		} else {
			long remainder = excess;
			remainder -= this.overdueDebt.getAmount();
			this.overdueDebt.cancel();
			long cancellation = 0;
			final List<Loan> cancelled = new LinkedList<>();
			for (final Loan loan : list) {
				if (loan.getPrincipal() >= remainder) {
					loan.cancel(remainder);
					if (loan.getPrincipal()==0) {
						cancelled.add(loan);						
					}
					cancellation += remainder;
					remainder = 0;
					break;
				}
				cancellation += loan.getPrincipal();
				remainder -= loan.getPrincipal();
				loan.cancel(loan.getPrincipal());
				cancelled.add(loan);
			}
			this.loans.minus(cancellation);
			this.list.removeAll(cancelled);
			
			if (remainder > 0) {
				throw new RuntimeException("Remainder should be 0");
			}
		}
		this.outstandingLoans.minus(excess);
	}

}
