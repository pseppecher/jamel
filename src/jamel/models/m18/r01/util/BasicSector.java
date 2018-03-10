package jamel.models.m18.r01.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.DynamicSeries;
import jamel.data.Expression;
import jamel.data.SectorDataManager;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Phase;
import jamel.util.Sector;
import jamel.util.Simulation;

/**
 * A basic sector.
 */
public class BasicSector extends JamelObject implements Sector {

	/**
	 * Returns the specified action.
	 * 
	 * @param phaseName
	 *            the name of the phase.
	 * @param agentClass
	 *            the targeted Class of agents.
	 * 
	 * @return the specified action.
	 */
	@SuppressWarnings("unchecked")
	private static Consumer<? super Agent> getAction(String phaseName, Class<? extends Agent> agentClass) {
		final Consumer<? super Agent> action;
		try {
			final Method getPhaseMethod = agentClass.getMethod("getAction", String.class);
			action = (Consumer<? super Agent>) getPhaseMethod.invoke(null, phaseName);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException("Something went wrong while creating the action phase \"" + phaseName
					+ "\" for the agent \"" + agentClass.getName() + "\".", e);
		}
		return action;
	}

	/**
	 * Creates and returns a new agent.
	 * 
	 * @param sector
	 *            the sector.
	 * @param agentClass
	 *            the agent class.
	 * 
	 * @param id
	 *            the id of the agent to be created.
	 * @return a new agent.
	 */
	private static Agent getNewAgent(final Sector sector, Class<? extends Agent> agentClass, final int id) {
		final Agent result;
		try {
			result = agentClass.getConstructor(Sector.class, int.class).newInstance(sector, id);
		} catch (Exception e) {
			final String message = "Something went wrong while creating a new agent.";
			Jamel.println("***");
			Jamel.println(message);
			Jamel.println("sector.getName(): " + sector.getName());
			Jamel.println("agentClass.getName(): " + agentClass.getName());
			Jamel.println("id: " + id);
			Jamel.println();
			throw new RuntimeException(message, e);
		}
		return result;
	}

	/**
	 * The class of the agents that populate the sector.
	 */
	final private Class<? extends Agent> agentClass;

	/**
	 * The collection of agents that populate this sector.
	 */
	final private List<Agent> agents;

	/**
	 * The data manager.
	 */
	final private SectorDataManager dataManager;

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
	public BasicSector(final Parameters params, final Simulation simulation) {
		super(simulation);
		this.params = params;
		this.name = this.params.getAttribute("name");

		// Initializes the type of the agents.

		{
			final String agentClassName;
			final String attributeAgentClassName = this.params.getAttribute("agentClassName");
			if (attributeAgentClassName.isEmpty()) {
				throw new RuntimeException("Sector \'" + this.name + "\': missing or empty attribute: agentClassName");
			}
			agentClassName = attributeAgentClassName;
			try {
				final Class<?> klass = Class.forName(agentClassName);
				if (!Agent.class.isAssignableFrom(klass)) {
					throw new RuntimeException("Agent class is not assignable from " + klass.getName());
					// TODO c'est une erreur du scénario : à traiter comme
					// telle. Balancer un message d'erreur à la GUI qui display
					// une box.
				}
				@SuppressWarnings("unchecked")
				final Class<? extends Agent> klass2 = (Class<? extends Agent>) klass;
				this.agentClass = klass2;
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Something went wrong while creating the sector \'" + this.name + "\'", e);
			}
		}

		// Looks for the initial number of agents.

		{

			final String initialPopulationString = this.params.getAttribute("initialPopulation");
			final int initialPopulation;
			if (initialPopulationString.isEmpty()) {
				initialPopulation = 0;
			} else {
				initialPopulation = Integer.parseInt(initialPopulationString);
			}

			this.agents = new ArrayList<>(initialPopulation);
			for (int i = 0; i < initialPopulation; i++) {
				this.agents.add(getNewAgent(this, agentClass, i));
			}

		}

		this.dataManager = new SectorDataManager(this.agents, this);
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
		for (int i = 0; i < this.agents.size(); i++) {
			this.agents.get(i).close();
		}
	}

	@Override
	public void doEvent(Parameters event) {
		final String criteria = event.getAttribute("select");
		for (Agent agent : this.agents) {
			if (criteria.isEmpty() || criteria.equals("*") || agent.satisfy(criteria)) {
				agent.doEvent(event);
			}
		}
	}

	@Override
	public Class<? extends Agent> getAgentClass() {
		return this.agentClass;
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
		final Consumer<? super Agent> action = getAction(phaseName, agentClass);

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
				return BasicSector.this;
			}

			@Override
			public void run() {

				final long start = System.currentTimeMillis();

				if (shuffle) {
					Collections.shuffle(BasicSector.this.agents, BasicSector.this.getRandom());
				}

				BasicSector.this.agents.forEach(action);

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
		for (int i = 0; i < this.agents.size(); i++) {
			this.agents.get(i).open();
		}
	}

	@Override
	public Agent select() {
		Agent result = this.agents.get(this.getRandom().nextInt(this.agents.size()));
		return result;
	}

	@Override
	public Agent[] select(int n, Agent special) {
		final Agent[] result = (Agent[]) Array.newInstance(this.agentClass, n);
		int i = 0;
		while (true) {
			// TODO vérifier que la collection est assez grande pour qu'il y ait
			// une sortie ou prévoir un coupe-circuit.
			Agent newAgent = this.agents.get(this.getRandom().nextInt(this.agents.size()));
			if (this == newAgent) {
				newAgent = null;
			} else {
				for (int j = 0; j < i - 1; j++) {
					// Si l'agent est déjà dans la sélection, on l'efface.
					if (result[j] == newAgent) {
						newAgent = null;
						break;
					}
				}
			}
			if (newAgent != null) {
				result[i] = newAgent;
				i++;
				if (i == result.length) {
					break;
				}
			}
		}
		return result;
	}

	@Override
	public List<? extends Agent> selectAll() {
		return new LinkedList<>(this.agents);
	}

	@Override
	public Agent[] selectArray(final int n) {
		// TODO revoir cette méthode, s'inspirer de la suivante + coupe circuit.
		final Agent[] result = (Agent[]) Array.newInstance(this.agentClass, n);
		for (int i = 0; i < n; i++) {
			result[i] = this.agents.get(this.getRandom().nextInt(this.agents.size()));
			for (int j = 0; j < i; j++) {
				// Si l'agent est déjà dans la sélection, on l'efface.
				if (result[j] == result[i]) {
					result[i] = null;
					i--;
					break;
				}
			}
		}
		return result;
	}

	@Override
	public List<? extends Agent> selectList(int n) {
		// TODO verifier cette méthode :
		// 1/ what if n>agents.size() ?
		// 2/ performance
		if (n > this.agents.size()) {
			throw new RuntimeException("n>this.agents.size()");
		}
		final List<Agent> result = new LinkedList<>();
		for (int i = 0; i < n; i++) {
			Agent agent = this.agents.get(this.getRandom().nextInt(this.agents.size()));
			if (result.contains(agent)) {
				i--;
			} else {
				result.add(agent);
			}
		}
		return result;
	}

}
