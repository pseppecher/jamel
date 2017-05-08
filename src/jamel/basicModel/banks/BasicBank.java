package jamel.basicModel.banks;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import jamel.util.AgentDataset;
import jamel.util.JamelObject;
import jamel.util.Sector;

/**
 * A basic bank.
 */
public class BasicBank extends JamelObject implements Bank {

	/**
	 * Returns the specified phase method.
	 * 
	 * @param phase
	 *            the name of the phase.
	 * @return the method that should be called by the specified phase.
	 */
	static public Method getPhaseMethod(final String phase) {
		final Method result;

		try {
			switch (phase) {
			case "opening":
				result = BasicBank.class.getMethod("open");
				break;
			case "debtRecovery":
				result = BasicBank.class.getMethod("debtRecovery");
				break;
			case "closure":
				result = BasicBank.class.getMethod("close");
				break;
			default:
				result = null;

			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Something went wrong while creating the method for this phase: " + phase, e);
		}
		return result;
	}

	/**
	 * The collection of accounts.
	 */
	private final Map<AccountHolder, BasicAccount> accounts = new LinkedHashMap<>();

	/**
	 * The sum of all outstanding credits.
	 */
	private long credits = 0;

	/**
	 * The dataset.
	 */
	final private AgentDataset dataset;

	/**
	 * The sum of all deposits.
	 */
	private long deposits = 0;

	/**
	 * The id of this agent.
	 */
	final private int id;

	/**
	 * The interests paid during the period.
	 */
	private long interests;

	/**
	 * The penalty rate, ie the interest rate for overdue debts.
	 */
	private double penaltyRate;

	/**
	 * The interest rate.
	 */
	private double rate = 0;

	/**
	 * The parent sector.
	 */
	@SuppressWarnings("unused")
	final private Sector sector;

	/**
	 * Creates a new basic agent.
	 * 
	 * @param sector
	 *            the parent sector.
	 * @param id
	 *            the id of the agent.
	 */
	public BasicBank(final Sector sector, final int id) {
		super(sector.getSimulation());
		this.sector = sector;
		this.id = id;
		this.dataset = new AgentDataset(this);
	}

	/**
	 * Receives the notification of a debt repayment.
	 * 
	 * @param amount
	 *            the amount of the repayment.
	 */
	void debtRepayment(long amount) {
		this.credits -= amount;
		this.deposits -= amount;
	}

	/**
	 * Returns the nominal interest rate.
	 * 
	 * @return the nominal interest rate.
	 */
	double getRate() {
		return this.rate;
	}

	/**
	 * Receives the notification of an interest payment.
	 * 
	 * @param interest
	 *            the interest paid.
	 */
	void newInterest(long interest) {
		this.deposits -= interest;
		this.interests += interest;
	}

	/**
	 * Receives the notification of a new loan.
	 * 
	 * @param amount
	 *            the amount of the credit.
	 */
	void newLoan(long amount) {
		this.credits += amount;
		this.deposits += amount;
	}

	/**
	 * Transfer the specified amount of money from the payer account to the
	 * payee account.
	 * 
	 * @param payerAccount
	 *            the account of the payer.
	 * @param payee
	 *            the payee.
	 * @param amount
	 *            the amount to be transfered.
	 * @param reason
	 *            the reason of the transfer.
	 */
	void transfer(final BasicAccount payerAccount, final AccountHolder payee, final long amount, final String reason) {
		payerAccount.debit(amount);
		this.accounts.get(payee).credit(amount, payee, reason);
	}

	/**
	 * Closes this agent.
	 */
	public void close() {
		this.dataset.put("countAgent", 1);
		this.dataset.put("interests", this.interests);
		this.dataset.put("deposits", this.deposits);
		this.dataset.put("credits", this.credits);
	}

	/**
	 * Recovers due debts.
	 */
	public void debtRecovery() {
		for (final BasicAccount account : this.accounts.values()) {
			account.payBack();
		}
	}

	@Override
	public Double getData(String dataKey, String period) {
		return this.dataset.getData(dataKey);
	}

	@Override
	public String getName() {
		return "Bank " + this.id;
	}

	/**
	 * Returns the penalty rate, ie the interest rate for overdue debts.
	 * 
	 * @return the penalty rate.
	 */
	public double getPenaltyRate() {
		return this.penaltyRate;
	}

	/**
	 * Opens this agent.
	 */
	public void open() {
		this.interests = 0;
	}

	@Override
	public Account openAccount(final AccountHolder accountHolder) {
		if (this.accounts.containsKey(accountHolder)) {
			throw new RuntimeException("This account already exists.");
		}
		final BasicAccount account = new BasicAccount(this, accountHolder);
		this.accounts.put(accountHolder, account);
		return account;
	}

}
