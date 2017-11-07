package jamel.models.modelJEE.firms.factory;

/**
 * WORK IN PROGRESS 
 */
public interface Technology {

	long getInputVolumeForANewMachine();

	int getProductionTime();

	long getProductivity();

	double getTimelifeMean();

	double getTimelifeStDev();

}
