package jamel.v170804.models.basicModel1.firms;

import jamel.util.JamelObject;
import jamel.v170804.data.AgentDataset;
import jamel.v170804.models.basicModel1.banks.Cheque;

/**
 * A basic supply for the basic firm.
 */
public class BasicSupply extends JamelObject implements Supply {

	/**
	 * The data keys.
	 */
	private static final BasicFirmKeys keys = new BasicFirmKeys();

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
	private long salesValue;

	/**
	 * The volume of the sales of the period.
	 */
	private long salesVolume;

	/**
	 * The supplier.
	 */
	final private BasicFirm supplier;

	/**
	 * The volume of this supply.
	 */
	private long volume;

	/**
	 * Creates a new supply.
	 * 
	 * @param firm
	 *            the supplier.
	 */
	public BasicSupply(final BasicFirm firm) {
		super(firm.getSimulation());
		this.supplier = firm;
		this.dataset = firm.getDataset();
	}

	/**
	 * Closes this supply. Must be called at the end of the period.
	 */
	@Override
	protected void close() {
		super.close();
	}

	/**
	 * Opens this supply. Must be called at the beginning of the period.
	 */
	@Override
	protected void open() {
		this.price = 0;
		this.volume = 0;
		this.salesVolume = 0;
		this.salesValue = 0;
		super.open();
	}

	/**
	 * Update the supply.
	 * 
	 * @param newVolume
	 *            the volume of goods offered.
	 * @param newPrice
	 *            the unit price.
	 */
	void update(final long newVolume, final double newPrice) {
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
		this.dataset.put(keys.salesValue, this.salesValue);
		this.dataset.put(keys.salesVolume, this.salesVolume);
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
	public long getVolume() {
		return this.volume;
	}

	@Override
	public boolean isEmpty() {
		return this.volume == 0;
	}

	@Override
	public Goods purchase(final long purchase, final Cheque cheque) {
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
