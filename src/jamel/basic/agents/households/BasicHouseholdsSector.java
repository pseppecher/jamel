package jamel.basic.agents.households;

import jamel.Simulator;
import jamel.basic.agents.util.AgentSet;
import jamel.basic.agents.util.BasicAgentSet;
import jamel.basic.agents.util.Parameters;
import jamel.basic.util.BankAccount;
import jamel.basic.util.JobOffer;
import jamel.basic.util.Supply;
import jamel.util.Circuit;
import jamel.util.Sector;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A basic households sector.
 */
public class BasicHouseholdsSector implements Sector, HouseholdsSector {

	/**
	 * Enumeration of the keys of the input messages handled by this sector.
	 */
	private static class KEY {

		@SuppressWarnings("javadoc")
		public static final String putData = "putData";

	}

	/**
	 * The keys of the parameters of the sector.
	 */
	protected static class PARAM {

		/** The key for the type of agents to create*/
		public static final String HOUSEHOLDS_TYPE = "agents.type";

	}

	/** The parameters of the agents. */
	private final Parameters parameters;

	/** The collection of agents. */
	protected final AgentSet<Household> agents;

	/** The circuit. */
	protected final Circuit circuit;

	/** The agent counter. */
	protected int countAgents;

	/** The sector name. */
	protected final String name;

	/**
	 * Creates a new sector for households.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public BasicHouseholdsSector(String name, Circuit circuit) {
		this.circuit = circuit;
		this.name = name;
		this.agents = new BasicAgentSet<Household>();
		this.parameters = new Parameters(name,circuit);
	}

	/**
	 * Closes the sector at the end of the period.
	 */
	private void close() {
		for (final Household household:this.agents.getList()) {
			household.close();
		}
		this.circuit.forward(KEY.putData,this.name,this.agents.collectData());
	}

	/**
	 * Creates households.
	 * @param type the type of households to create.
	 * @param lim the number of households to create.
	 * @return a list containing the new households.
	 */
	protected List<Household> createHousholds(String type, int lim) {
		final String errorMsg = "BasicHousholdsSector: error while creating household";
		final List<Household> list = new ArrayList<Household>(lim);
		for(int index=0;index<lim;index++) {
			this.countAgents++;
			final String name = this.name+"-"+this.countAgents;
			try {
				list.add((Household) Class.forName(type,false,ClassLoader.getSystemClassLoader()).getConstructor(String.class,HouseholdsSector.class).newInstance(name,this));
			} catch (IllegalArgumentException e) {
				Simulator.showErrorDialog(errorMsg);
				e.printStackTrace();
				throw new RuntimeException(errorMsg);
			} catch (SecurityException e) {
				Simulator.showErrorDialog(errorMsg);
				e.printStackTrace();
				throw new RuntimeException(errorMsg);
			} catch (InstantiationException e) {
				Simulator.showErrorDialog(errorMsg);
				e.printStackTrace();
				throw new RuntimeException(errorMsg);
			} catch (IllegalAccessException e) {
				Simulator.showErrorDialog(errorMsg);
				e.printStackTrace();
				throw new RuntimeException(errorMsg);
			} catch (InvocationTargetException e) {
				Simulator.showErrorDialog(errorMsg);
				e.printStackTrace();
				throw new RuntimeException(errorMsg);
			} catch (NoSuchMethodException e) {
				Simulator.showErrorDialog(errorMsg);
				e.printStackTrace();
				throw new RuntimeException(errorMsg);
			} catch (ClassNotFoundException e) {
				Simulator.showErrorDialog(errorMsg);
				e.printStackTrace();
				throw new RuntimeException(errorMsg);
			}
		}
		return list;
	}

	@Override
	public boolean doPhase(String phaseName) {

		if (phaseName.equals("opening")) {
			for (final Household household:agents.getList()) {
				household.open();
			}
		} 

		else if (phaseName.equals("job_search")) {
			for (final Household household:agents.getShuffledList()) {
				household.jobSearch();
			}
		}

		else if (phaseName.equals("consumption")) {
			for (final Household household:agents.getShuffledList()) {
				household.consumption();
			}
		}

		else if (phaseName.equals("closure")) {
			this.close();
		}

		else {
			throw new IllegalArgumentException("Unknown phase <"+phaseName+">");			
		}

		return true;
	}

	@Override
	public Object forward(String request, Object ... args) {

		final Object result;

		if (request.equals("getRandomHousehold")) {
			result = agents.getRandomAgent();
		}

		else if (request.equals("getRandomHouseholds")) {
			result = agents.getSimpleRandomSample((Integer) args[0]);
		}

		else if (request.equals("change in parameters")) {
			this.parameters.update();
			result = null;
		}

		else if (request.equals("new")) {
			this.agents.putAll(this.createHousholds(this.circuit.getParameter(this.name,PARAM.HOUSEHOLDS_TYPE),Integer.parseInt((String) args[0])));
			result = null;
		}

		else {
			throw new IllegalArgumentException(this.name+": Unknown request <"+request+">");
		}

		return result;

	}

	@Override
	public float getFloatParameter(String key) {
		return this.parameters.get(key);
	}

	@Override
	public JobOffer[] getJobOffers(int i) {
		return (JobOffer[]) circuit.forward("getJobOffers", i);			
	}

	/**
	 * Returns the sector name.
	 * @return the sector name.
	 */
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public BankAccount getNewAccount(Household household) {
		return (BankAccount) circuit.forward("getNewAccount", household);
	}

	@Override
	public String getStringParameter(String key) {
		return this.circuit.getParameter(this.name,key);
	}

	@Override
	public Supply[] getSupplies(int i) {
		return (Supply[]) circuit.forward("getSupplies", i);			
	}

	@Override
	public void pause() {
		// Does nothing.		
	}

}

// ***
