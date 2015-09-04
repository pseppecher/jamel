package jamel.austrian.firms;

import  jamel.austrian.roles.Employer;
import  jamel.austrian.roles.Debtor;
import  jamel.austrian.roles.Seller;


/**
 * An interface for firms.
 */
public interface Firm extends Seller, Debtor, Employer {

	void open();

	boolean invest();
	
	void updateProfits();
	
	void production();

	void goBankrupt(int remainingFunds);
	
	void close();

	void kill();

	int getStage();

	int getType();

	String getTypeAlphabetic();

	int getID();

	void laborRelease();

	void setMonitor();

	void updatePrice();

	void setProductionMode();

	void offerJobs();

	boolean buyMachinery();

	void planProduction();

	void registerExpansionDemand();

	void updateMachinePrice();
	
	double getData(String key);

}
