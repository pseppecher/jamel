package jamel.jamel.firms;

import java.util.Map;

/**
 * WORK IN PROGRESS - 16-09-2015
 * TODO: 
 */
public interface Technology {

	long getInputVolumeForANewMachine();

	int getProductionTime();

	long getProductivity();

	double getTimelifeMean();

	double getTimelifeStDev();

	String getTypeOfProduction();

	Map<String, Float> get(String string);

	String getTypeOfInputForMachineCreation();

}
