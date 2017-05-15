package jamel.basicModel.firms;

import jamel.basicModel.banks.Cheque;
import jamel.util.AgentDataset;

/**
 * A basic supply for the basic firm.
 */
public class BasicSupply implements Supply {

	/**
	 * The dataset.
	 */
	private final AgentDataset dataset;

	/**
	 * The unit price.
	 */
	private double price;

	/**
	 * The value of the sales of the period.
	 */
	private int salesValue;

	/**
	 * The volume of the sales of the period.
	 */
	private int salesVolume;

	/**
	 * The supplier.
	 */
	final private BasicFirm supplier;

	/**
	 * The current period.
	 */
	private int time;

	/**
	 * The volume of this supply.
	 */
	private int volume;

	/**
	 * Creates a new supply.
	 * 
	 * @param firm
	 *            the supplier.
	 */
	public BasicSupply(final BasicFirm firm) {
		this.supplier = firm;
		this.dataset = firm.getDataset();
		this.time = firm.getPeriod() - 1;
	}

	/**
	 * Resets the supply.
	 */
	void reset() {
		if (!this.supplier.isOpen()) {
			throw new RuntimeException("Closed.");
		}
		this.time++;
		if (this.time != this.supplier.getPeriod()) {
			throw new RuntimeException("Inconsistency.");
		}
		this.price = 0;
		this.volume = 0;
		this.salesVolume = 0;
		this.salesValue = 0;
		this.dataset.clear();
	}

	/**
	 * Update the supply.
	 * 
	 * @param newVolume
	 *            the volume of goods offered.
	 * @param newPrice
	 *            the unit price.
	 */
	void update(final int newVolume, final double newPrice) {
		if (newVolume == 0 || newVolume == 0) {
			throw new IllegalArgumentException();
		}
		if (this.volume != 0 || this.price != 0) {
			throw new RuntimeException();
		}
		this.volume = newVolume;
		this.price = newPrice;
	}

	/**
	 * Updates the dataset.
	 */
	void updateData() {
		this.dataset.put("salesValue", this.salesValue);
		this.dataset.put("salesVolume", this.salesVolume);
	}

	@Override
	public double getPrice() {
		return this.price;
	}

	@Override
	public Supplier getSupplier() {
		return this.supplier;
	}

	@Override
	public long getTotalValue() {
		return (long) (this.volume * this.price);
	}

	@Override
	public int getVolume() {
		return this.volume;
	}

	@Override
	public boolean isEmpty() {
		return this.volume == 0;
	}

	@Override
	public Goods purchase(final int purchase, final Cheque cheque) {
		if ((long) (this.price * purchase) != cheque.getAmount()) {
			throw new RuntimeException("Inconsistency");
		}
		if (purchase > this.volume) {
			throw new RuntimeException("Inconsistency");
		}
		this.volume -= purchase;
		final Goods result = this.supplier.supply(purchase);
		result.setValue(cheque.getAmount());
		this.salesVolume += result.getVolume();
		this.salesValue += result.getValue();
		this.supplier.accept(cheque);
		return result;
	}

}
