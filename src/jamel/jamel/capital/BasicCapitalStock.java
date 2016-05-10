package jamel.jamel.capital;

import jamel.basic.util.Timer;
import jamel.jamel.roles.Corporation;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.widgets.Chequable;
import jamel.jamel.widgets.Cheque;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A basic implementation of a capital stock.
 */
public class BasicCapitalStock implements CapitalStock {

	/** The cheque account. */
	private final Chequable account;

	/** The book value of this stock. */
	private long bookValue = 0;

	/** The list of the book values of each ownership share. */
	private final List<Long> bookValues = new ArrayList<Long>();

	/** If this stock is canceled. */
	private boolean canceled = false;

	/** The corporation. */
	final private Corporation corporation;

	/** The periode this stock was created. */
	private final int date;

	/** The dividend distributed. */
	private long distributedDividend;

	/** The dividend. */
	private Long dividend;

	/** The list of the dividends. */
	final private List<Long> dividends = new ArrayList<Long>();

	/**
	 * The quantity of shares of the corporation, which have been allocated
	 * (allotted) and are subsequently held by shareholders.
	 */
	private int issuedShares = 0;

	/** A flag that indicates whether the stock is open or not. */
	private boolean open = false;

	/** The curent period. */
	private Integer period = null;

	/** The list of the shares. */
	final private List<StockCertificate> shares = new ArrayList<StockCertificate>();

	/**
	 * The maximum amount of share capital that the company is authorized to
	 * issue (allocate) to shareholders.
	 */
	private final int sharesAuthorised = 100;

	/** The timer. */
	private final Timer timer;

	/**
	 * Creates a new basic capital stock for the specified corporation.
	 * 
	 * @param corporation
	 *            the corporation the capital stock of which is to be created.
	 * @param account
	 *            the bank account.
	 * @param timer
	 *            the timer.
	 * 
	 */
	public BasicCapitalStock(Corporation corporation, Chequable account, Timer timer) {
		this.corporation = corporation;
		this.account = account;
		this.timer = timer;
		this.date = timer.getPeriod().intValue();
	}

	/**
	 * Creates a new basic capital stock for the specified corporation and
	 * prepares the distribution of the capital in shares of equal size.
	 * 
	 * @param corporation
	 *            the corporation the capital stock of which is to be created.
	 * @param shareholders
	 *            the number of shareholders between this stock must be shared.
	 * @param account
	 *            the bank account.
	 * @param timer
	 *            the timer.
	 * 
	 */
	public BasicCapitalStock(Corporation corporation, int shareholders, Chequable account, Timer timer) {
		this(corporation, account, timer);
		this.shareOutCapital(shareholders);
	}

	/**
	 * Returns <code>true</code> if the book value of this stock has changed.
	 * 
	 * @return <code>true</code> if the book value of this stock has changed.
	 */
	private boolean bookValueAsChanged() {
		return (this.bookValue != this.corporation.getBookValue());
	}

	/**
	 * Creates a new set of ownership shares.
	 * 
	 * @param id
	 *            the id of this ownership share.
	 * 
	 * @param size
	 *            the number of shares to be created.
	 * @return a new set of ownership shares.
	 */
	private StockCertificate getNewShares(final int id, final int size) {

		if (size + this.issuedShares > this.sharesAuthorised) {
			throw new IllegalArgumentException("Shares issued: "+(size + this.issuedShares)+", authorised: "+this.sharesAuthorised);
		}

		this.issuedShares += size;

		return new StockCertificate() {

			@Override
			public Long getBookValue() {
				final Long result;
				if (canceled) {
					result = null;
				} else {
					if (bookValueAsChanged()) {
						updateBookValue();
					}
					result = BasicCapitalStock.this.bookValues.get(id);
				}
				return result;
			}

			@Override
			public Corporation getCorporation() {
				return corporation;
			}

			@Override
			public Cheque getDividend() {
				if (!open) {
					throw new RuntimeException("This stock is closed");
				}
				final Cheque result;
				final Long amount = dividends.get(id);
				if (amount == null || amount == 0) {
					result = null;
				} else {
					result = account.newCheque(amount);
					dividends.set(id, null);
					distributedDividend += amount;
				}
				return result;
			}

			@Override
			public int getShares() {
				return size;
			}

			@Override
			public boolean isCancelled() {
				return canceled;
			}

		};
	}

	/**
	 * Shares out the stock between the specified number of shareholders.
	 * 
	 * @param shareholders
	 *            the number of shareholders.
	 */
	private void shareOutCapital(int shareholders) {
		final int[] sizes = new int[shareholders];
		final int size = sharesAuthorised / shareholders;
		int remainder = sharesAuthorised;
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = size;
			remainder -= size;
		}
		if (remainder < 0) {
			throw new RuntimeException("Something went wrong while sharing out this stock.");
		}
		if (remainder > 0) {
			for (int i = 0; i < sizes.length; i++) {
				sizes[i]++;
				remainder--;
				if (remainder == 0) {
					break;
				}
			}
		}
		if (remainder != 0) {
			throw new RuntimeException("Something went wrong while sharing out this stock.");
		}
		for (int id = 0; id < sizes.length; id++) {
			final StockCertificate newShares = this.getNewShares(id, sizes[id]);
			this.shares.add(id, newShares);
			this.bookValues.add(id, 0l);
			this.dividends.add(id, null);
		}
	}

	/**
	 * Updates the book value of this stock.
	 * <p>
	 * The book value of each share is updated.
	 */
	private void updateBookValue() {
		
		if (canceled) {
			throw new RuntimeException("This capital stock is canceled.");
		}
		
		if (this.issuedShares != this.sharesAuthorised) {
			throw new RuntimeException("The distribution of this capital stock is not completed.");
		}
		
		if (this.corporation.getBookValue() != this.bookValue) {
			
			// On n'ajuste que si la valeur de la companie a changé. 
			
			this.bookValue = this.corporation.getBookValue();
			long residualValue = this.bookValue;
			int residualShares = this.issuedShares;
			for (int id = 0; id < this.bookValues.size(); id++) {
				final long newBookValue;
				final int myShares = this.shares.get(id).getShares(); 
				if (myShares==residualShares) {
					newBookValue=residualValue;
				} else {
					final double fraction = 1d*myShares/residualShares;
					newBookValue= (long) (fraction*residualValue);
				}
				this.bookValues.set(id, newBookValue);
				residualValue -= newBookValue;
				residualShares -= myShares;
			}			
			
			if (residualValue != 0) {
				throw new RuntimeException("Something went wrong while updating the book value.");
			}
			
		}
		
	}

	@Override
	public void cancel() {
		if (!open) {
			throw new RuntimeException("This stock is closed.");
		}
		if (canceled) {
			throw new RuntimeException("This capital stock is already canceled.");
		}
		canceled = true;
	}

	@Override
	public void close() {
		if (!this.open) {
			throw new RuntimeException("Already closed.");
		}
		if ((this.dividend != null && this.dividend != this.distributedDividend)
				|| (this.dividend == null && this.distributedDividend != 0)) {
			throw new RuntimeException("Inconsistent dividends.");
		}
		this.open = false;
	}

	@Override
	public List<StockCertificate> getCertificates() {
		if (canceled) {
			throw new RuntimeException("This capital stock is canceled.");
		}
		return new LinkedList<StockCertificate>(this.shares);
	}

	@Override
	public Corporation getCorporation() {
		return this.corporation;
	}

	@Override
	public int getDate() {
		return date;
	}

	@Override
	public double getDistributedDividends() {
		return this.distributedDividend;
	}

	@Override
	public boolean isOpen() {
		return this.open;
	}

	@Override
	public StockCertificate issueNewShares(Integer nShares) {
		if (canceled) {
			throw new RuntimeException("This capital stock is canceled.");
		}
		if (!open) {
			throw new RuntimeException("This stock is closed.");
		}
		final int id = this.shares.size();
		final StockCertificate newShares = this.getNewShares(id, nShares);
		this.shares.add(id, newShares);
		this.bookValues.add(id, 0l);
		this.dividends.add(id, null);
		return newShares;
	}

	@Override
	public void open() {
		if (canceled) {
			throw new RuntimeException("This capital stock is canceled.");
		}
		if (this.open) {
			throw new RuntimeException("Already open.");
		}
		if (period == null) {
			period = this.timer.getPeriod().intValue();
		} else {
			period++;
			if (period != this.timer.getPeriod().intValue()) {
				throw new AnachronismException(
						"Current period expected <" + period + "> but was <" + this.timer.getPeriod().intValue() + ">");
			}
		}
		this.open = true;
		this.dividend = null;
		this.distributedDividend = 0;
	}

	@Override
	public void setDividend(final long dividend) {
		if (canceled) {
			throw new RuntimeException("This capital stock is canceled.");
		}
		if (!this.open) {
			throw new RuntimeException("This stock is closed.");
		}
		if (this.dividend != null) {
			throw new RuntimeException("The dividend is already set.");
		}
		if (dividend < 0) {
			throw new RuntimeException("The dividend must be positive.");
		}
		if (this.issuedShares != this.sharesAuthorised) {
			throw new RuntimeException("Issued shares is " + this.issuedShares + ", expected " + this.sharesAuthorised
					+ ": " + this.corporation.getName()+", period "+this.timer.getPeriod().intValue());
		}
		this.dividend = dividend;
		if (dividend > 0) {
			long remainder = dividend;
			for (int id = 0; id < shares.size(); id++) {
				final long div = dividend * this.shares.get(id).getShares() / this.sharesAuthorised;
				this.dividends.set(id, div);
				remainder -= div;
			}
			while (remainder > 0) {
				for (int id = 0; id < shares.size(); id++) {
					this.dividends.set(id, this.dividends.get(id) + 1);
					remainder--;
					if (remainder == 0) {
						break;
					}
				}
			}
		}
	}

}

// ***
