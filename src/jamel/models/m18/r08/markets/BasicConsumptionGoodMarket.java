package jamel.models.m18.r08.markets;

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
import jamel.models.m18.r08.households.Household;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Phase;
import jamel.util.Sector;
import jamel.util.Simulation;

/**
 * Represent the consumption good market.
 * 
 * 2018-03-10: jamel/models/m18/r01/markets/BasicConsumptionGoodMarket.java
 * 
 * 2018-03-02: permet simplement de brasser des consommateurs (de type
 * Household) provenant de secteurs différents (par exemple Workers et
 * Sharholders) de façon à ce qu'il n'y ait pas d'ordre de préférence entre eux
 * lors de l'accès au marché.
 */
public class BasicConsumptionGoodMarket extends JamelObject implements Sector {

	/**
	 * The data manager.
	 */
	final private SectorDataManager dataManager;

	/**
	 * The collection of household that populate this sector.
	 */
	final private List<Sector> consumers = new LinkedList<>();

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
	public BasicConsumptionGoodMarket(final Parameters params, final Simulation simulation) {
		super(simulation);
		this.params = params;
		this.name = this.params.getAttribute("name");

		this.dataManager = null;

		final String householdSectorsParameter = this.params.getString("parameters.households");
		if (householdSectorsParameter == null || householdSectorsParameter.isEmpty()) {
			throw new RuntimeException("Missing or empty employers attribute.");
		}
		for (final String householdSectorName : householdSectorsParameter.split(",")) {
			this.consumers.add(this.getSimulation().getSector(householdSectorName.trim()));
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
		Jamel.notUsed();
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

		if (!phaseName.equals("consumption")) {
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
				return BasicConsumptionGoodMarket.this;
			}

			@Override
			public void run() {

				final long start = System.currentTimeMillis();

				final List<Agent> households = new ArrayList<>();

				for (Sector sector : consumers) {
					households.addAll(sector.selectAll());
				}

				if (shuffle) {
					Collections.shuffle(households, getRandom());
				}

				for (int i = 0; i < households.size(); i++) {
					((Household) households.get(i)).consumption();
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
		Jamel.notUsed();
		return null;
	}

	@Override
	public Agent[] select(int n, Agent special) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public List<? extends Agent> selectAll() {
		Jamel.notUsed();
		return null;
	}

	@Override
	public Agent[] selectArray(final int n) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public List<? extends Agent> selectList(int n) {
		Jamel.notUsed();
		return null;
	}

}
