package jamel.models.m18.r08.firms;

/*
 * 2018-28-03 jamel/models/m18/r03/firms/BasicTechnology.java
 * Pour modéliser le progrès technologique.
 * Ici, permet simplement une croissance de la produtictivité avec le temps.
 * A chaque période, la productivité augmente d'un facteur donné.
 */

import java.util.List;

import jamel.Jamel;
import jamel.data.DynamicSeries;
import jamel.data.Expression;
import jamel.models.m18.r08.households.Household;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Phase;
import jamel.util.Sector;
import jamel.util.Simulation;

/**
 * Represents the current technology.
 */
public class BasicTechnology extends JamelObject implements Sector {

	/**
	 * 
	 */
	final private float inputVolumeForANewMachineFactor;

	/**
	 * The average time life of the machines.
	 */
	final private int machineTimeLife;

	/**
	 * The standard deviation of the time life of the machines.
	 */
	final private int machineTimeLifeStDev;

	/**
	 * The name of the sector.
	 */
	final private String name;

	/**
	 * The parameters of the sector.
	 */
	final private Parameters params;

	/**
	 * The lenght of the production process.
	 */
	final private int productionProcessLenght;

	/**
	 * The productivity of one machine.
	 */
	private float productivity;

	/**
	 * The quality of the goods required as input for the creation of a new
	 * machine.
	 */
	final private String qualityOfInputForTheCreationOfANewMachine;

	/**
	 * Creates a new basic technology.
	 * 
	 * @param parameters
	 *            the parameters of the technology.
	 * 
	 * @param simulation
	 *            the parent simulation.
	 */
	public BasicTechnology(final Parameters parameters, final Simulation simulation) {
		super(simulation);
		this.params = parameters;
		this.name = this.params.getAttribute("name");
		this.productivity = parameters.getInt("parameters.productivity");
		this.productionProcessLenght = parameters.getInt("parameters.processLenght");
		this.qualityOfInputForTheCreationOfANewMachine = parameters
				.getString("parameters.machines.creation.input.quality");
		this.machineTimeLife = parameters.getInt("parameters.machines.timeLife.mean");
		this.machineTimeLifeStDev = parameters.getInt("parameters.machines.timeLife.stDev");
		this.inputVolumeForANewMachineFactor = parameters.getInt("parameters.machines.creation.input.volume")
				/ this.productivity;
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

	/**
	 * @return the inputVolumeForANewMachine
	 */
	public int getInputVolumeForANewMachine() {
		return (int) (this.productivity * this.inputVolumeForANewMachineFactor);
	}

	/**
	 * @return the machineTimeLife
	 */
	public int getMachineTimeLife() {
		return machineTimeLife;
	}

	/**
	 * @return the machineTimeLifeStDev
	 */
	public int getMachineTimeLifeStDev() {
		return machineTimeLifeStDev;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Parameters getParameters() {
		return this.params;
	}

	@Override
	public Phase getPhase(final String phaseName, final boolean shuffle) {
		Jamel.notUsed();
		return null;
	}

	/**
	 * @return the productionProcessLenght
	 */
	public int getProductionProcessLenght() {
		return productionProcessLenght;
	}

	/**
	 * @return the productivity
	 */
	public int getProductivity() {
		return (int) productivity;
	}

	/**
	 * @return the qualityOfInputForTheCreationOfANewMachine
	 */
	public String getQualityOfInputForTheCreationOfANewMachine() {
		return qualityOfInputForTheCreationOfANewMachine;
	}

	@Override
	public DynamicSeries getScatterSeries(final String xKey, final String yKey, Expression[] conditions,
			String selection) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public void open() {
		// this.productivity *= 1.003;
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
