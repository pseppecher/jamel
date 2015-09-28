package jamel.jamel.firms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jamel.Jamel;
import jamel.jamel.capital.BasicCapitalStock;
import jamel.jamel.capital.CapitalStock;
import jamel.jamel.capital.StockCertificate;
import jamel.jamel.firms.factory.FinishedGoods;
import jamel.jamel.firms.factory.Machine;
import jamel.jamel.firms.managers.CapitalManager;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.util.Memory;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.Supply;

/**
 * Une firme expérimentale pour préparer l'investissement.
 */
@SuppressWarnings("javadoc")
public class Firm150908 extends BasicFirm implements Investor {

	private Memory<Long> salesValueMemory = new Memory<Long>(12);

	private Memory<Long> salesVolumeMemory = new Memory<Long>(12);

	private Memory<Long> wagebillMemory = new Memory<Long>(12);

	private Memory<Integer> workforceMemory = new Memory<Integer>(12);

	private float animalSpirit = 0;
	
	final private static float discountRate = 0.005f; // Should be a parameter.

	public Firm150908(String name, IndustrialSector sector) {
		super(name, sector);
	}
	
	/**
	 * Returns an array of supplies, sorted by price in ascending order.
	 * 
	 * @return an array of supplies, sorted by price in ascending order.
	 */
	private Supply[] getSupplies(int size) {

		final ArrayList<Supply> listOfSupplies = new ArrayList<Supply>(size);
		for (final Firm firm : this.sector.getSimpleRandomSample(size)) {
			if (!firm.equals(this)) {
				// Une firme ne peut pas être son propre fournisseur.

				final Supply supply = firm.getSupply();
				if (supply != null) {
					listOfSupplies.add(supply);
				}
			}
		}

		// On transforme la liste en tableau.
		final Supply[] supplies = listOfSupplies.toArray(new Supply[listOfSupplies.size()]);

		// On range les offres selon leurs prix, les moins chers d'abord.
		if (supplies.length > 0) {
			Arrays.sort(supplies, InvestorToolBox.supplyComparator);
		}

		return supplies;

	}

	@Override
	protected void closeManagers() {
		super.closeManagers();
		this.salesVolumeMemory.add((Long) this.salesManager.askFor("salesVolume"));
		this.salesValueMemory.add((Long) this.salesManager.askFor("salesValue"));
		this.wagebillMemory.add((Long) this.workforceManager.askFor("wagebill"));
		this.workforceMemory.add((Integer) this.workforceManager.askFor("workforce"));
	}

	/**
	 * Creates and returns a new capital manager.
	 * 
	 * Lisse la distribution des dividendes.
	 * 
	 * @return a new {@linkplain CapitalManager}.
	 */
	@Override
	protected CapitalManager getNewCapitalManager() {

		final CapitalManager newCapitalManager = new CapitalManager("CapitalManager", timer) {

			private CapitalStock capitalStock = null;

			/** The dividend paid. */
			private long dividend;

			private Memory<Long> income = new Memory<Long>(12);

			/** The capital of the firm at the beginning of the period. */
			private long initialCapital;

			/**
			 * The memory of past net profits.
			 */
			private Memory<Long> netProfitMemory = new Memory<Long>(12);

			/**
			 * A flag that indicates whether the ownership of the firm is
			 * distributed or not.
			 */
			private boolean ownership = false;

			private long getCapital() {
				return factory.getValue() + account.getAmount() - account.getDebt();
			}

			private long getCapitalTarget() {
				final long assets = account.getAmount() + factory.getValue();
				return (long) ((assets) * sector.getParam(CAPITAL_TARGET));
			}

			/**
			 * Returns the amount of debt exceeding the firm target.
			 * 
			 * @return the amount of debt exceeding the firm target.
			 */
			private double getLiabilitiesExcess() {
				final double result;
				final double excess = account.getDebt() - getLiabilitiesTarget();
				result = Math.max(0, excess);
				return result;
			}

			/**
			 * Returns the target value of the liabilities.
			 * 
			 * @return the target value of the liabilities.
			 */
			private double getLiabilitiesTarget() {
				final long assets = account.getAmount() + factory.getValue();
				final long capitalTarget = getCapitalTarget();// (long)
				// ((assets) *
				// sector.getParam(CAPITAL_TARGET));
				return assets - capitalTarget;
			}

			@Override
			public Object askFor(String key) {
				checkConsistency();
				final Object result;
				if (key.equals("capital")) {
					result = this.getCapital();
				} else if (key.equals("capitalTarget")) {
					result = getCapitalTarget();
				} else if (key.equals("assets")) {
					result = factory.getValue() + account.getAmount();
				} else if (key.equals("liabilities")) {
					result = account.getDebt();
				} else {
					result = null;
				}
				return result;
			}

			@Override
			public void bankrupt() {
				checkConsistency();
				capitalStock.cancel();
			}

			@Override
			public void clearOwnership() {
				List<Firm> sample = sector.getSimpleRandomSample(1);
				Firm150908.this.animalSpirit = (Float) sample.get(0).askFor("animalSpirit");
				checkConsistency();
				final boolean isOpen = capitalStock.isOpen();
				this.capitalStock.cancel();
				this.capitalStock = new BasicCapitalStock(Firm150908.this, account, timer);
				if (isOpen) {
					this.capitalStock.open();
				}
			}

			@Override
			public void close() {
				checkConsistency();

				this.capitalStock.close();

				isConsistent();

				final long cash = account.getAmount();
				final long factoryValue = factory.getValue();
				final long assets = factoryValue + cash;
				final long liabilities = account.getDebt();
				final long capital = assets - liabilities;
				final boolean insolvent = (timer.getPeriod().intValue() - creation > 12 && capital < 0);
				// TODO: 12 should be a parameter
				
				this.income.add(capital - initialCapital + this.dividend);

				final long shortTermDebt = account.getShortTermDebt();
				final long longTermDebt = account.getLongTermDebt();

				this.dataset.put("cash", (double) cash);
				this.dataset.put("assets", (double) assets);
				this.dataset.put("liabilities", (double) liabilities);
				this.dataset.put("capital", (double) capital);

				this.dataset.put("debt.shortTerm", shortTermDebt);
				this.dataset.put("debt.longTerm", longTermDebt);

				this.dataset.put("dividends", (double) this.dividend);
				this.dataset.put("interest", (double) account.getInterest());

				this.dataset.put("liabilities.target", getLiabilitiesTarget());
				this.dataset.put("liabilities.excess", getLiabilitiesExcess());

				this.dataset.put("liabilities.new", account.getNewDebt());
				this.dataset.put("liabilities.repayment", account.getRepaidDebt());

				this.dataset.put("canceledDebts", (double) account.getCanceledDebt());
				this.dataset.put("canceledDeposits", (double) account.getCanceledMoney());

				final long netProfit = capital - initialCapital + dividend;

				this.netProfitMemory.add(netProfit);
				final double returnOnEquity = this.netProfitMemory.getSum() / capital;
				final double returnOnAssets = this.netProfitMemory.getSum() / assets;
				this.dataset.put("returnOnEquity", returnOnEquity);
				this.dataset.put("returnOnAssets", returnOnAssets);

				if (insolvent) {
					this.dataset.put("insolvents", 1.);
				} else {
					this.dataset.put("insolvents", 0.);
				}
			}

			/**
			 * Issues the specified number of new shares.
			 * 
			 * @param nShares
			 *            the number of new shares to be issued.
			 * @return a {@link StockCertificate} that encapsulates the new shares.
			 */
			private StockCertificate getNewShares(Integer nShares) {
				checkConsistency();
				return this.capitalStock.issueNewShares(nShares);
			}

			@Override
			public StockCertificate[] getNewShares(List<Integer> shares) {
				this.clearOwnership();
				final StockCertificate[] newShares = new StockCertificate[shares.size()];
				for (int i = 0; i < shares.size(); i++) {
					newShares[i] = this.getNewShares(shares.get(i));
				}
				return newShares;
			}
			
			@Override
			public boolean isConsistent() {
				checkConsistency();
				final boolean isConsistent;
				final long grossProfit = (Long) salesManager.askFor("grossProfit");
				final long interest = account.getInterest();
				final long bankruptcy = account.getCanceledMoney() + factory.getInventoryLosses()
						- account.getCanceledDebt();
				final long capital = this.getCapital();

				isConsistent = (capital == this.initialCapital + grossProfit - (this.dividend + interest + bankruptcy));
				if (!isConsistent) {
					if (this.capitalStock.getDistributedDividends() != this.dividend) {
						Jamel.println("distributed dividend = " + this.capitalStock.getDistributedDividends());
						Jamel.println("expected = " + this.dividend);
						// throw new RuntimeException("Inconsistency");
					}
					Jamel.println("capital = " + capital);
					Jamel.println("expected = " + (this.initialCapital + grossProfit
							- (this.capitalStock.getDistributedDividends() + interest + bankruptcy)));
					throw new RuntimeException("Inconsistency: " + Firm150908.this.name);
				}
				return isConsistent;
			}

			@Override
			public boolean isSolvent() {
				checkConsistency();
				return (this.getCapital() >= 0);
			}

			@Override
			public void open() {
				super.open();
				this.initialCapital = this.getCapital();
				this.dividend = 0;
				this.capitalStock.open();
			}

			@Override
			public void payDividend() {
				checkConsistency();
				isConsistent();
				final long cash = account.getAmount();
				final long assets = cash + factory.getValue();
				final double capital = getCapital();
				final long capitalTarget = (long) ((assets) * sector.getParam(CAPITAL_TARGET));
				final double averageIncome = this.income.getMean();
				long newDividend;
				if (capital > 0) {
					final double ratio = capital / capitalTarget;
					newDividend = (long) Math.min(averageIncome * ratio, capital / 6);
				} else {
					newDividend = 0;
				}
				if (newDividend > cash) {
					newDividend = cash;
				}
				if (newDividend < 0) {
					newDividend = 0;
				}
				dividend = newDividend;
				capitalStock.setDividend(dividend);

				this.dataset.put("payDividend.cash", cash);
				this.dataset.put("payDividend.assets", assets);
				this.dataset.put("payDividend.capital", capital);
				this.dataset.put("payDividend.capitalTarget", capitalTarget);
				this.dataset.put("payDividend.averageIncome", averageIncome);
				this.dataset.put("payDividend.dividend", dividend);

				this.dataset.put("debt2target.ratio", (account.getDebt()) / getLiabilitiesTarget());
			}

			@Override
			public void secureFinancing(long amount) {
				checkConsistency();
				if (amount > account.getAmount()) {
					account.newShortTermLoan(amount - account.getAmount());
				}
				if (account.getAmount() < amount) {
					throw new RuntimeException("Production is not financed.");
				}
			}

			@Override
			public void updateOwnership() {
				// checkChronologicalConsistency();
				if (ownership) {
					throw new RuntimeException("The ownership of this firm is already fixed.");
				}
				final List<Shareholder> shareHolders = sector.selectCapitalOwner(10);
				if (shareHolders.size() > 0) {
					this.capitalStock = new BasicCapitalStock(Firm150908.this, shareHolders.size(), account, timer);
					List<StockCertificate> certificates = this.capitalStock.getCertificates();
					for (int id = 0; id < certificates.size(); id++) {
						final StockCertificate certif = certificates.get(id);
						final Shareholder shareHolder = shareHolders.get(id);
						shareHolder.addAsset(certif);
					}
					ownership = true;
				} else {
					throw new RuntimeException("No shareholder.");
				}
			}
		};
		newCapitalManager.updateOwnership();
		return newCapitalManager;
	}

	@Override
	public void invest() {
		final long capital = (Long) this.capitalManager.askFor("capital");
		final long capitalTarget = (Long) this.capitalManager.askFor("capitalTarget");
		this.data.put("demand12", salesVolumeMemory.getMean());

		final double workforce = this.workforceMemory.getSum();
		final double salesVolume = this.salesVolumeMemory.getSum();

		int investmentSize = 0;
		long investmentCost = 0;
		long investmentVolume = 0;

		final float firmness = 0.95f; // TODO firmness should be a parameter.
		final float change = random.nextFloat();
		if (change > firmness) {
			animalSpirit += 4f * (random.nextFloat() - 0.5f);
		}

		if (capital > capitalTarget) {

			// Il faut que le niveau de capital de la firme soit satisfaisant.

			if (workforce > 0 && salesVolume > 0) {

				// Il faut que la firme ait fonctionné au moins une fois au
				// cours des périodes récentes, pour qu'on puisse calculer un
				// prix moyen et un salaire moyen.

				final double price = this.salesValueMemory.getSum() / salesVolume;
				final double wage = this.wagebillMemory.getSum() / workforce;

				final Technology technology = new Technology() {

					@Override
					public long getInputVolumeForANewMachine() {
						return 1000;
					}

					@Override
					public int getProductionTime() {
						return 5;
					}

					@Override
					public long getProductivity() {
						return 100;
					}
				};

				final long productivity = technology.getProductivity();
				// TODO: demander au secteur la productivité de la technologie
				// courante

				if (productivity * price > wage) {

					// Il faut que la technologie courante soit rentable.

					// On récupère une liste d'offres.
					final Supply[] supplies = getSupplies(10);

					if (supplies.length > 0) {

						// Il faut qu'il y ait au moins 1 offre de 'raw
						// materials'.

						final long input = technology.getInputVolumeForANewMachine();

						final Long[] machinePrices = InvestorToolBox.getPrices(supplies, input);

						final long[] machinery = (long[]) this.factory.askFor("machinery");
						final double anticipedDemand = this.salesVolumeMemory.getMean() * (1. + animalSpirit / 100);// 1.1;
						// TODO: il faut intégrer ici un facteur indiquant la
						// tendance et/ou l'esprit animal de l'entrepreneur

						// TODO: Il faudrait demander à la banque son taux +
						// tenir
						// compte de l'inflation + aversion au risque

						final int time = 120;
						investmentSize = InvestorToolBox.getOptimumSize(machinePrices, productivity, machinery,
								anticipedDemand, price, wage, discountRate, time);
						investmentCost = machinePrices[investmentSize];

						if (investmentSize > 0) {
							// Jamel.println("investmentCost: " +
							// investmentCost);
							final long newLongTermLoan = (long) (0.5 + investmentCost * 1.001
									- this.account.getAmount());
							// TODO: emprunter la totalité du coût de
							// l'investissement.

							if (newLongTermLoan > 0) {
								this.account.newLongTermLoan(newLongTermLoan);
								// Jamel.println("new loan: " +
								// newLongTermLoan);
							}
							// Jamel.println("this.account.getAmount(): " +
							// this.account.getAmount());

							long requiredVolume = investmentSize * input;
							// Jamel.println("requiredVolume: " +
							// requiredVolume);

							final Commodities stuff = new FinishedGoods();

							// TODO: WORK IN PROGRESS 11-09-2015

							for (Supply supply : supplies) {
								final long supplyVolume = supply.getVolume();
								final long purchaseVolume;
								if (supplyVolume > requiredVolume) {
									purchaseVolume = requiredVolume;
								} else {
									purchaseVolume = supplyVolume;
								}
								final long expense = supply.getPrice(purchaseVolume);
								final Commodities purchase = supply.buy(purchaseVolume,
										this.account.newCheque(expense));
								stuff.put(purchase);
								requiredVolume -= purchaseVolume;
								if (requiredVolume == 0) {
									break;
								}
							}
							if (stuff.getValue() > 1.01 * investmentCost + 1 || stuff.getValue() < investmentCost) {
								throw new RuntimeException(
										"Expense is " + stuff.getValue() + ", expected was " + investmentCost);
							}
							investmentCost = stuff.getValue();
							if (stuff.getVolume() != investmentSize * input) {
								throw new RuntimeException("Not enough stuff.");
							}
							investmentVolume = stuff.getVolume();
							final Machine[] newMachines = InvestorToolBox.getNewMachines(investmentSize, stuff,
									technology, timer, random);
							// TODO: vérifier que stuff a été consommé.
							this.factory.expandCapacity(newMachines);
						}

					}

				}
			}
		}
		this.data.put("investment.size", investmentSize);
		this.data.put("investment.vol", investmentVolume);
		this.data.put("investment.val", investmentCost);
		this.data.put("animalSpirit", animalSpirit);
		this.data.put("animalSpirit.weighted", animalSpirit * (Long) this.capitalManager.askFor("capital"));
		this.data.put("discountRate", discountRate);
		this.data.put("discountRate.weighted", discountRate * (Long) this.capitalManager.askFor("capital"));
	}

	@Override
	public Object askFor(String key) {
		final Object result;
		if ("animalSpirit".equals(key)) {
			result = this.animalSpirit;
		} else {
			result = null;
		}
		return result;
	}

}

// ***
