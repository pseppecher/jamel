package jamel.v170804.models.basicModel3;

import jamel.Jamel;
import jamel.util.Agent;
import jamel.util.Expression;
import jamel.util.Parameters;
import jamel.util.Phase;
import jamel.util.Sector;
import jamel.util.Simulation;
import jamel.v170804.util.BasicSector;

/**
 * An alias of {@code BasicSector}.
 */
public class SimpleSector implements Sector {

	/**
	 * The {@code BasicSector}.
	 */
	final private Sector sector;

	/**
	 * Creates a new basic sector.
	 * 
	 * @param params
	 *            the parameters of the sector.
	 * 
	 * @param simulation
	 *            the parent simulation.
	 */
	public SimpleSector(final Parameters params, final Simulation simulation) {
		this.sector = new BasicSector(params, simulation);
	}

	@Override
	public void close() {
		this.sector.close();
	}

	@Override
	public void doEvent(Parameters event) {
		this.sector.doEvent(event);
	}

	@Override
	public Expression getDataAccess(String agentName, String[] args) {
		return this.sector.getDataAccess(agentName, args);
	}

	@Override
	public Expression getDataAccess(String[] args) {
		return this.sector.getDataAccess(args);
	}

	@Override
	public String getName() {
		return this.sector.getName();
	}

	@Override
	public Parameters getParameters() {
		return this.sector.getParameters();
	}

	@Override
	public Integer getPeriod() {
		return this.sector.getPeriod();
	}

	@Override
	public Phase getPhase(final String phaseName, final String[] options) {
		return this.sector.getPhase(phaseName, options);
	}

	@Override
	public Simulation getSimulation() {
		return this.sector.getSimulation();
	}

	@Override
	public void open() {
		this.sector.open();
	}

	@Override
	public Agent select() {
		Jamel.notUsed();
		return null;
	}

	@Override
	public Agent[] select(int n) {
		return this.sector.select(n);
	}

}
