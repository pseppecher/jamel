package jamel.basicModel.households;

import java.util.function.Consumer;

import jamel.basicModel.banks.Account;
import jamel.basicModel.banks.Amount;
import jamel.basicModel.banks.Bank;
import jamel.basicModel.banks.Cheque;
import jamel.basicModel.firms.Goods;
import jamel.basicModel.firms.Supplier;
import jamel.basicModel.firms.Supply;
import jamel.util.Agent;
import jamel.util.AgentDataset;
import jamel.util.JamelObject;
import jamel.util.NotUsedException;
import jamel.util.Sector;

/**
 * Represents a shareholder.
 */
public class BasicShareholder extends JamelObject implements Agent, Shareholder {

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
		case "opening":
			action = (agent) -> {
				((BasicShareholder) agent).open();
			};
			break;
		case "consumption":
			action = (agent) -> {
				((BasicShareholder) agent).consumption();
			};
			break;
		case "closure":
			action = (agent) -> {
				((BasicShareholder) agent).close();
			};
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}

		return action;

	}

	/**
	 * The bank account of this shareholder.
	 */
	private final Account account;

	/**
	 * The dataset.
	 */
	final private AgentDataset dataset;

	/**
	 * The amount of the dividends received for this period.
	 */
	private final Amount dividends = new Amount();

	/**
	 * The id of this agent.
	 */
	final private int id;

	/**
	 * A flag that indicates whether this shareholder is open or not.
	 */
	private boolean open;

	/**
	 * The parent sector.
	 */
	final private Sector sector;

	/**
	 * The sector of the suppliers.
	 */
	private final Sector supplierSector;

	/**
	 * Creates a new shareholder.
	 * 
	 * @param sector
	 *            the sector of this shareholder.
	 * @param id
	 *            the id of this shareholder.
	 */
	public BasicShareholder(final Sector sector, final int id) {
		super(sector.getSimulation());
		this.sector = sector;
		this.id = id;
		this.account = ((Bank) this.getSimulation().getSector("Banks").select(1)[0]).openAccount(this);
		this.supplierSector = this.getSimulation().getSector("Firms");
		// TODO "Firms" sould be a parameter
		if (this.supplierSector == null) {
			throw new RuntimeException("Supplier sector is missing.");
		}
		this.dataset = new AgentDataset(this);
	}

	/**
	 * Closes the shareholder at the end of the period.
	 */
	private void close() {
		if (!this.open) {
			throw new RuntimeException("Already closed.");
		}
		this.dataset.put("countAgent", 1);
		this.dataset.put("money", this.account.getAmount());
		this.open = false;
	}

	/**
	 * The consumption phase.
	 */
	private void consumption() {
		if (!this.open) {
			throw new RuntimeException("Closed.");
		}
		long consumptionVolume = 0;
		long consumptionValue = 0;
		long budget = this.account.getAmount();
		this.dataset.put("consumptionBudget", budget);
		if (budget > 0) {
			final Agent[] selection = this.supplierSector.select(10);
			while (budget > 0) {
				Agent supplier = null;
				for (int i = 0; i < selection.length; i++) {
					if (selection[i] != null) {
						final Supply supply_i = ((Supplier) selection[i]).getSupply();
						if (supply_i == null || supply_i.getPrice() > budget || supply_i.getVolume() == 0) {
							selection[i] = null;
						} else if (supplier == null) {
							supplier = selection[i];
							selection[i] = null;
						} else if (((Supplier) supplier).getSupply().getPrice() > supply_i.getPrice()) {
							final Agent disappointing = supplier;
							supplier = selection[i];
							selection[i] = disappointing;
						}
					}
				}
				if (supplier == null) {
					break;
				}

				final Supply supply = ((Supplier) supplier).getSupply();
				final long spending;
				final int consumVol;
				if (supply.getTotalValue() <= budget) {
					consumVol = supply.getVolume();
					spending = (long) (consumVol * supply.getPrice());
					if (spending != supply.getTotalValue()) {
						throw new RuntimeException("Inconsistency.");
					}
				} else {
					consumVol = (int) (budget / supply.getPrice());
					spending = (long) (consumVol * supply.getPrice());
				}
				final Goods goods = supply.purchase(consumVol,
						this.account.issueCheque(supply.getSupplier(), spending));
				if (goods.getVolume() != consumVol) {
					throw new RuntimeException("Bad volume");
				}
				budget -= spending;
				consumptionValue += spending;
				consumptionVolume += goods.getVolume();
				goods.consume();
			}

		}
		this.dataset.put("consumptionVolume", consumptionVolume);
		this.dataset.put("consumptionValue", consumptionValue);
		// TODO updater les chiffres de la consommation et de l'épargne.
	}

	/**
	 * Opens the shareholder at the beginning of the period.
	 */
	private void open() {
		if (this.open) {
			throw new RuntimeException("Already open.");
		}
		this.open = true;
		this.dividends.cancel();
	}

	@Override
	public void acceptDividendCheque(Cheque cheque) {
		this.account.deposit(cheque);
		// TODO comptabiliser ce dépôt pour calculer le revenu de l'agent.
		// a des fins statistiques mais aussi comportementales.
	}

	@Override
	public Long getAssetTotalValue() {
		throw new NotUsedException();
	}

	@Override
	public int getBorrowerStatus() {
		throw new NotUsedException();
	}

	@Override
	public Double getData(String dataKey, String period) {
		return this.dataset.getData(dataKey);
	}

	@Override
	public String getName() {
		return "shareholder_" + this.id;
	}

	@Override
	public Sector getSector() {
		return this.sector;
	}

	@Override
	public void goBankrupt() {
		throw new NotUsedException();
	}

	@Override
	public boolean isBankrupted() {
		throw new NotUsedException();
	}

	@Override
	public boolean isSolvent() {
		throw new NotUsedException();
	}

}
