package jamel.v170804.models.basicModel3;

import java.io.File;
import java.util.Random;

import org.jfree.data.xy.XYSeries;

import jamel.Jamel;
import jamel.util.Expression;
import jamel.util.Parameters;
import jamel.util.Sector;
import jamel.util.Simulation;
import jamel.v170804.util.BasicSimulation;

/**
 * An alias of {@code BasicSimulation}.
 */
public class SimpleSimulation implements Simulation {

	/**
	 * The {@code BasicSimulation}.
	 */
	private Simulation simulation;

	/**
	 * Creates an new simulation.
	 * 
	 * @param scenario
	 *            the parameters of the simulation.
	 * @param file
	 *            The file that contains the description of the simulation.
	 */
	public SimpleSimulation(final Parameters scenario, final File file) {
		this.simulation = new BasicSimulation(scenario, file);
	}

	@Override
	public Expression getDataAccess(final String key) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public Expression getExpression(String key) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public File getFile() {
		Jamel.notUsed();
		return null;
	}

	@Override
	public String getInfo(final String query) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public String getModel() {
		Jamel.notUsed();
		return null;
	}

	@Override
	public String getName() {
		Jamel.notUsed();
		return null;
	}

	@Override
	public int getPeriod() {
		Jamel.notUsed();
		return 0;
	}

	/**
	 * Returns the random.
	 * 
	 * @return the random.
	 */
	@Override
	public Random getRandom() {
		Jamel.notUsed();
		return null;
	}

	@Override
	public Sector getSector(final String name) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public XYSeries getSeries(final String x, final String y, final String conditions) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public boolean isPaused() {
		Jamel.notUsed();
		return false;
	}

	@Override
	public void pause() {
		Jamel.notUsed();
	}

	@Override
	public void run() {
		this.simulation.run();
	}

}
