package jamel.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jamel.data.DataManager;
import jamel.data.Expression;

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
	 * To count the agent creation since the start.
	 */
	private int nAgent = 0;

	/**
	 * The name of the sector.
	 */
	final private String name;

	/**
	 * The specification of the sector.
	 */
	final private Element specification;

	/**
	 * Creates a new basic sector.
	 * 
	 * @param specification
	 *            an XML element that contains the specification of the sector.
	 * 
	 * @param simulation
	 *            the parent simulation.
	 */
	public BasicSector(final Element specification, final Simulation simulation) {
		super(simulation);
		this.specification = specification;
		this.name = this.specification.getAttribute("name");

		// Inits the type of the agents.

		{
			final NodeList nodeList = this.specification.getElementsByTagName("agentClassName");
			if (nodeList.getLength() == 0) {
				throw new RuntimeException("Missing tag : agentClassName");
			}
			final String agentClassName = nodeList.item(0).getTextContent().trim();
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
			final NodeList nodeList = this.specification.getElementsByTagName("initialPopulation");
			final int initialPopulation;
			if (nodeList.getLength() == 0) {
				initialPopulation = 0;
			} else {
				initialPopulation = Integer.parseInt(nodeList.item(0).getTextContent().trim());
			}
			this.agents = new ArrayList<>(getNewAgents(initialPopulation));
		}

		this.dataManager = new DataManager(this.agents, this);
	}

	/**
	 * Creates and returns a collection of new agents.
	 * 
	 * @param number
	 *            the number of agents to be created.
	 * @return a collection of new agents.
	 */
	private Collection<Agent> getNewAgents(final int number) {
		final Collection<Agent> result = new LinkedList<>();
		for (int i = 0; i < number; i++) {
			try {
				result.add(agentClass.getConstructor(Sector.class, int.class).newInstance(this, this.nAgent));
				this.nAgent++;
			} catch (Exception e) {
				throw new RuntimeException("Something went wrong while creating a new agent.", e);
			}
		}
		return result;
	}

	@Override
	public void doEvent(Element event) {
		throw new RuntimeException("Not yet implemented: " + event.getTagName());
	}

	@Override
	public Expression getDataAccess(String[] args) {
		return this.dataManager.getDataAccess(args);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Phase getPhase(final String phaseName, final boolean shuffle) {
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
			public Sector getSector() {
				return BasicSector.this;
			}

			@Override
			public void run() {

				if (shuffle) {
					Collections.shuffle(BasicSector.this.agents, BasicSector.this.getRandom());
				}

				BasicSector.this.agents.forEach(action);

			}

		};

		return result;

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
