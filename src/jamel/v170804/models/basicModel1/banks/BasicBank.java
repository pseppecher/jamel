package jamel.v170804.models.basicModel1.banks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Sector;
import jamel.v170804.data.AgentDataset;
import jamel.v170804.models.basicModel1.households.Shareholder;
import jamel.v170804.util.ArgChecks;

/**
 * A basic bank.
 */
public class BasicBank extends JamelObject implements Agent, Bank {

	/**
	 * Returns the specified action.
	 * 
	 * @param phaseName
	 *            the name of the action.
	 * @return the specified action.
	 */
	static public Consumer<? super Agent> getAction(final String phaseName) {

		final Consumer<? super Agent> action;

		switch (phaseName) {
		case "payDividends":
			action = (agent) -> {
				((BasicBank) agent).payDividends();
			};
			break;
		case "debtRecovery":
			action = (agent) -> {
				((BasicBank) agent).debtRecovery();
			};
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}

		return action;

	}

	/**
	 * The collection of accounts.
	 */
	private final List<BasicAccount> accounts = new ArrayList<>();

	/**
	 * The capital target ratio.
	 */
	final private double capitalTargetRatio;

	/**
	 * The dataset.
	 */
	final private AgentDataset dataset;

	/**
	 * The id of this bank.
	 */
	final private int id;

	/**
	 * The amount of outstanding deposits.
	 */
	private final Amount outstandindDeposits = new Amount();

	/**
	 * The amount of outstanding loans.
	 */
	private final Amount outstandingLoans = new Amount();

	/**
	 * The owners of the firm.
	 */
	private List<Shareholder> owners = new LinkedList<>();

	/**
	 * The penalty rate, ie the interest rate for overdue debts.
	 */
	private final double penaltyRate = 0.02;

	/**
	 * The interest rate.
	 */
	private final double rate = 0.01;

	/**
	 * The parent sector.
	 */
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
		final Parameters params = this.sector.getParameters();
		ArgChecks.nullNotPermitted(params, "params");
		this.capitalTargetRatio = params.getDoubleAttribute("initialMarkup");
		this.dataset = new AgentDataset(this);
	}

	/**
	 * For debugging purposes.
	 */
	@SuppressWarnings("unused")
	private void checkConsistency() {
		long sumDebts = 0;
		long sumDeposits = 0;
		for (BasicAccount account : this.accounts) {
			sumDebts += account.getDebt();
			sumDeposits += account.getAmount();
		}
		if (sumDebts != this.outstandingLoans.getAmount() || sumDeposits != this.outstandindDeposits.getAmount()) {
			Jamel.println();
			Jamel.println("Debts", sumDebts, this.outstandingLoans.getAmount());
			Jamel.println("Deposits", sumDeposits, this.outstandindDeposits.getAmount());
			Jamel.println();
			throw new RuntimeException("Inconsistency");
		}
	}

	/**
	 * Recovers due debts.
	 */
	private void debtRecovery() {
		// TODO Il serait bon que seuls les comptes entreprises soient passés en
		// revue ici...
		// checkConsistency();
		for (int i = 0; i < this.accounts.size(); i++) {
			this.accounts.get(i).debtRecovery(this.penaltyRate);
		}
		// checkConsistency();
	}

	/**
	 * Initializes the owners of this bank.
	 */
	private void initOwners() {
		final Agent[] selection = this.getSimulation().getSector("Shareholders").select(10);
		for (int i = 0; i < selection.length; i++) {
			if (selection[i] != null) {
				this.owners.add((Shareholder) selection[i]);
			}
		}
	}

	/**
	 * The dividend payment phase.
	 */
	private void payDividends() {
		checkOpen();
		if (this.owners.isEmpty()) {
			throw new RuntimeException("No owners.");
		}

		final long assets = this.outstandingLoans.getAmount();
		final long liabilities = this.outstandindDeposits.getAmount();
		final long capital = assets - liabilities;
		final long capitalTarget = (long) (assets * this.capitalTargetRatio);
		final long capitalExcess = Math.max(capital - capitalTarget, 0);
		if (capitalExcess > this.owners.size()) {
			final long newDividend = capitalExcess / this.owners.size();
			for (final Shareholder shareholder : this.owners) {
				shareholder.acceptDividendCheque(new BankCheque(this, shareholder, newDividend, this.getPeriod()));
			}
		}
	}

	/**
	 * Creates and returns a new deposit account.
	 * 
	 * @return a new deposit account.
	 */
	DepositAccount getNewDepositAccount() {
		return new DepositAccount(this.outstandindDeposits);
	}

	/**
	 * Creates and returns a new loan account.
	 * 
	 * @param deposit
	 *            a deposit account that will be linked with this loan account.
	 * 
	 * @return a new loan account.
	 */
	LoanAccount getNewLoanAccount(DepositAccount deposit) {
		return new LoanAccount(deposit, this.outstandingLoans);
	}

	/**
	 * Returns the penalty rate, ie the interest rate for overdue debts.
	 * 
	 * @return the penalty rate.
	 */
	double getPenaltyRate() {
		return this.penaltyRate;
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
	 * Closes this agent.
	 */
	@Override
	public void close() {
		this.dataset.put("countAgent", 1);
		this.dataset.put("assets", this.outstandingLoans.getAmount());
		this.dataset.put("liabilities", this.outstandindDeposits.getAmount());
		this.dataset.close();
		super.close();
	}

	@Override
	public Long getAssetTotalValue() {
		Jamel.notYetImplemented();
		return null;
	}

	@Override
	public int getBorrowerStatus() {
		Jamel.notYetImplemented();
		return 0;
	}

	@Override
	public Double getData(String dataKey, int period) {
		return this.dataset.getData(dataKey, period);
	}

	@Override
	public String getName() {
		return "Bank " + this.id;
	}

	@Override
	public Sector getSector() {
		return this.sector;
	}

	@Override
	public void goBankrupt() {
		Jamel.notYetImplemented();
	}

	@Override
	public boolean isBankrupted() {
		Jamel.notYetImplemented();
		return false;
	}

	@Override
	public boolean isSolvent() {
		Jamel.notYetImplemented();
		return false;
	}

	/**
	 * Opens this agent.
	 */
	@Override
	public void open() {
		if (this.owners.isEmpty()) {
			initOwners();
		}
		this.dataset.open();
		super.open();
	}

	@Override
	public Account openAccount(final AccountHolder accountHolder) {
		// TODO: il serait bon, selon la nature de AccountHolder, de délivrer
		// des comptes de nature différente.
		// Pour les ménages, un compte de dépôt simple.
		// pour les entreprises, un compte pouvant être débiteur.
		// Ils seraient classés dans deux listes différentes, ce qui permettrait
		// à la banque de ne passer en revue que les comptes débiteurs lors de
		// la phase de remboursement.
		final BasicAccount account = new BasicAccount(this, accountHolder);
		this.accounts.add(account);
		return account;
	}

}
