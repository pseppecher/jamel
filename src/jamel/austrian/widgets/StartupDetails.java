package jamel.austrian.widgets;

public class StartupDetails {
	
	private final String type;
	
	private final int stage;
	
	private final boolean physicalCapital;
	
	
	public StartupDetails(String type, int stage, boolean physicalCapital){
		
		this.type= type;
		this.stage = stage;
		this.physicalCapital = physicalCapital;
		
	}
	
	
	public String getType(){
		return type;
	}
	
	public int getStage(){
		return stage;
	}
	
	public boolean getProductionForm(){
		return physicalCapital;
	}
	
	
}
