package jamel.varia.schelling;

import jamel.basic.Circuit;
import jamel.basic.agent.AgentDataset;
import jamel.basic.agent.BasicAgentDataset;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.BasicSectorDataSet;
import jamel.basic.sector.Phase;
import jamel.basic.sector.Sector;
import jamel.basic.sector.SectorDataset;
import jamel.basic.util.BasicParameters;
import jamel.basic.util.InitializationException;
import jamel.basic.util.JamelParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The Schelling's model.
 */
public class Schelling implements Sector {

	/**
	 * Represent a location.
	 */
	private class Point {
		
		/** x */
		public final int x;
		
		/** y */
		public final int y;
		
		/**
		 * Creates a new point.
		 * @param x the x coordinate of the new point.
		 * @param y the y coordinate of the new point.
		 */
		public Point(int x, int y) {
			this.x=x;
			this.y=y;			
		}
		
	}

	/** The height. */
	private int height=200;

	/** The land. */
	private int[][] land;

	/** The total number of agents. */
	private int nAgent;
	
	/** The name of the sector. */
	private final String name;
	
	/** The random. */
	private final Random random;

	/** The tolerance of the agents. */
	private int tolerance;

	/** The list of free locations. */
	private ArrayList<Point> vacancies;

	/** The width. */
	private int width=200;

	/**
	 * Creates a new sector.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 * @throws InitializationException If something goes wrong.
	 */
	public Schelling(String name, Circuit circuit) throws InitializationException {
		this.name=name;
		this.random=circuit.getRandom();
	}

	/**
	 * Returns the color of the specified location.
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 * @return the color.
	 */
	private int getColorAt(int x, int y) {
		if (x<0) {
			x+=height;
		}
		else if (x>=height) {
			x-=height;			
		}
		if (y<0) {
			y+=width;
		}
		else if (y>=width) {
			y-=width;			
		}
		return land[x][y];
	}

	/**
	 * Returns a vacancy selected at random.
	 * @return a vacancy.
	 */
	private Point getVacancy() {
		final int id = this.random.nextInt(vacancies.size());
		return vacancies.get(id);
	}

	/**
	 * Moves the agent from the specified location.
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 */
	private void move(int x, int y) {
		final Point free = getVacancy();
		if (free!=null) {
			final int color = land[x][y];
			land[x][y]=0;
			land[free.x][free.y]=color;
			this.vacancies.remove(free);
			this.vacancies.add(new Point(x,y));
		}
	}
	
	/**
	 * Returns the number of agent of the specified color in the neighborhood of the specified location.
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 * @param color the color.
	 * @return the number of agents.
	 */
	private int neighborhood(final int x, final int y, final int color) {
		int result =0;
		for (int xx=x-1;xx<x+2;xx++) {
			for (int yy=y-1;yy<y+2;yy++) {
				if (xx!=x || yy!=y) {
					if (getColorAt(xx,yy)==color) {
						result++;
					}
				}
			}			
		}
		return result;
	}

	@Override
	public void doEvent(Element event) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public SectorDataset getDataset() {
		final SectorDataset sectorDataset = new BasicSectorDataSet(this.nAgent);
		int white = 0;
		int black = 0;
		for (int x=0;x<width;x++) {
			for (int y=0;y<height;y++) {
				final int color = land[x][y]; 
				if(color!=0) {
					final String agentName;
					if (color==1) {
						agentName="White"+white;
						white++;
					}
					else {
						agentName="Black"+black;
						black++;
					}
					final AgentDataset agentData = new BasicAgentDataset(agentName);
					agentData.put("x", (double) x);
					agentData.put("y", (double) y);
					agentData.put("color", (double) color);
					sectorDataset.put(agentData);					
				}
			}
		}
		return sectorDataset;	
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Phase getPhase(final String name) {
		Phase result = null;
		if (name.equals("move")) {
			result = new AbstractPhase(name, this){

				@Override
				public void run() {
					final List<Point> dissatisfied = new LinkedList<Point>();
					for (int x=0;x<width;x++) {
						for (int y=0;y<height;y++) {
							final int color = land[x][y];
							if (color>0) {
								final int other;
								if (color==1) {
									other=2;
								}
								else {
									other=1;								
								}
								if (neighborhood(x,y,other)>tolerance) {
									dissatisfied.add(new Point(x,y));
								}
							}
						}
					}
					
					Collections.shuffle(dissatisfied,random);
					for(Point point:dissatisfied) {
						move(point.x,point.y);					
					}
				}

			};			
		}
		return result;
	}

	@Override
	public void init(Element element) throws InitializationException {
		if (element==null) {
			throw new IllegalArgumentException("Element is null");			
		}

		// Looking for the settings. 
		final Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		final JamelParameters params = new BasicParameters();
		for (int i=0; i< attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType()==Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				params.put(attr.getName(), Float.parseFloat(attr.getValue()));
			}
		}
		
		this.tolerance=params.get("tolerance").intValue();
		this.height=params.get("height").intValue();
		this.width=params.get("width").intValue();
		final int free=params.get("vacancies").intValue();
		this.land=new int[height][width];
		this.vacancies = new ArrayList<Point>(free);
		this.nAgent = height*width-free;
		for (int x=0;x<width;x++) {
			for (int y=0;y<width;y++) {
				vacancies.add(new Point(x,y));
			}
		}
		for (int i=0;i<nAgent/2;i++) {
			final Point freeLoc = getVacancy();
			land[freeLoc.x][freeLoc.y]=1;
			vacancies.remove(freeLoc);
			final Point freeLoc2 = getVacancy();
			land[freeLoc2.x][freeLoc2.y]=2;
			vacancies.remove(freeLoc2);
		}
		
	}

}

// ***
