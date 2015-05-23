package jamelV3.loktaVolterra;

import java.util.Random;

import jamelV3.basic.agent.Agent;

/**
 * An abstract agent.
 */
abstract class AbstractAgent implements Agent{
	
	/** If the agent is still alive. */
	boolean alive=true;

	/** The vital energy. */
	double energy = 10;
	
	/** The name. */
	final String name;
	
	/** The random. */
	final Random random;

	/** The x coordinate. */
	double x;

	/** The maximum x. */
	final int xMax;

	/** The y coordinate. */
	double y;

	/** The maximum y. */
	final int yMax;
	
	/**
	 * Creates the agent.
	 * @param name the name.
	 * @param xMax the x maximum. 
	 * @param yMax the y maximum.
	 * @param random the random.
	 * @param energy the vital energy.
	 */
	AbstractAgent(String name, int xMax, int yMax, Random random, double energy) {
		this.name=name;
		this.random=random;
		this.energy=energy;
		this.xMax=xMax;
		this.yMax=yMax;
	}
	
	/**
	 * @return the alive
	 */
	boolean isAlive() {
		return alive;
	}

	/**
	 * Kills the agent.
	 */
	void kill() {
		this.alive = false;
	}
	
	/**
	 * Moves.
	 * @param move the move.
	 * @param cost the cost.
	 */
	void move(Float move, Float cost) {
		if(this.random.nextBoolean()) {
			x+=move;
		}
		else {
			x-=move;
		}
		if (x>=xMax) {
			x-=xMax;
		}
		else if (x<0) {
			x+=xMax;
		}
		if (x==xMax) {
			x=0;
		}
		if(this.random.nextBoolean()) {
			y+=move;
		}
		else {
			y-=move;
		}
		if (y>=yMax) {
			y-=yMax;
		}
		else if (y<0) {
			y+=yMax;
		}
		if (y==yMax) {
			y=0;
		}
		energy -= cost;
	}	
	
	@Override
	public String getName() {
		return this.name;
	}
	
}

// ***
