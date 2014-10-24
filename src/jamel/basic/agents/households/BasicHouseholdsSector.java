package jamel.basic.agents.households;

import jamel.Simulator;
import jamel.basic.agents.util.AgentSet;
import jamel.basic.agents.util.BasicAgentSet;
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
	private static class PARAM {

		/** The key for the type of agents to create*/
		public static final String HOUSEHOLDS_TYPE = "agents.type";

	}

	/** The sector name. */
	private final String name;

	/** The circuit. */
	private final Circuit circuit;

	/** The households counter. */
	private int countHouseholds;

	/** The collection of households. */
	private final AgentSet<Household> households;

	/**
	 * Creates a new sector for households.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public BasicHouseholdsSector(String name, Circuit circuit) {
		this.circuit=circuit;
		this.name=name;
		this.households = new BasicAgentSet<Household>();
	}

	/**
	 * Closes the sector at the end of the period.
	 */
	private void close() {
		for (final Household household:this.households.getList()) {
			household.close();
		}
		this.circuit.forward(KEY.putData,this.name,this.households.collectData());
	}

	/**
	 * Creates households.
	 * @param type the type of households to create.
	 * @param lim the number of households to create.
	 * @return a list containing the new households.
	 */
	private List<Household> createHousholds(String type, int lim) {
		final String errorMsg = "BasicHousholdsSector: error while creating household";
		final List<Household> list = new ArrayList<Household>(lim);
		for(int index=0;index<lim;index++) {
			this.countHouseholds++;
			final String name = "Household"+this.countHouseholds;
			Household household;
			try {
				household = (Household) Class.forName(type,false,ClassLoader.getSystemClassLoader()).getConstructor(String.class,HouseholdsSector.class).newInstance(name,this);
				list.add(household);
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
			};
		}
		return list;
	}

	@Override
	public boolean doPhase(String phaseName) {

		if (phaseName.equals("opening")) {
			for (final Household household:households.getList()) {
				household.open();
			}
		} 

		else if (phaseName.equals("job_search")) {
			for (final Household household:households.getShuffledList()) {
				household.jobSearch();
			}
		}

		else if (phaseName.equals("consumption")) {
			for (final Household household:households.getShuffledList()) {
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
			result = households.getRandomAgent();
		}

		else if (request.equals("getRandomHouseholds")) {
			result = households.getRandomList((Integer) args[0]);
		}

		else if (request.equals("addDataKey")) { // DELETE
			throw new RuntimeException("This request is obsolete.");
			//result = this.dataKeys .add((String) args[0]);
		}

		else if (request.equals("change in parameters")) {
			for (final Household household:households.getList()) {
				household.updateParameters();
			}
			result = null;
		}

		else if (request.equals("new")) {
			this.households.putAll(this.createHousholds(this.circuit.getParameter(this.name,PARAM.HOUSEHOLDS_TYPE),Integer.parseInt((String) args[0])));
			result = null;
		}

		else {
			throw new IllegalArgumentException(this.name+": Unknown request <"+request+">");
		}

		return result;

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

	public String getParameter(String key) {
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
