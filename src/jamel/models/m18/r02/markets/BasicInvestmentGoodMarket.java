package jamel.models.m18.r02.markets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jamel.Jamel;
import jamel.data.DynamicSeries;
import jamel.data.Expression;
import jamel.models.m18.r02.firms.Firm;
import jamel.models.m18.r02.households.Household;
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
	 * The customer sectors.
	 */
	private List<Sector> customerSectors = new LinkedList<>();

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
	public BasicInvestmentGoodMarket(final Parameters params, final Simulation simulation) {
		super(simulation);
		this.params = params;
		this.name = this.params.getAttribute("name");

		final String firmSectorsParameter = this.params.getString("parameters.firms");
		if (firmSectorsParameter == null || firmSectorsParameter.isEmpty()) {
			throw new RuntimeException("Missing or empty firms attribute.");
		}
		for (final String firmSectorName : firmSectorsParameter.split(",")) {
			this.customerSectors.add(this.getSimulation().getSector(firmSectorName.trim()));
		}

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
		Jamel.notUsed();
		return null;
	}

	@Override
	public Expression getIndividualDataAccess(String agentName, String[] args) {
		Jamel.notUsed();
		return null;
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

				final List<Agent> firms = new ArrayList<>();

				for (Sector sector : customerSectors) {
					firms.addAll(sector.selectAll());
				}

				if (shuffle) {
					Collections.shuffle(firms, getRandom());
				}

				for (int i = 0; i < firms.size(); i++) {
					((Firm) firms.get(i)).invest();
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
		Jamel.notUsed();
		return null;
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
