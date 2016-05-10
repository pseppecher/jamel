package jamel.jamel.firms;

import java.util.Map;

/**
 * Represents a technology.
 */
public interface Technology {

	/**
	 * Returns the value of a new machine in real terms
	 * (number of goods to produce a machine).
	 * 
	 * @return the value of a new machine in real terms.
	 */
	long getInputVolumeForANewMachine();

	/**
	 * Returns the length of the production process.
	 * @return the length of the production process.
	 */
	int getProductionTime();

	/**
	 * Returns the productivity of the machines.
	 * @return the productivity of the machines.
	 */
	long getProductivity();

	/**
	 * Returns the technical coefficients of this technology.
	 * 
	 * @return the technical coefficients of this technology.
	 */
	Map<String, Float> getTechnicalCoefficients();

	/**
	 * Returns the average time life of the machines. 
	 * @return the average time life of the machines.
	 */
	double getTimelifeMean();

	/**
	 * Returns the standard deviation of the time life of the machines. 
	 * @return the standard deviation of the time life of the machines.
	 */
	double getTimelifeStDev();

	/**
	 * Returns the type of the goods used to create a new machine. 
	 * @return the type of the goods used to create a new machine.
	 */
	String getTypeOfInputForMachineCreation();

	/**
	 * Returns the type of the goods produced by a machine.
	 * @return the type of the goods produced by a machine.
	 */
	String getTypeOfProduction();

}
