package jamel.models.modelJEE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.Expression;
import jamel.data.SectorDataManager;
import jamel.gui.DynamicXYSeries;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Phase;
import jamel.util.Sector;
import jamel.util.Simulation;

/**
 * A basic sector.
 */
@SuppressWarnings("javadoc")
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
			final Method getActionMethod = agentClass.getMethod("getAction", String.class);
			action = (Consumer<? super Agent>) getActionMethod.invoke(null, phaseName);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException("Something went wrong while creating the action \"" + phaseName
					+ "\" for the agent \"" + agentClass.getName() + "\".", e);
		}
		return action;
	}

	/**
	 * The class of the agents that populate the sector.
	 */
	final private Class<? extends Agent> agentClass;

	/** The collection of agent. */
	private final BasicAgentSet<Agent> agentSet;

	/**
	 * To count the number of agents created since the start of the simulation.
	 */
	private int countAgents;

	/**
	 * The data manager.
	 */
	final private SectorDataManager dataManager;

	final private String name;

	/**
	 * The parameters of the sector.
	 */
	final private Parameters parameters;

	/**
	 * Creates a new basic sector.
	 * 
	 * @param parameters
	 *            the parameters of the sector.
	 * 
	 * @param simulation
	 *            the parent simulation.
	 */
	public BasicSector(final Parameters parameters, final Simulation simulation) {
		super(simulation);
		this.agentSet = new BasicAgentSet<>(this.getRandom());
		this.parameters = parameters;
		this.name = this.parameters.getAttribute("name");
		this.agentClass = getClass(parameters.getAttribute("agent"));
		this.dataManager = new SectorDataManager(this);
	}

	/**
	 * Creates firms.
	 * 
	 * @param klass
	 *            the type of agent to create.
	 * @param lim
	 *            the number of agents to create.
	 * @return a list containing the new agents.
	 */
	final private List<Agent> createAgents(Class<? extends Agent> klass, int lim) {
		final List<Agent> result = new ArrayList<>(lim);
		try {
			for (int index = 0; index < lim; index++) {
				this.countAgents++;
				final String firmName = "Firm" + this.countAgents;
				final Agent firm = klass.getConstructor(String.class, Sector.class).newInstance(firmName, this);
				result.add(firm);
			}
		} catch (Exception e) {
			throw new RuntimeException("Firm creation failure", e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	final private Class<? extends Agent> getClass(final String agentClassName) {
		final Class<? extends Agent> result;
		try {
			final Class<?> klass = Class.forName(agentClassName);
			if (!Agent.class.isAssignableFrom(klass)) {
				throw new RuntimeException("Agent class is not assignable from " + klass.getName());
				// TODO c'est une erreur du scénario : à traiter comme
				// telle. Balancer un message d'erreur à la GUI qui display
				// une box.
			}
			result = (Class<? extends Agent>) klass;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Something went wrong while creating the sector \'" + this.getName() + "\'", e);
		}
		return result;
	}

	@Override
	public void close() {
		Jamel.notUsed();
	}

	@Override
	final public void doEvent(Parameters event) {
		final String eventType = event.getAttribute("event");
		if (eventType.equals("Populate")) {
			final int newFirms = Integer.parseInt(event.getAttribute("size"));
			final List<Agent> list = this.createAgents(this.agentClass, newFirms);
			this.agentSet.putAll(list);
			this.dataManager.put(list);
		} else {
			throw new RuntimeException("Unknown event or not yet implemented: " + event.getName() + ", " + eventType);
		}
	}

	@Override
	final public Class<? extends Agent> getAgentClass() {
		return this.agentClass;
	}

	@Override
	final public Expression getDataAccess(String[] args) {
		return this.dataManager.getDataAccess(args);
	}

	@Override
	final public Expression getIndividualDataAccess(String agentName, String[] args) {
		Jamel.notYetImplemented();
		return null;
	}

	/**
	 * Returns the sector name.
	 * 
	 * @return the sector name.
	 */
	@Override
	final public String getName() {
		return name;
	}

	@Override
	final public Parameters getParameters() {
		return this.parameters.get("settings");
	}

	@Override
	final public Phase getPhase(String phaseName, final boolean shuffle) {

		if (phaseName == null) {
			throw new RuntimeException("Phase name is null");
		}
		final Consumer<? super Agent> action = getAction(phaseName, agentClass);

		final Phase result = new Phase() {

			@Override
			public String getName() {
				return phaseName;
			}

			@Override
			public long getRuntime() {
				Jamel.notYetImplemented();
				return 0;
			}

			@Override
			public Sector getSector() {
				return BasicSector.this;
			}

			@Override
			public void run() {
				if (shuffle) {
					BasicSector.this.agentSet.getShuffledList().forEach(action);
				} else {
					BasicSector.this.agentSet.getList().forEach(action);
				}

			}

		};

		return result;

	}

	@Override
	public DynamicXYSeries getScatterSeries(String xKey, String yKey, Expression[] conditions, String selection) {
		return this.dataManager.getScatterSeries(xKey, yKey, conditions, selection);
	}

	@Override
	public void open() {
		Jamel.notUsed();
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
	final public List<? extends Agent> selectAll() {
		final List<? extends Agent> result;
		if (agentSet.size() > 1) {
			result = agentSet.getShuffledList();
		} else {
			result = agentSet.getList();
		}
		return result;
	}

	@Override
	public Agent[] selectArray(int n) {
		Jamel.notUsed();
		return null;
	}

	@Override
	final public List<? extends Agent> selectList(int n) {
		final List<? extends Agent> result;
		if (agentSet.size() > 1) {
			result = agentSet.select(n);
		} else {
			result = agentSet.getList();
		}
		return result;
	}

}
