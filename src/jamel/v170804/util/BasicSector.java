package jamel.v170804.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.util.Parameters;
import jamel.util.Phase;
import jamel.util.Sector;
import jamel.util.Simulation;
import jamel.v170804.data.DataManager;
import jamel.util.Agent;
import jamel.util.Expression;
import jamel.util.JamelObject;

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
			throw new RuntimeException("Something went wrong while creating a new agent.", e);
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
	final private DataManager dataManager;

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
	private Map<String, Phase> phases = new HashMap<>();

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

		// Inits the type of the agents.

		{
			final String agentClassName = this.params.getAttribute("agentClassName");
			if (agentClassName.isEmpty()) {
				throw new RuntimeException("Sector \'" + this.name + "\': missing or empty attribute: agentClassName");
			}
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

		this.dataManager = new DataManager(this.agents, this);
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
		super.close();
		for (int i = 0; i < this.agents.size(); i++) {
			this.agents.get(i).close();
		}
	}

	@Override
	public void doEvent(Parameters event) {
		Jamel.notYetImplemented("Not yet implemented: " + event.getName());
	}

	@Override
	public Expression getDataAccess(String agentName, String[] args) {
		return this.dataManager.getDataAccess(agentName, args);
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
	public String getName() {
		return this.name;
	}

	@Override
	public Parameters getParameters() {
		return this.params.get("parameters");
	}

	@Override
	public Phase getPhase(final String phaseName, final String[] options) {

		final Set<String> set = new HashSet<>();
		Collections.addAll(set, options);

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

				if (set.contains("shuffle")) {
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
	public void open() {
		super.open();
		for (int i = 0; i < this.agents.size(); i++) {
			this.agents.get(i).open();
		}
	}

	@Override
	public Agent[] select(int n) {
		Agent[] result = new Agent[n];
		for (int i = 0; i < n; i++) {
			result[i] = this.agents.get(this.getRandom().nextInt(this.agents.size()));
			for (int j = 0; j < i - 1; j++) {
				// Si l'agent est déjà dans la sélection, on l'efface.
				if (result[j] == result[i]) {
					result[i] = null;
				}
			}
		}
		return result;
	}

}
