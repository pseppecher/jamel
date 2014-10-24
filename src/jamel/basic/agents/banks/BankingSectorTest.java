package jamel.basic.agents.banks;

import static org.junit.Assert.*;

import java.util.Random;

import jamel.basic.agents.roles.AccountHolder;
import jamel.basic.agents.roles.Asset;
import jamel.basic.agents.roles.CapitalOwner;
import jamel.basic.data.AgentDataset;
import jamel.basic.util.BankAccount;
import jamel.basic.util.BasicPeriod;
import jamel.basic.util.Cheque;
import jamel.basic.util.JamelParameters;
import jamel.util.Circuit;
import jamel.util.Period;
import jamel.util.Timer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A class to test the BankingSector2.
 */
@SuppressWarnings("javadoc")
public class BankingSectorTest {
	
	/**
	 * A pseudo-circuit.
	 */
	private class TCircuit extends Circuit {

		/** The parameters of the simulation. */
		private final JamelParameters jamelParameters = new JamelParameters();

		public CapitalOwner defaultCapitalOwner;

		private TCircuit() {
			super(new Timer() {
				@Override public Period getPeriod() {return new BasicPeriod(t);}
				@Override public void next() {}
			}, new Random(0));
			this.jamelParameters.put("Bank.rate.normal", "0.05");
			this.jamelParameters.put("Bank.rate.penalty", "0.1");
			this.jamelParameters.put("Bank.term.normal", "12");
			this.jamelParameters.put("Bank.term.extended", "24");
			this.jamelParameters.put("Bank.capital.targetedRatio", "0.1");
			this.jamelParameters.put("Bank.capital.propensityToDistributeExcess", "1");
		}

		@Override
		public Object forward(String message, Object... args) {
			final Object result;
			if ("selectCapitalOwner".equals(message)) {
				result = this.defaultCapitalOwner;
			}
			else {
				throw new IllegalArgumentException("Unknown message: "+message);
			}
			return result;
		}

		@Override
		public String getParameter(String... keys) {
			return this.jamelParameters.get(keys);
		}

		@Override
		public String[] getParameterArray(String... keys) {
			return null;
		}

		@Override
		public String[] getStartingWith(String string) {
			return null;
		}

		@Override
		public boolean isPaused() {
			return false;
		}

		@Override
		public void run() {
			// Does nothing.
		}

	}

	/**
	 * returns a new account holder.
	 * @return an AccountHolder.
	 */
	private static AccountHolder newAccountHolder() {
		return new AccountHolder(){
			@Override public void bankrupt() {}
			@Override public long getAssets() {return 0;}
			@Override public AgentDataset getData() {return null;}
			@Override public String getName(){return "Basic Account Holder";}
			@Override public boolean isBankrupted() {return false;}
			@Override public void updateParameters() {}
		};
	}

	private BankingSector bank;

	private long bankAssets;

	private long bankCapital;

	private boolean bankConsistency;

	private long bankLiabilities;

	private TCircuit circuit;

	private int t=0;

	private void updateAccounting() {
		this.bankLiabilities = (Long) bank.forward("getLiabilities");
		this.bankAssets = (Long) bank.forward("getAssets");
		this.bankCapital = (Long) bank.forward("getCapital");
		this.bankConsistency = (Boolean) bank.forward("isConsistent");
	}

	@Before
	public void setUp() throws Exception {
		this.t=0;
		this.circuit = new TCircuit();
		this.bank=new BankingSector("Bank", this.circuit);
	}

	@After
	public void tearDown() throws Exception {
		this.circuit=null;
		this.bank=null;
	}

	/**
	 * Tests the creation of a new account.
	 */
	@Test
	public void test_01_getNewAccount() {

		final BankAccount account1 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		assertEquals("Bad amount:",0,account1.getAmount());
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 0, account1.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 0, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

	/**
	 * Tests lending.
	 */
	@Test
	public void test_02_lend() {

		final BankAccount account1 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 0, account1.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 0, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		account1.lend(100);
		this.updateAccounting();
		assertEquals("Account amount:", 100, account1.getAmount());
		assertEquals("Account debt:", 100, account1.getDebt());
		assertEquals("Bank liabilities:", 100, this.bankLiabilities);
		assertEquals("Bank assets:", 100, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

	/**
	 * Tests the payment by check: rejection of a bounced check.
	 */
	@Test
	public void test_040_PaymentByCheque() {

		final BankAccount account1 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		final BankAccount account2 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 0, account1.getDebt());
		assertEquals("Account amount:", 0, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 0, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		account1.lend(1000);
		this.updateAccounting();
		assertEquals("Account amount:", 1000, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Account amount:", 0, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);		
		assertEquals("Bank capital:", 0, this.bankCapital);

		final Cheque cheque = account1.newCheque(1000);
		account2.deposit(cheque);
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Account amount:", 1000, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);		
		assertEquals("Bank capital:", 0, this.bankCapital);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

	/**
	 * Tests the payment by check: rejection of a bounced check.
	 */
	@Test
	public void test_041_PaymentByCheque() {

		final BankAccount account1 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		final BankAccount account2 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 0, account1.getDebt());
		assertEquals("Account amount:", 0, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 0, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		account1.lend(1000);
		this.updateAccounting();
		assertEquals("Account amount:", 1000, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Account amount:", 0, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);		
		assertEquals("Bank capital:", 0, this.bankCapital);

		final Cheque cheque = account1.newCheque(1010);
		try {
			account2.deposit(cheque);
			fail("This check should be rejected.");
		} catch (Exception e) {
		}
		this.updateAccounting();
		assertEquals("Account amount:", 1000, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Account amount:", 0, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

	/**
	 * Tests the payment by check: rejection of bounced check.
	 */
	@Test
	public void test_042_PaymentByCheque() {

		final BankAccount account1 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		final BankAccount account2 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 0, account1.getDebt());
		assertEquals("Account amount:", 0, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 0, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		final Cheque check1 = account1.newCheque(1000); // Creates a bounced check;
		try {
			account2.deposit(check1); // Tries to deposit the check.
			fail("This check should be rejected.");
		} catch (RuntimeException e) {}
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 0, account1.getDebt());
		assertEquals("Account amount:", 0, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 0, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		account1.lend(1000); // Creates enough money.
		this.updateAccounting();
		assertEquals("Account amount:", 1000, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Account amount:", 0, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		account2.deposit(check1); // Deposits the check.
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Account amount:", 1000, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

	/**
	 * Tests the payment by check: rejection of a cancelled check.
	 */
	@Test
	public void test_043_PaymentByCheque() {

		final BankAccount account1 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		final BankAccount account2 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 0, account1.getDebt());
		assertEquals("Account amount:", 0, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 0, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		account1.lend(5000); // Creates a lot of stacks.
		this.updateAccounting();
		assertEquals("Account amount:", 5000, account1.getAmount());
		assertEquals("Account debt:", 5000, account1.getDebt());
		assertEquals("Account amount:", 0, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 5000, this.bankLiabilities);
		assertEquals("Bank assets:", 5000, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		final Cheque cheque = account1.newCheque(1000); // Creates a check.
		account2.deposit(cheque); // Deposits the check.
		this.updateAccounting();
		assertEquals("Account amount:", 4000, account1.getAmount());
		assertEquals("Account debt:", 5000, account1.getDebt());
		assertEquals("Account amount:", 1000, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 5000, this.bankLiabilities);
		assertEquals("Bank assets:", 5000, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		try {
			account2.deposit(cheque); // Try to deposits the check once more.
			fail("A check cannot be deposit twice.");
		} catch (Exception e) {
		}
		this.updateAccounting();
		assertEquals("Account amount:", 4000, account1.getAmount());
		assertEquals("Account debt:", 5000, account1.getDebt());
		assertEquals("Account amount:", 1000, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 5000, this.bankLiabilities);
		assertEquals("Bank assets:", 5000, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

	/**
	 * Tests interest payment.
	 */
	@Test
	public void test_050_interestPayment() {

		final BankAccount bankAccount = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		bankAccount.lend(1000);
		this.updateAccounting();
		assertEquals("Account amount:", 1000, bankAccount.getAmount());
		assertEquals("Account debt:", 1000, bankAccount.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);

		this.t++;// Next period.
		this.bank.doPhase("debt_recovery");
		this.updateAccounting();
		assertEquals("Account amount:", 950, bankAccount.getAmount());
		assertEquals("Account debt:", 1000, bankAccount.getDebt());
		assertEquals("Bank liabilities:", 950, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);

		this.t++;// Next period.
		this.bank.doPhase("debt_recovery");
		this.updateAccounting();
		assertEquals("Account amount:", 900, bankAccount.getAmount());
		assertEquals("Account debt:", 1000, bankAccount.getDebt());
		assertEquals("Bank liabilities:", 900, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

	/**
	 * Tests interest payment.
	 */
	@Test
	public void test_051_interestPayment() {
		
		this.circuit.jamelParameters.put("Bank.rate.normal", "0.01");
		this.bank.forward("updateParameters");

		final BankAccount bankAccount = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		bankAccount.lend(1000);
		this.updateAccounting();
		assertEquals("Account amount:", 1000, bankAccount.getAmount());
		assertEquals("Account debt:", 1000, bankAccount.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		this.t++;// Next period.
		this.bank.doPhase("debt_recovery");
		this.updateAccounting();
		assertEquals("Account amount:", 991, bankAccount.getAmount());
		assertEquals("Account debt:", 1000, bankAccount.getDebt());
		assertEquals("Bank liabilities:", 991, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);
		assertEquals("Bank capital:", 9, this.bankCapital);

		this.t++;// Next period.
		this.bank.doPhase("debt_recovery");
		this.updateAccounting();
		assertEquals("Account amount:", 982, bankAccount.getAmount());
		assertEquals("Account debt:", 1000, bankAccount.getDebt());
		assertEquals("Bank liabilities:", 982, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);
		assertEquals("Bank capital:", 18, this.bankCapital);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

	/**
	 * Tests interest payment, in the case of non-sufficient funds.
	 */
	@Test
	public void test_052_interestPayment() {

		final BankAccount account1 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		final BankAccount account2 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 0, account1.getDebt());
		assertEquals("Account amount:", 0, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 0, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		account1.lend(1000); // Lend 1000.
		account2.deposit(account1.newCheque(1000)); // Transfer 1000 to the account2.
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Account amount:", 1000, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		this.t++; // Next period.
		this.bank.doPhase("debt_recovery");
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 1050, account1.getDebt());
		assertEquals("Account amount:", 1000, account2.getAmount());
		assertEquals("Account debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1050, this.bankAssets);
		assertEquals("Bank capital:", 50, this.bankCapital);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

	/**
	 * Tests the loans payback.
	 */
	@Test
	public void test_06_PayBack() {

		final BankAccount account1 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 0, account1.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 0, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		account1.lend(1000);
		this.updateAccounting();
		assertEquals("Account amount:", 1000, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		this.bank.doPhase("debt_recovery");
		this.updateAccounting();
		assertEquals("Account amount:", 950, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Bank liabilities:", 950, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);
		assertEquals("Bank capital:", 50, this.bankCapital);

		t++;
		this.bank.doPhase("debt_recovery");
		this.updateAccounting();
		assertEquals("Account amount:", 900, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Bank liabilities:", 900, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);
		assertEquals("Bank capital:", 100, this.bankCapital);

		t=12;
		this.bank.doPhase("debt_recovery");
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 150, account1.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 150, this.bankAssets);
		assertEquals("Bank capital:", 150, this.bankCapital);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

	/**
	 * Test bankruptcy.
	 */
	@Test
	public void test_07_Bankruptcy() {

		final BankAccount account1 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 0, account1.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 0, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		account1.lend(1000);
		this.updateAccounting();
		assertEquals("Account amount:", 1000, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Bank liabilities:", 1000, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		for (t=0;t<12;t++) {
			this.bank.doPhase("debt_recovery");
		}
		this.updateAccounting();
		assertEquals("Period:", 11, t);
		assertEquals("Account amount:", 400, account1.getAmount());
		assertEquals("Account debt:", 1000, account1.getDebt());
		assertEquals("Bank liabilities:", 400, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);	
		assertEquals("Bank capital:", 600, this.bankCapital);

		t=12;
		this.bank.doPhase("debt_recovery");
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 650, account1.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 650, this.bankAssets);
		assertEquals("Bank capital:", 650, this.bankCapital);

		for (int t=13;t<=23;t++) {
			this.bank.doPhase("debt_recovery");
		}
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 1846, account1.getDebt());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 1846, this.bankAssets);
		assertEquals("Bank capital:", 1846, this.bankCapital);

		t=24;
		this.bank.doPhase("debt_recovery");
		this.updateAccounting();
		assertEquals("Account amount:", 0, account1.getAmount());
		assertEquals("Account debt:", 0, account1.getDebt());
		assertTrue("Account is not closed", !account1.isOpen());
		assertEquals("Bank liabilities:", 0, this.bankLiabilities);
		assertEquals("Bank assets:", 0, this.bankAssets);
		assertEquals("Bank capital:", 0, this.bankCapital);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

	/**
	 * Test dividend_payment.
	 */
	@Test
	public void test_08_Dividend() {

		final BankAccount account1 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		final BankAccount account2 = (BankAccount) this.bank.forward("getNewAccount", newAccountHolder());
		
		circuit.defaultCapitalOwner = new CapitalOwner() {
			@Override public void addAsset(Asset asset) {}
			@Override public AgentDataset getData() {return null;}
			@Override public String getName() {return "Basic Capital Owner";}
			@Override public void receiveDividend(Cheque cheque,Asset asset) {account2.deposit(cheque);}
			@Override public void removeAsset(Asset asset) {}
			@Override public void updateParameters() {}
		};

		account1.lend(1000);
		for (int t=0;t<12;t++) {
			this.bank.doPhase("debt_recovery");
		}
		this.updateAccounting();
		assertEquals("Account 1 amount:", 400, account1.getAmount());
		assertEquals("Account 1 debt:", 1000, account1.getDebt());
		assertEquals("Account 2 amount:", 0, account2.getAmount());
		assertEquals("Account 2 debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 400, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);	
		assertEquals("Bank capital:", 600, this.bankCapital);

		this.bank.doPhase("pay_dividend");
		this.updateAccounting();
		assertEquals("Account 1 amount:", 400, account1.getAmount());
		assertEquals("Account 1 debt:", 1000, account1.getDebt());
		assertEquals("Account 2 amount:", 500, account2.getAmount());
		assertEquals("Account 2 debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 900, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);	
		assertEquals("Bank capital:", 100, this.bankCapital);

		this.bank.doPhase("pay_dividend");
		this.updateAccounting();
		assertEquals("Account 1 amount:", 400, account1.getAmount());
		assertEquals("Account 1 debt:", 1000, account1.getDebt());
		assertEquals("Account 2 amount:", 500, account2.getAmount());
		assertEquals("Account 2 debt:", 0, account2.getDebt());
		assertEquals("Bank liabilities:", 900, this.bankLiabilities);
		assertEquals("Bank assets:", 1000, this.bankAssets);	
		assertEquals("Bank capital:", 100, this.bankCapital);

		assertTrue("Bank accounting is not consistent", this.bankConsistency);

	}

}

// ***
