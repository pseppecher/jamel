package jamel.jamel.capital;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import jamel.basic.util.BasicTimer;
import jamel.jamel.roles.AccountHolder;
import jamel.jamel.roles.Corporation;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.Cheque;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class BasicCapitalStockTest {

	private static BankAccount getNewAccount() {
		return new BankAccount() {

			@Override
			public void deposit(Cheque cheque) {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public AccountHolder getAccountHolder() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public long getAmount() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public long getCanceledDebt() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public long getCanceledMoney() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public long getDebt() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public String getInfo() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public long getInterest() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public long getLongTermDebt() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public long getNewDebt() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public long getRepaidDebt() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public long getShortTermDebt() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public boolean isCancelled() {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public Cheque newCheque(final long amount) {
				return new Cheque() {

					@Override
					public long getAmount() {
						return amount;
					}

					@Override
					public boolean payment() {
						throw new RuntimeException("Not yet implemented.");
					}

				};
			}

			@Override
			public void newLongTermLoan(long principal) {
				throw new RuntimeException("Not yet implemented.");
			}

			@Override
			public void newShortTermLoan(long principal) {
				throw new RuntimeException("Not yet implemented.");
			}
		};
	}

	private static Corporation getNewCorporation(final long bookValue) {
		return new Corporation() {

			@Override
			public Long getBookValue() {
				return bookValue;
			}

			@Override
			public String getName() {
				throw new RuntimeException("Not used.");
			}

			@Override
			public boolean isCancelled() {
				throw new RuntimeException("Not used.");
			}

		};
	}

	@Test
	public void test1() {
		final BasicTimer timer = new BasicTimer(0);
		final Corporation corporation = getNewCorporation(100000);
		final int shareholders = 10;
		final BankAccount account = getNewAccount();
		final BasicCapitalStock stock = new BasicCapitalStock(corporation, shareholders, account, timer);

		timer.next();
		stock.open();

		final List<StockCertificate> certificates = stock.getCertificates();
		for (StockCertificate certif : certificates) {
			assertEquals("Number of shares ", 10, certif.getShares(), 0);
			assertEquals("Book value ", 10000, certif.getBookValue(), 0);
			assertNull(certif.getDividend());
		}
		stock.setDividend(500);
		for (StockCertificate certif : certificates) {
			final Cheque cheque = certif.getDividend();
			assertEquals("Cheque amount", 50, cheque.getAmount(), 0);
			assertNull(certif.getDividend());
		}
	}

	@Test
	public void test2() {

		final BasicTimer timer = new BasicTimer(0);
		final long capital = 100000;
		final Corporation corporation = getNewCorporation(capital);
		final int shareholders = 3;
		final BankAccount account = getNewAccount();
		final BasicCapitalStock stock = new BasicCapitalStock(corporation, shareholders, account, timer);

		timer.next();
		stock.open();

		final List<StockCertificate> certificates = stock.getCertificates();

		final StockCertificate certif0 = certificates.get(0);
		assertEquals("Number of shares ", 34, certif0.getShares(), 0);
		assertEquals("Book value ", 34000, certif0.getBookValue(), 0);
		assertNull(certif0.getDividend());

		final StockCertificate certif1 = certificates.get(1);
		assertEquals("Number of shares ", 33, certif1.getShares(), 0);
		assertEquals("Book value ", 33000, certif1.getBookValue(), 0);
		assertNull(certif1.getDividend());

		final StockCertificate certif2 = certificates.get(2);
		assertEquals("Number of shares ", 33, certif2.getShares(), 0);
		assertEquals("Book value ", 33000, certif2.getBookValue(), 0);
		assertNull(certif2.getDividend());

		assertEquals("Book value ", capital, certif0.getBookValue() + certif1.getBookValue() + certif2.getBookValue(),
				0);

		long dividend = 700;

		stock.setDividend(dividend);

		long amount0 = certif0.getDividend().getAmount();
		long amount1 = certif1.getDividend().getAmount();
		long amount2 = certif2.getDividend().getAmount();
		long sum = amount0 + amount1 + amount2;

		assertEquals("Cheque amount", 238, amount0, 0);
		assertEquals("Cheque amount", 231, amount1, 0);
		assertEquals("Cheque amount", 231, amount2, 0);
		assertEquals("Sum of cheques", dividend, sum, 0);

		assertNull(certif0.getDividend());
		assertNull(certif1.getDividend());
		assertNull(certif2.getDividend());

		stock.close();
		timer.next();
		stock.open();

		dividend = 708;

		stock.setDividend(dividend);

		amount0 = certif0.getDividend().getAmount();
		amount1 = certif1.getDividend().getAmount();
		amount2 = certif2.getDividend().getAmount();
		sum = amount0 + amount1 + amount2;

		assertEquals("Cheque amount", 241, amount0, 0);
		assertEquals("Cheque amount", 234, amount1, 0);
		assertEquals("Cheque amount", 233, amount2, 0);
		assertEquals("Sum of cheques", dividend, sum, 0);

		assertNull(certif0.getDividend());
		assertNull(certif1.getDividend());
		assertNull(certif2.getDividend());

	}

	@Test
	public void test3() {

		final BasicTimer timer = new BasicTimer(0);
		final long capital = 4099;
		final Corporation corporation = getNewCorporation(capital);
		final int shareholders = 3;
		final BankAccount account = getNewAccount();
		final BasicCapitalStock stock = new BasicCapitalStock(corporation, shareholders, account, timer);

		timer.next();
		stock.open();

		final List<StockCertificate> certificates = stock.getCertificates();

		final StockCertificate certif0 = certificates.get(0);
		assertEquals("Number of shares ", 34, certif0.getShares(), 0);
		assertEquals("Book value ", 1394, certif0.getBookValue(), 0);
		assertNull(certif0.getDividend());

		final StockCertificate certif1 = certificates.get(1);
		assertEquals("Number of shares ", 33, certif1.getShares(), 0);
		assertEquals("Book value ", 1353, certif1.getBookValue(), 0);
		assertNull(certif1.getDividend());

		final StockCertificate certif2 = certificates.get(2);
		assertEquals("Number of shares ", 33, certif2.getShares(), 0);
		assertEquals("Book value ", 1352, certif2.getBookValue(), 0);
		assertNull(certif2.getDividend());

		assertEquals("Book value ", capital, certif0.getBookValue() + certif1.getBookValue() + certif2.getBookValue(),
				0);

		long dividend = 700;

		stock.setDividend(dividend);

		long amount0 = certif0.getDividend().getAmount();
		long amount1 = certif1.getDividend().getAmount();
		long amount2 = certif2.getDividend().getAmount();
		long sum = amount0 + amount1 + amount2;

		assertEquals("Cheque amount", 238, amount0, 0);
		assertEquals("Cheque amount", 231, amount1, 0);
		assertEquals("Cheque amount", 231, amount2, 0);
		assertEquals("Sum of cheques", dividend, sum, 0);

		assertNull(certif0.getDividend());
		assertNull(certif1.getDividend());
		assertNull(certif2.getDividend());

		stock.close();
		timer.next();
		stock.open();

		dividend = 708;

		stock.setDividend(dividend);

		amount0 = certif0.getDividend().getAmount();
		amount1 = certif1.getDividend().getAmount();
		amount2 = certif2.getDividend().getAmount();
		sum = amount0 + amount1 + amount2;

		assertEquals("Cheque amount", 241, amount0, 0);
		assertEquals("Cheque amount", 234, amount1, 0);
		assertEquals("Cheque amount", 233, amount2, 0);
		assertEquals("Sum of cheques", dividend, sum, 0);

		assertNull(certif0.getDividend());
		assertNull(certif1.getDividend());
		assertNull(certif2.getDividend());

	}

}

// ***
