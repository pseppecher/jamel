package jamelV3.loktaVolterra;

import java.util.Random;

import jamelV3.basic.agent.BasicAgentDataset;
import jamelV3.basic.agent.Agent;
import jamelV3.basic.agent.AgentDataset;

@SuppressWarnings("javadoc")
class Prey implements Agent {

	private static int id=0;

	private boolean alive;

	private double energy = 10;

	private final String name;

	private final Random random;

	private Preys sector;

	private double x;

	private final int xMax;

	private double y;

	private final int yMax;

	public Prey(Preys sector, Random random) {
		this.sector = sector;
		this.name = "Prey-"+id;
		id++;
		this.random = random;
		this.xMax= this.sector.getParam(Preys.landWidth).intValue();
		this.yMax= this.sector.getParam(Preys.landHeight).intValue();
		this.x = this.random.nextInt(xMax-1);
		this.y = this.random.nextInt(yMax-1);
		this.alive = true;
	}

	public Prey(Preys sector, Random random, double x, double y, double energy) {
		this.sector = sector;
		this.name = "Prey-"+id;
		id++;
		this.random = random;
		this.xMax= this.sector.getParam(Preys.landWidth).intValue();
		this.yMax= this.sector.getParam(Preys.landHeight).intValue();
		this.x = x;
		this.y = y;
		this.energy = energy;
		this.alive = true;
	}

	@Override
	public AgentDataset getData() {
		return new BasicAgentDataset(name) {{
			this.put("x", x);
			this.put("y",y);
			this.put("energy",energy);
			this.put("individual",1.);
		}};
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void move() {
		final double move = this.sector.getParam(Preys.move);
		if (!alive) {
			throw new RuntimeException("Walking dead");
		}
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
		energy -= this.sector.getParam(Preys.cost);
		energy += this.sector.eatGrass(x,y,sector.getParam(Preys.eatVolume));
		if (energy<0) {
			this.alive=false;
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
