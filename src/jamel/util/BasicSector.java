package jamel.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jamel.Jamel;
import jamel.data.DataManager;
import jamel.data.Expression;

/**
 * A basic sector.
 */
public class BasicSector extends JamelObject implements Sector {

	/**
	 * The class of the agents that populate the sector.
	 */
	final private Class<? extends Agent> agentClass;

	/**
	 * The collection of agents that populate this sector.
	 */
	final private List<Agent> agents = new LinkedList<>();

	/**
	 * The number of agents created.
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
	 * The data manager.
	 */
	final private DataManager dataManager;

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
		this.dataManager=new DataManager(agents,this);

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
					// telle.
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
			this.agents.addAll(getNewAgents(initialPopulation));
		}

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
	public Expression getDataAccess(String[] args) {
		return this.dataManager.getDataAccess(args);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Phase getPhase(final String phaseName) {
		if (phaseName == null) {
			throw new RuntimeException("Phase name is null");
		}
		final Method phaseMethod;
		try {
			final Method getPhaseMethod = agentClass.getMethod("getPhaseMethod", String.class);
			phaseMethod = (Method) getPhaseMethod.invoke(null, phaseName);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException("Something went wrong", e);
		}

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
				for (final Agent agent : BasicSector.this.agents) {
					try {
						phaseMethod.invoke(agent);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException("Something went wrong while running the phase \"" + phaseName
								+ "\" for the sector \"" + BasicSector.this.name, e);
					}
				}
				Jamel.println(BasicSector.this.name, this.getName());
			}

		};

		return result;

	}

}
