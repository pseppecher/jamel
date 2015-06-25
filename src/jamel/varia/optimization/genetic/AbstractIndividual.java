package jamel.varia.optimization.genetic;

import jamel.basic.agent.AgentDataset;
import jamel.basic.agent.BasicAgentDataset;

import java.util.Comparator;
import java.util.Random;

/**
 * An individual element of an {@link BasicGA}.
 */
abstract public class AbstractIndividual implements Individual {

	/** The id of the agent. */
	private static int id=0;

	/** 
	 * The fitness comparator.<p>
	 * To compare vertices according to their fitness (ascending order).
	 */
	public static final Comparator<Individual>fitnessComparator = new Comparator<Individual>() {
		@Override
		public int compare(Individual indiv1, Individual indiv2) {
			return (new Double(indiv1.getFitness()).compareTo(indiv2.getFitness()));
		}
	};

	/** The name of the agent. */
	final private String name;

	/** The x coordinate of the agent. */
	private double x;

	/** The y coordinate of the agent. */
	private double y;

	/** The parent algorithm. */
	protected final BasicGA basicGA;	

	/** The curiosity (exploration or mutation factor). */
	protected final float curiosity;

	/** The random. */
	protected final Random random;	

	/** The size of the tournament selection. */
	protected final int tournamentSize;

	/** The new x coordinate of the agent. */
	protected Double xx=null;

	/** The new y coordinate of the agent. */
	protected Double yy=null;

	/**
	 * Creates a new agent.
	 * @param basicGA  the parent algorithm.
	 * @param random  the random.
	 */
	public AbstractIndividual(BasicGA basicGA, Random random) {
		this.basicGA=basicGA;
		this.random=random;
		this.name="Agent_"+id;
		id++;
		final float left = this.basicGA.getParam("left");
		final float right = this.basicGA.getParam("right");
		final float top = this.basicGA.getParam("top");
		final float bottom = this.basicGA.getParam("bottom");
		this.x=left+this.random.nextFloat()*(right-left);
		this.y=bottom+this.random.nextFloat()*(top-bottom);
		this.curiosity = this.basicGA.getParam("curiosity");
		this.tournamentSize = this.basicGA.getParam("tournamentSize").intValue();
	}

	/**
	 * The adaptation procedure.
	 * @param ind0  the first parent.
	 * @param ind1  the second parent.
	 */
	protected void adaptation(Individual ind0, Individual ind1) {
		// Adaptation
		final double xG=(ind0.getX()+ind1.getX())/2;
		final double yG=(ind0.getY()+ind1.getY())/2;
		final double xRange=Math.abs(ind0.getX()-ind1.getX());
		final double yRange=Math.abs(ind0.getY()-ind1.getY());
		xx = xG+xRange*(0.5-random.nextFloat())*this.curiosity;
		yy = yG+yRange*(0.5-random.nextFloat())*this.curiosity;
	}

	@Override
	public void adapt() {
		if (xx!=null) {
			this.x=xx;
			this.y=yy;
		}
	}

	@Override
	public AgentDataset getData() {
		return new BasicAgentDataset(this.getName()) {{
			this.put("x",x);
			this.put("y",y);
			this.put("fitness",getFitness());
			this.put("agent",1.);
		}};
	}

	@Override
	public double getFitness() {
		return basicGA.getFitness(x,y);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}
	
	@Override
	abstract public void tournament();

}

// ***
