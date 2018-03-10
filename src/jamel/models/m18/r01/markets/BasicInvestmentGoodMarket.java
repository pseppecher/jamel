package jamel.models.m18.r01.markets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jamel.Jamel;
import jamel.data.DynamicSeries;
import jamel.data.Expression;
import jamel.data.SectorDataManager;
import jamel.models.m18.r01.firms.Firm;
import jamel.models.m18.r01.households.Household;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Phase;
import jamel.util.Sector;
import jamel.util.Simulation;

/**
 * Represent the investment good market.
 * 
 * 2018-03-10: jamel/models/m18/r01/markets/BasicInvestmentGoodMarket.java
 *  
 * 2018-03-10: jamel/models/m18/r01/markets/BasicConsumptionGoodMarket.java 
 * 
 * 2018-03-02: permet simplement de brasser des consommateurs (de type
 * Household) provenant de secteurs différents (par exemple Workers et
 * Sharholders) de façon à ce qu'il n'y ait pas d'ordre de préférence entre eux
 * lors de l'accès au marché.
 */
public class BasicInvestmentGoodMarket extends JamelObject implements Sector {

	/**
	 * The data manager.
	 */
	final private SectorDataManager dataManager;

	/**
	 * The collection of firms that populate this sector (demand side).
	 */
	final private List<Firm> firms;

	/**
	 * The name of the sector.
	 */
	final private String name;

	/**
	 * The parameters of the sector.
	 */
	final private Parameters params;

	/**
	 * The list of the phases, accessible by their names.
	 */
	final private Map<String, Phase> phases = new HashMap<>();

	/**
	 * Creates a new basic sector.
	 * 
	 * @param params
	 *            the parameters of the sector.
	 * 
	 * @param simulation
	 *            the parent simulation.
	 */
	@SuppressWarnings("unchecked")
	public BasicInvestmentGoodMarket(final Parameters params, final Simulation simulation) {
		super(simulation);
		this.params = params;
		this.name = this.params.getAttribute("name");

		this.firms = new ArrayList<>();

		this.dataManager = new SectorDataManager(this.firms, this);

		final String firmSectorsParameter = this.params.getString("parameters.firms");
		if (firmSectorsParameter == null || firmSectorsParameter.isEmpty()) {
			throw new RuntimeException("Missing or empty firms attribute.");
		}
		for (final String firmSectorName : firmSectorsParameter.split(",")) {
			this.firms
					.addAll((List<Firm>) this.getSimulation().getSector(firmSectorName.trim()).selectAll());
		}

	}

	/**
	 * Returns an expression that gives access to some sectoral data.
	 * 
	 * @param arg
	 *            the argument of the expression to be returned.
	 * @return an expression that gives access to some sectoral data.
	 */
	private Expression getSectorDataAccess(String arg) {
		final Expression result;
		final String[] args = arg.split("\\.");
		if (args[0].equals("phase") && args[2].equals("runtime")) {
			final Phase phase = this.phases.get(args[1]);
			result = new Expression() {

				@Override
				public Double getValue() {
					return (double) phase.getRuntime();
				}

				@Override
				public String toString() {
					return name + ".phase." + args[1] + ".runtime";
				}

			};
		} else {
			throw new RuntimeException("Bad key: '" + args[0] + "' in '" + arg + "'");
		}
		return result;
	}

	@Override
	public void close() {
		// Does nothing.
	}

	@Override
	public void doEvent(Parameters event) {
		final String criteria = event.getAttribute("select");
		for (Agent agent : this.firms) {
			if (criteria.isEmpty() || criteria.equals("*") || agent.satisfy(criteria)) {
				agent.doEvent(event);
			}
		}
	}

	@Override
	public Class<? extends Agent> getAgentClass() {
		return Household.class;
	}

	@Override
	public Expression getDataAccess(String[] args) {
		final Expression result;
		if (args.length == 1) {
			result = this.getSectorDataAccess(args[0]);
		} else {
			result = this.dataManager.getDataAccess(args);
		}
		return result;
	}

	@Override
	public Expression getIndividualDataAccess(String agentName, String[] args) {
		return this.dataManager.getDataAccess(agentName, args);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Parameters getParameters() {
		return this.params.get("parameters");
	}

	@Override
	public Phase getPhase(final String phaseName, final boolean shuffle) {

		if (phaseName == null) {
			throw new RuntimeException("Phase name is null");
		}

		if (!phaseName.equals("matching")) {
			throw new RuntimeException("Bad phase name: '" + phaseName + "'");
		}

		final Phase result = new Phase() {

			private long runtime = 0;

			@Override
			public String getName() {
				return phaseName;
			}

			@Override
			public long getRuntime() {
				return this.runtime;
			}

			@Override
			public Sector getSector() {
				return BasicInvestmentGoodMarket.this;
			}

			@Override
			public void run() {

				final long start = System.currentTimeMillis();

				if (shuffle) {
					Collections.shuffle(firms, getRandom());
				}

				for (int i = 0; i < firms.size(); i++) {
					firms.get(i).invest();
				}

				final long end = System.currentTimeMillis();

				runtime += end - start;

			}

		};

		final Phase previousValue = this.phases.put(phaseName, result);
		if (previousValue != null) {
			throw new RuntimeException(
					"The sector " + name + " alreday contains a phase with the name '" + phaseName + "'");
		}

		return result;

	}

	@Override
	public DynamicSeries getScatterSeries(final String xKey, final String yKey, Expression[] conditions,
			String selection) {
		return this.dataManager.getScatterSeries(xKey, yKey, conditions, selection);
	}

	@Override
	public void open() {
		// Does nothing.
	}

	@Override
	public Agent select() {
		Agent result = this.firms.get(this.getRandom().nextInt(this.firms.size()));
		return result;
	}

	@Override
	public Agent[] select(int n, Agent special) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public List<? extends Agent> selectAll() {
		return new LinkedList<>(this.firms);
	}

	@Override
	public Agent[] selectArray(final int n) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public List<? extends Agent> selectList(int n) {
		// TODO verifier cette méthode :
		// 1/ what if n>agents.size() ?
		// 2/ performance
		if (n > this.firms.size()) {
			throw new RuntimeException("n>this.agents.size()");
		}
		final List<Agent> result = new LinkedList<>();
		for (int i = 0; i < n; i++) {
			Agent agent = this.firms.get(this.getRandom().nextInt(this.firms.size()));
			if (result.contains(agent)) {
				i--;
			} else {
				result.add(agent);
			}
		}
		return result;
	}

}
