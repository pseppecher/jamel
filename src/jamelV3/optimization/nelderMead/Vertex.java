package jamelV3.optimization.nelderMead;

import jamelV3.basic.agent.Agent;
import jamelV3.basic.agent.AgentDataset;
import jamelV3.basic.agent.BasicAgentDataset;

/**
 * Represents one vertex of a {@link Simplex}.
 */
public class Vertex implements Agent{

	/** The id of the vertex. */
	private static int id=0;

	/** The name of the vertex. */
	final private String name;

	/** The simplex. */
	final private Simplex simplex;

	/** The x coordinate of the vertex. */
	private double x;

	/** The y coordinate of the vertex. */
	private double y;

	/**
	 * Creates a new vertex.
	 * @param simplex  the simplex.
	 * @param x  the x coordinate.
	 * @param y  the y coordinate.
	 */
	public Vertex(Simplex simplex, float x, float y) {
		this.name = "Pod_"+id;
		this.simplex = simplex;
		id++;
		this.x=x;
		this.y=y;
	}

	@Override
	public AgentDataset getData() {
		return new BasicAgentDataset(this.getName()) {{
			this.put("x",x);
			this.put("y",y);
			this.put("fitness",simplex.getFitness(x,y));
			this.put("individual",1.);
		}};
	}

	/**
	 * Returns the fitness of the vertex.
	 * @return the fitness of the vertex.
	 */
	public double getFitness() {
		return simplex.getFitness(x,y);
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the x coordinate of the vertex.
	 * @return the x coordinate of the vertex.
	 */
	public double getX() {
		return x;
	}

	/**
	 * Returns the y coordinate of the vertex.
	 * @return the y coordinate of the vertex.
	 */
	public double getY() {
		return y;
	}

	/**
	 * Moves the vertex to the specified destination.
	 * @param xx  the x coordinate of the destination.
	 * @param yy  the y coordinate of the destination.
	 */
	public void moveTo(double xx, double yy) {
		this.x=xx;
		this.y=yy;
	}

}

// ***
