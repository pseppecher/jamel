package jamelV3.loktaVolterra;

import java.util.Random;

import jamelV3.basic.agent.BasicAgentDataset;
import jamelV3.basic.agent.AgentDataset;

/**
 * Represent a prey.
 */
class Prey extends AbstractAgent {

	/** The id. */
	private static int id=0;

	/** sector */
	private Preys sector;

	/**
	 * Creates a new prey.
	 * @param sector the sector.
	 * @param random the random.
	 */
	public Prey(Preys sector, Random random) {
		super("Prey-"+id, sector.getParam(Preys.landWidth).intValue(), sector.getParam(Preys.landHeight).intValue(), random,1);// 1= initial energy should be a parameter
		id++;
		this.sector = sector;
		this.x = this.random.nextInt(xMax-1);
		this.y = this.random.nextInt(yMax-1);
	}

	/**
	 * Creates a new prey.
	 * @param sector the sector.
	 * @param random the random.
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 * @param energy the initial energy.
	 */
	public Prey(Preys sector, Random random, double x, double y, double energy) {
		super("Prey-"+id, sector.getParam(Preys.landWidth).intValue(), sector.getParam(Preys.landHeight).intValue(), random,energy);
		id++;
		this.sector = sector;
		this.x = x;
		this.y = y;
	}

	/**
	 * This prey is eaten. 
	 * @param volume the volume to be eaten.
	 * @return the resulting energy for the prey.
	 */
	public double eat(double volume) {
		final double result;
		if (volume<this.energy) {
			this.energy-=volume;
			result=volume;
		}
		else {
			result=this.energy;
			this.energy=0;
			this.kill();
			this.sector.remove(this);
		}
		return result;
	}

	@Override
	public AgentDataset getData() {
		return new BasicAgentDataset(this.getName()) {{
			this.put("x",x);
			this.put("y",y);
			this.put("energy",energy);
			this.put("individual",1.);
		}};
	}

	/**
	 * Returns the x coordinate.
	 * @return the x coordinate.
	 */
	public double getX() {
		return x;
	}

	/**
	 * Returns the y coordinate.
	 * @return the y coordinate.
	 */
	public double getY() {
		return y;
	}

	/**
	 * Moves.
	 */
	public void move() {
		if (!isAlive()) {
			throw new RuntimeException("Walking dead");
		}
		this.move(this.sector.getParam(Preys.move), this.sector.getParam(Preys.cost));
		energy += this.sector.eatGrass(x,y,sector.getParam(Preys.eatVolume));
		if (energy<0) {
			this.kill();
			this.sector.remove(this);
		}
		if (energy>sector.getParam(Preys.reproductionThreshold)) {
			final double birthEnergy = this.sector.getParam(Preys.birthEnergy); 
			energy -= birthEnergy;
			this.sector.newPrey(new Prey(this.sector,this.random,x,y,birthEnergy));
		}
	}

}

// ***
