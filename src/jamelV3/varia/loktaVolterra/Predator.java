package jamelV3.varia.loktaVolterra;

import java.util.Random;

import jamelV3.basic.agent.BasicAgentDataset;
import jamelV3.basic.agent.AgentDataset;

/**
 * Represent a predator.
 */
@SuppressWarnings("javadoc")
class Predator extends AbstractAgent {

	private static int id=0;
	
	private Predators sector;

	public Predator(Predators sector, Random random, double energy) {
		super("Predator-"+id, sector.getParam(Preys.landWidth).intValue(), sector.getParam(Preys.landHeight).intValue(), random, energy);
		id++;
		this.sector = sector;
		this.x = this.random.nextInt(xMax-1);
		this.y = this.random.nextInt(yMax-1);
	}

	public Predator(Predators sector, Random random, double x, double y, double energy) {
		super("Predator-"+id, sector.getParam(Preys.landWidth).intValue(), sector.getParam(Preys.landHeight).intValue(), random, energy);
		id++;
		this.sector = sector;
		this.x = x;
		this.y = y;
	}

	@Override
	public AgentDataset getData() {
		return new BasicAgentDataset(getName()) {{
			this.put("x", x);
			this.put("y",y);
			this.put("energy",energy);
			this.put("individual",1.);
		}};
	}

	public void move() {
		if (!isAlive()) {
			throw new RuntimeException("Walking dead");
		}
		this.move(this.sector.getParam(Preys.move), this.sector.getParam(Preys.cost).floatValue());
		energy += this.sector.eatPrey(x,y,sector.getParam(Preys.eatVolume));
		if (energy<0) {
			this.kill();
			this.sector.remove(this);
		}
		if (energy>sector.getParam(Preys.reproductionThreshold)) {
			final double birthEnergy = this.sector.getParam(Preys.birthEnergy); 
			energy -= birthEnergy;
			this.sector.newPredator(new Predator(this.sector,this.random,x,y,birthEnergy));
		}
	}

}

// ***
