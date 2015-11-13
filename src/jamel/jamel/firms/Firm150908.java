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
import jamel.jamel.firms.managers.WorkforceManager;
import jamel.jamel.firms.util.Workforce;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.roles.Worker;
import jamel.jamel.util.Memory;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.JobContract;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.LaborPower;
import jamel.jamel.widgets.Supply;

/**
 * Une firme expérimentale pour préparer l'investissement.
 */
@SuppressWarnings("javadoc")
public class Firm150908 extends BasicFirm implements Investor {

	final private static float discountRate = 0.006f; // Should be a parameter.

	private float animalSpirit;

	private Double averagePrice = null;

	private Double averageWage = null; 

	private float capitalTargetRatio = 0f;

	private Memory<Long> salesValueMemory = new Memory<Long>(12);

	private Memory<Long> salesVolumeMemory = new Memory<Long>(12);

	private final float mutation;

	private final float stability;

	private final Technology technology = new Technology() {

		@Override
		public long getInputVolumeForANewMachine() {
			return 500;
		}

		@Override
		public int getProductionTime() {
			return sector.getParam(PRODUCTION_TIME).intValue();
		}

		@Override
		public long getProductivity() {
			return 100;
		}
	};

	private Memory<Long> wagebillMemory = new Memory<Long>(12);

	private Memory<Integer> workforceMemory = new Memory<Integer>(12);

	private int imitations=0;

	public Firm150908(String name, IndustrialSector sector) {
		super(name, sector);
		//this.animalSpirit = 0.5f + 0.5f * this.random.nextFloat();
		this.animalSpirit = sector.getParam("animalSpirits").floatValue();
		final float min = sector.getParam("capitalTargetRatio.initialValue.min").floatValue();
		final float max = sector.getParam("capitalTargetRatio.initialValue.max").floatValue();
		this.capitalTargetRatio = min + (max-min) * this.random.nextFloat();
		this.mutation = sector.getParam("mutation.strenght").floatValue();
		this.stability = 1f-sector.getParam("mutation.probability").floatValue(); 
	}

	private void imitation() {
		this.imitations++;
		List<Firm> sample = sector.getSimpleRandomSample(1);
		Firm firm = sample.get(0);
		//Firm150908.this.animalSpirit = (Float) firm.askFor("animalSpirit");
		Firm150908.this.capitalTargetRatio = (Float) firm.askFor("capitalTargetRatio");
		mutation(this.mutation);
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

	private void invest(int investmentSize, Long[] machinePrices, long input, Supply[] supplies) {
		if (investmentSize > machinePrices.length - 1) {
			throw new IllegalArgumentException("Investment size is " + investmentSize + " but there is only "
					+ (machinePrices.length - 1) + " machines available.");
		}
		long investmentCost = machinePrices[investmentSize];
		final long need = (long) (0.5 + investmentCost * 1.001);
		final long autofinancement = (long) (investmentCost * this.capitalTargetRatio);
		final long newLongTermLoan = need - autofinancement;
		if (newLongTermLoan > 0) {
			this.account.newLoan(newLongTermLoan, 120, true);
		}
		if (this.account.getAmount() < need) {
			final long newShortTermLoan = need - this.account.getAmount();
			this.account.newLoan(newShortTermLoan, 12, true);
		}
		/*
		 * final long newLongTermLoan = (long) (0.5 + investmentCost * 1.001 -
		 * this.account.getAmount());
		 */

		long requiredVolume = investmentSize * input;

		final Commodities stuff = new FinishedGoods();

		// long totalExpense = 0;
		for (Supply supply : supplies) {
			final long supplyVolume = supply.getVolume();
			final long purchaseVolume;
			if (supplyVolume > requiredVolume) {
				purchaseVolume = requiredVolume;
			} else {
				purchaseVolume = supplyVolume;
			}
			final long expense = supply.getPrice(purchaseVolume);
			final Commodities purchase = supply.buy(purchaseVolume, this.account.newCheque(expense));
			// totalExpense+=expense;
			stuff.put(purchase);
			requiredVolume -= purchaseVolume;
			if (requiredVolume == 0) {
				break;
			}
		}
		/*
		 * investmentCost=totalExpense; if (stuff.getValue() > 1.01 *
		 * investmentCost + 1 || stuff.getValue() < investmentCost) { throw new
		 * RuntimeException("Expense is " + stuff.getValue() + ", expected was "
		 * + investmentCost); }
		 */
		investmentCost = stuff.getValue();
		if (stuff.getVolume() != investmentSize * input) {
			throw new RuntimeException("Not enough stuff.");
		}
		final long investmentVolume = stuff.getVolume();
		final Machine[] newMachines = InvestorToolBox.getNewMachines(investmentSize, stuff, technology, timer, random);
		// TODO: vérifier que stuff a été consommé.
		this.factory.expandCapacity(newMachines);
		this.data.put("investment.size", investmentSize);
		this.data.put("investment.vol", investmentVolume);
		this.data.put("investment.val", investmentCost);

	}

	private void mutation(float mut) {
		//animalSpirit += mut * 0.05f * random.nextGaussian();
		this.capitalTargetRatio += mut * 0.05f * random.nextGaussian();
		if (this.capitalTargetRatio > 1) {
			this.capitalTargetRatio = 1f;
		} else if (this.capitalTargetRatio < 0) {
			this.capitalTargetRatio = 0f;
		}
	}

	@Override
	protected void closeManagers() {
		super.closeManagers();
		this.salesVolumeMemory.add((Long) this.salesManager.askFor("salesVolume"));
		this.salesValueMemory.add((Long) this.salesManager.askFor("salesValue"));
		this.wagebillMemory.add((Long) this.workforceManager.askFor("wagebill"));
		this.workforceMemory.add((Integer) this.workforceManager.askFor("workforce"));
		
		this.data.put("imitations",this.imitations);
		/*
		 * if (timer.getPeriod().intValue()>900 &&
		 * this.pricingManager.getPrice()<12000) {
		 * Jamel.println(name,this.pricingManager.getPrice()); // TODO: REMOVE
		 * ME (debugging) }
		 */
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
				return (long) ((assets) * capitalTargetRatio);
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
				return assets - getCapitalTarget();
			}

			/**
			 * Issues the specified number of new shares.
			 * 
			 * @param nShares
			 *            the number of new shares to be issued.
			 * @return a {@link StockCertificate} that encapsulates the new
			 *         shares.
			 */
			private StockCertificate getNewShares(Integer nShares) {
				checkConsistency();
				return this.capitalStock.issueNewShares(nShares);
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
					long val = factory.getValue() + account.getAmount();
					if (val < 0) {
						Jamel.println("factory.getValue()", factory.getValue());
						Jamel.println("account.getAmount()", account.getAmount());
						throw new RuntimeException("Assets are negative.");
					}
					result = val;
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
				imitation();
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

				this.dataset.put("cash", cash);
				this.dataset.put("assets", assets);
				this.dataset.put("liabilities", liabilities);
				this.dataset.put("capital", capital);

				this.dataset.put("debt.shortTerm", shortTermDebt);
				this.dataset.put("debt.longTerm", longTermDebt);

				this.dataset.put("dividends", this.dividend);
				this.dataset.put("interest", account.getInterest());

				this.dataset.put("liabilities.target", getLiabilitiesTarget());
				this.dataset.put("liabilities.excess", getLiabilitiesExcess());

				this.dataset.put("liabilities.new", account.getNewDebt());
				this.dataset.put("liabilities.repayment", account.getRepaidDebt());

				this.dataset.put("canceledDebts", account.getCanceledDebt());
				this.dataset.put("canceledDeposits", account.getCanceledMoney());

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
				return true; // FIXME LATER
				/*
				 * checkConsistency(); final boolean isConsistent; final long
				 * grossProfit = (Long) salesManager.askFor("grossProfit");
				 * final long interest = account.getInterest(); final long
				 * bankruptcy = account.getCanceledMoney() +
				 * factory.getInventoryLosses() - account.getCanceledDebt();
				 * final long capital = this.getCapital();
				 * 
				 * isConsistent = (capital == this.initialCapital + grossProfit
				 * - (this.dividend + interest + bankruptcy)); if
				 * (!isConsistent) { if
				 * (this.capitalStock.getDistributedDividends() !=
				 * this.dividend) { Jamel.println("distributed dividend = " +
				 * this.capitalStock.getDistributedDividends()); Jamel.println(
				 * "expected = " + this.dividend); // throw new
				 * RuntimeException("Inconsistency"); } Jamel.println(
				 * "capital = " + capital); Jamel.println("expected = " +
				 * (this.initialCapital + grossProfit -
				 * (this.capitalStock.getDistributedDividends() + interest +
				 * bankruptcy))); // throw new RuntimeException(
				 * "Inconsistency: " + // Firm150908.this.name); // FIXME }
				 * return isConsistent;
				 */
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
				final long capitalTarget2 = getCapitalTarget();
				final double averageIncome = this.income.getMean();
				long newDividend;
				if (capital > 0) {
					final double ratio = capital / capitalTarget2;
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
				this.dataset.put("payDividend.capitalTarget", capitalTarget2);
				this.dataset.put("payDividend.averageIncome", averageIncome);
				this.dataset.put("payDividend.dividend", dividend);

				this.dataset.put("debt2target.ratio", (account.getDebt()) / getLiabilitiesTarget());
			}

			@Override
			public void secureFinancing(long amount) {
				checkConsistency();
				if (amount > account.getAmount()) {
					account.newLoan(amount - account.getAmount(), 12, false);
					// TODO : 12 should be a parameter
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

	/**
	 * Returns a new {@link WorkforceManager}.
	 * 
	 * @return a new {@link WorkforceManager}.
	 */
	@Override
	protected WorkforceManager getNewWorkforceManager() {
		return new WorkforceManager("WorkforceManager", timer) {

			/** The wage offered. */
			private Double highWage = null;

			/** The job offer. */
			private JobOffer jobOffer = null;

			/** The wage offered. */
			private Double lowWage = null;

			/** The manpower target. */
			private Integer manpowerTarget = null;

			/** The payroll (= the anticipated wage bill) */
			private Long payroll = null;

			/** Memory of the recent job openings. */
			private final Memory<Integer> recentJobOpenings = new Memory<Integer>(3);

			/** Memory of the recent vacancies. */
			private final Memory<Integer> recentVacancies = new Memory<Integer>(3);

			/** Memory of the recent workforce target. */
			private final Memory<Integer> recentWorkforceTarget = new Memory<Integer>(3);

			/** Number of job vacancies in the current period. */
			private Integer vacancies = null;

			/** The wage offered. */
			private Double wage = null;

			/**
			 * The wagebill.
			 */
			private Long wagebill;

			/** The workforce. */
			private final Workforce workforce = new Workforce();

			/**
			 * Returns the vacancy rate.
			 * 
			 * @return the vacancy rate.
			 */
			private Double getVacancyRate() {
				final Double result;
				final double sumVacancies = recentVacancies.getSum();
				final double sumJobs = recentWorkforceTarget.getSum();
				if (sumJobs == 0) {
					result = null;
				} else {
					result = sumVacancies / sumJobs;
				}
				return result;
			}

			/**
			 * Creates a new job offer.
			 */
			private void newJobOffer() {
				if (vacancies < 0) {
					throw new RuntimeException("Negative number of vacancies");
				}
				if (vacancies == 0) {
					jobOffer = null;
				} else {
					final int validPeriod = timer.getPeriod().intValue();
					jobOffer = new JobOffer() {

						private final long jobWage = (long) Math.floor(wage);

						@Override
						public JobContract apply(final Worker worker) {
							timer.checkConsistency(validPeriod);
							if (!(vacancies > 0)) {
								throw new RuntimeException("No vacancy.");
							}
							vacancies--;

							final int term;
							final float min = sector.getParam(LABOUR_CONTRACT_MIN);
							final float max = sector.getParam(LABOUR_CONTRACT_MAX);
							if (max == min) {
								term = (int) min;
							} else {
								term = (int) (min + random.nextInt((int) (max - min)));
							}

							final JobContract jobContract = newJobContract(worker, jobWage, term, timer);
							workforce.add(jobContract);
							return jobContract;

						}

						@Override
						public Object getEmployerName() {
							timer.checkConsistency(validPeriod);
							return Firm150908.this.getName();
						}

						@Override
						public long getWage() {
							timer.checkConsistency(validPeriod);
							return jobWage;
						}
					};
				}
			}

			@Override
			public Object askFor(String key) {
				checkConsistency();
				final Object result;
				if ("wagebill".equals(key)) {
					result = this.wagebill;
				} else if ("workforce".equals(key)) {
					result = this.workforce.size();
				} else {
					result = null;
				}
				return result;
			}

			@Override
			public void close() {
				checkConsistency();
				recentVacancies.add(this.vacancies);
				this.dataset.put(JOB_VACANCIES, this.vacancies);
			}

			@Override
			public JobOffer getJobOffer() {
				checkConsistency();
				final JobOffer result;
				if (this.vacancies > 0) {
					result = this.jobOffer;
				} else {
					result = null;
				}
				return result;
			}

			@Override
			public LaborPower[] getLaborPowers() {
				checkConsistency();
				return this.workforce.getLaborPowers();
			}

			@Override
			public long getPayroll() {
				checkConsistency();
				return this.payroll;
			}

			@Override
			public void layoff() {
				checkConsistency();
				workforce.layoff();
			}

			@Override
			public void open() {
				super.open();
				this.jobOffer = null;
				this.manpowerTarget = null;
				this.payroll = null;
				this.vacancies = null;
				this.wagebill = null;
			}

			@Override
			public void payWorkers() {
				checkConsistency();
				this.wagebill = 0l;
				for (JobContract contract : workforce) {
					contract.payWage(account.newCheque(contract.getWage()));
					this.wagebill += contract.getWage();
				}
				this.dataset.put(WAGE_BILL, this.wagebill.doubleValue());
			}

			/**
			 * A trial-and-error procedure for wage adjusting.
			 */
			@Override
			public void updateWage() {
				checkConsistency();
				final StringBuilder info = new StringBuilder();
				final Double vacancyRate = getVacancyRate();
				dataset.put("vacancies.rate", vacancyRate);
				final Double vacancyRatio;
				if (vacancyRate != null) {
					vacancyRatio = getVacancyRate() / sector.getParam(NORMAL_VACANCY_RATE);
				} else {
					vacancyRatio = null;
				}
				if (this.wage == null) {
					info.append("Wage=null<br>");
					this.wage = sector.getRandomWage();
					if (this.wage == null) {
						this.wage = (double) sector.getParam(WAGE_INITIAL_VALUE);
					}
					this.highWage = this.wage * (1f + sector.getParam(WAGE_FLEX_UP));
					this.lowWage = this.wage * (1f - sector.getParam(WAGE_FLEX_DOWN));

				} else if (vacancyRatio == null) {
					info.append("Does nothing<br>");
				} else if (vacancyRatio < 1) {
					// On baisse le salaire
					info.append("Vacancy rate: low<br>Decision: down<br>");
					this.highWage = this.wage;
					this.wage = this.lowWage + random.nextFloat() * (this.wage - this.lowWage);
					this.lowWage = this.lowWage * (1f - sector.getParam(WAGE_FLEX_DOWN));
				} else {
					// On hausse le salaire
					info.append("Vacancy rate: high<br>Decision: up<br>");
					this.lowWage = this.wage;
					this.wage = this.wage + random.nextFloat() * (this.highWage - this.wage);
					this.highWage = this.highWage * (1f + sector.getParam(WAGE_FLEX_UP));
				}
				//info.append("New wage: " + wage + "<br>");
				dataset.putInfo("updateWage", info.toString());
				
				this.dataset.put(VACANCIES_RATE, vacancyRate);
				this.dataset.put(WAGES, wage);
				this.dataset.put("wage.low", lowWage);
				this.dataset.put("wage.high", highWage);
			}

			@Override
			public void updateWorkforce() {
				checkConsistency();
				workforce.cleanUp();
				manpowerTarget = Math.round(factory.getCapacity() * productionManager.getTarget());
				final int jobOpenings;
				if (manpowerTarget <= workforce.size()) {
					if (manpowerTarget < workforce.size()) {
						workforce.layoff(workforce.size() - manpowerTarget);
					}
					payroll = workforce.getPayroll();
					jobOpenings = manpowerTarget - workforce.size();
					if (jobOpenings != 0) {
						throw new RuntimeException("Inconsistency");
					}
				} else {
					jobOpenings = manpowerTarget - workforce.size();
					payroll = workforce.getPayroll() + jobOpenings * (long) ((double) this.wage);
				}
				this.dataset.put(JOB_OPENINGS, jobOpenings);
				this.recentJobOpenings.add(jobOpenings);
				this.recentWorkforceTarget.add(manpowerTarget);
				this.dataset.put(WORKFORCE_TARGET, manpowerTarget);
				this.vacancies = jobOpenings;
				this.newJobOffer();
			}

		};
	}

	@Override
	public Object askFor(String key) {
		final Object result;
		if ("capitalTargetRatio".equals(key)) {
			result = this.capitalTargetRatio;
		} else {
			result = super.askFor(key);
		}
		return result;
	}

	@Override
	public void open() {
		super.open();
		this.imitations=0;
	}
	
	@Override
	public void invest() {
		this.data.put("desinvestment.size", 0);
		final long capital = (Long) this.capitalManager.askFor("capital");
		final long capitalTarget2 = (Long) this.capitalManager.askFor("capitalTarget");
		this.data.put("demand12", salesVolumeMemory.getMean());

		final double workforce = this.workforceMemory.getSum();
		final double salesVolume = this.salesVolumeMemory.getSum();
		if (workforce > 0 && salesVolume > 0) {
			averagePrice = this.salesValueMemory.getSum() / salesVolume;
			averageWage = this.wagebillMemory.getSum() / workforce;
		}

		int investmentSize = 0;
		long investmentCost = 0;
		long investmentVolume = 0;
		this.data.put("investment.size", investmentSize);
		this.data.put("investment.vol", investmentVolume);
		this.data.put("investment.val", investmentCost);

		final float change = random.nextFloat();
		if (change > stability) {
			mutation(mutation);
		}

		final double anticipedDemand = this.salesVolumeMemory.getMean() * (1. + animalSpirit);// 1.1;
		this.data.put("demand.anticipated", anticipedDemand);

		if (averagePrice != null && averageWage != null) {

			// Il faut que la firme ait fonctionné au moins une fois au
			// cours des périodes récentes, pour qu'on puisse calculer un
			// prix moyen et un salaire moyen.

			final long[] machinery = (long[]) this.factory.askFor("machinery");

			if (this.factory.getCapacity() == 0 || capital > capitalTarget2) {

				// Il faut que le niveau de capital de la firme soit
				// satisfaisant
				// pour qu'on puisse envisager l'achat de nouvelles machines

				// On récupère une liste d'offres.
				final Supply[] supplies = getSupplies(10);

				if (supplies.length > 0) {

					// Il faut qu'il y ait au moins 1 offre de 'raw
					// materials'.

					// final long input =
					// technology.getInputVolumeForANewMachine();

					final Long[] machinePrices = InvestorToolBox.getPrices(supplies,
							technology.getInputVolumeForANewMachine());

					// TODO: Il faudrait demander à la banque son taux +
					// tenir
					// compte de l'inflation + aversion au risque
					if (machinePrices.length == 1) {
						investmentSize = 0;
					} else if (this.factory.getCapacity() == 0) {
						imitation();
						investmentSize = 1;
					} else {
						final int time = 120;
						investmentSize = InvestorToolBox.getOptimumSize(machinePrices, technology.getProductivity(),
								machinery, anticipedDemand, averagePrice, averageWage, discountRate, time);
					}

					if (investmentSize > 0) {
						invest(investmentSize, machinePrices, technology.getInputVolumeForANewMachine(), supplies);
					} /*
					 * else if (investmentSize < 0) {
					 * this.factory.scrap(-investmentSize);
					 * this.data.put("desinvestment.size", -investmentSize);
					 * investmentSize = 0; // TODO: bidouillage to be
					 * removed. }
					 */

				}

			}
		}
		//this.data.put("animalSpirit", animalSpirit);
		//this.data.put("animalSpirit.weighted", animalSpirit * (Integer) this.factory.askFor("capacity"));
		this.data.put("capitalTargetRatio", capitalTargetRatio);
		this.data.put("capitalTargetRatio.weighted", capitalTargetRatio * (Integer) this.factory.askFor("capacity"));
		
		this.data.put("targetDebtRatio", 1f-capitalTargetRatio);
		this.data.put("targetDebtRatio.weighted", (1f-capitalTargetRatio) * (Integer) this.factory.askFor("capacity"));
	}

}

// ***
