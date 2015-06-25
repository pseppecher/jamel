package jamel.varia.loktaVolterra;

import jamel.basic.agent.Agent;
import jamel.basic.agent.AgentDataset;
import jamel.basic.agent.BasicAgentDataset;

@SuppressWarnings("javadoc")
public class LandSquare implements Agent{

	private double grass;
	
	private final Grass grassSector;
	
	private final int x;

	private final int y;
	
	public LandSquare(Grass grass, int x, int y) {
		this.grassSector=grass;
		this.x=x;
		this.y=y;
		this.grass=this.grassSector.getParam(Grass.lim);
	}

	public double consume(double volume) {
		final double result;
		if (grass>=volume) {
			grass-=volume;
			result=volume;
		}
		else {
			result=grass;
			grass=0;
		}
		return result;
	}

	@Override
	public AgentDataset getData() {
		return new BasicAgentDataset(getName()) {{
			this.put("x", (double) x);
			this.put("y", (double) y);
			this.put("grass", grass);
		}};
	}

	@Override
	public String getName() {
		return "square-"+x+"-"+y;
	}

	public void grow() {
		this.grass+=this.grassSector.getParam(Grass.growth);
		double lim=this.grassSector.getParam(Grass.lim);
		if (this.grass>lim) {
			this.grass=lim;
		}
	}

}

// ***
