package jamel.models.m18.r08.markets;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jamel.Jamel;
import jamel.data.DynamicSeries;
import jamel.data.Expression;
import jamel.models.m18.r08.households.BasicWorker2;
import jamel.models.m18.r08.roles.Employer;
import jamel.models.m18.r08.roles.Worker;
import jamel.models.m18.r08.util.BasicSector;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Phase;
import jamel.util.Sector;
import jamel.util.Simulation;

/**
 * A basic labor market.
 * 
 * Admet plusieurs secteur employeurs mais un seul secteur travailleurs.
 * 
 * 2018-03-10: jamel/models/m18/r01/markets/BasicLaborMarket.java
 * 
 * 2018-03-08 : BasicLaborMarket3
 * nouvelle version encore plus proche du labor market de la version ICC.
 * Elle permettra aux entreprises de copier les salaires d'entreprises
 * appartenant à d'autres secteurs.
 * 
 * 2018-03-08 : BasicLaborMarket2
 * nouvelle version plus proche du labor market de la version ICC.
 * Conçu pour travailler uniquement avec BasicWorker2.
 */
public class BasicLaborMarket_BAK extends JamelObject implements Sector {

	/**
	 * The employers.
	 */
	final private List<Sector> employers = new LinkedList<>();

	/**
	 * The name of the sector.
	 */
	final private String name;

	/**
	 * The parameters of the sector.
	 */
	final private Parameters params;

	/**
	 * The list of the phases, accessible by their names.
	 */
	final private Map<String, Phase> phases = new HashMap<>();

	/**
	 * The workers.
	 */
	final private Sector workers;

	/**
	 * Creates a new basic sector.
	 * 
	 * @param params
	 *            the parameters of the sector.
	 * 
	 * @param simulation
	 *            the parent simulation.
	 */
	public BasicLaborMarket_BAK(final Parameters params, final Simulation simulation) {
		super(simulation);
		this.params = params;
		this.name = this.params.getAttribute("name");
		final String employerString = this.params.getString("parameters.employers");
		if (employerString == null || employerString.isEmpty()) {
			throw new RuntimeException("Missing or empty employers attribute.");
		}
		for (final String employerSector : employerString.split(",")) {
			this.employers.add(this.getSimulation().getSector(employerSector.trim()));
		}
		this.workers = this.getSimulation().getSector(this.params.getString("parameters.workers"));
	}

	/**
	 * Returns an expression that gives access to some sectoral data.
	 * 
	 * @param arg
	 *            the argument of the expression to be returned.
	 * @return an expression that gives access to some sectoral data.
	 */
	private Expression getSectorDataAccess(String arg) {
		final Expression result;
		final String[] args = arg.split("\\.");
		if (args[0].equals("phase") && args[2].equals("runtime")) {
			final Phase phase = this.phases.get(args[1]);
			result = new Expression() {

				@Override
				public Double getValue() {
					return (double) phase.getRuntime();
				}

				@Override
				public String toString() {
					return name + ".phase." + args[1] + ".runtime";
				}

			};
		} else {
			throw new RuntimeException("Bad key: '" + args[0] + "' in '" + arg + "'");
		}
		return result;
	}

	@Override
	public void close() {
		// TODO IMPLEMENT ME
	}

	@Override
	public void doEvent(Parameters event) {
		Jamel.notUsed();
	}

	@Override
	public Class<? extends Agent> getAgentClass() {
		Jamel.notUsed();
		return null;
	}

	@Override
	public Expression getDataAccess(String[] args) {
		final Expression result;
		if (args.length == 1) {
			result = this.getSectorDataAccess(args[0]);
		} else {
			Jamel.println("Bad query", args);
			throw new RuntimeException("Bad query");
		}
		return result;
	}

	@Override
	public Expression getIndividualDataAccess(String agentName, String[] args) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Parameters getParameters() {
		Jamel.notUsed();
		return null;
	}

	@Override
	public Phase getPhase(final String phaseName, final boolean shuffle) {

		if (phaseName == null) {
			throw new RuntimeException("Phase name is null");
		}

		final Phase result;
		if (phaseName.equals("matching")) {
			result = new Phase() {

				private long runtime = 0;

				@Override
				public String getName() {
					return phaseName;
				}

				@Override
				public long getRuntime() {
					return this.runtime;
				}

				@Override
				public Sector getSector() {
					return BasicLaborMarket_BAK.this;
				}

				@Override
				public void run() {

					final long start = System.currentTimeMillis();

					final List<Worker> jobSeekers = new LinkedList<>();
					for (Agent worker : workers.selectAll()) {
						if (!((Worker) worker).isEmployed()) {
							jobSeekers.add((Worker) worker);
						}
					}

					final List<Employer> employers2 = new LinkedList<>();
					for (Sector employerSector : employers) {
						for (Agent employer : employerSector.selectAll()) {
							if (((Employer) employer).getJobOffer() != null
									&& !((Employer) employer).getJobOffer().isEmpty()) {
								employers2.add((Employer) employer);
							}
						}
					}

					Collections.shuffle(employers2, getRandom());
					Collections.shuffle(jobSeekers, getRandom());

					for (Employer employer : employers2) {
						if (jobSeekers.size() == 0) {
							break;
						}
						if (employer.getJobOffer() == null) {
							throw new RuntimeException("Inconsistency");
						}
						final int size = employer.getJobOffer().size();

						for (int i = 0; i < size * 3; i++) {
							final BasicWorker2 jobSeeker = (BasicWorker2) jobSeekers
									.get(getRandom().nextInt(jobSeekers.size()));
							jobSeeker.addJobOffer(employer.getJobOffer());
						}

					}

					for (Worker jobSeeker : jobSeekers) {
						((BasicWorker2) jobSeeker).chooseJob();
					}

					final long end = System.currentTimeMillis();

					runtime += end - start;

				}

			};
		} else {
			throw new RuntimeException("Unknown phase name: " + phaseName);
		}

		final Phase previousValue = this.phases.put(phaseName, result);
		if (previousValue != null) {
			throw new RuntimeException(
					"The sector " + name + " alreday contains a phase with the name '" + phaseName + "'");
		}

		return result;

	}

	@Override
	public DynamicSeries getScatterSeries(final String xKey, final String yKey, Expression[] conditions,
			String selection) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public void open() {
		// Jamel.println(this.employers.size(),this.workers.size());
		// TODO IMPLEMENT ME
	}

	@Override
	public Agent select() {
		Jamel.notUsed();
		return null;
	}

	@Override
	public Agent[] select(int n, Agent special) {
		Jamel.notUsed();
		return null;
	}

	@Override
	public List<? extends Agent> selectAll() {
		Jamel.notUsed();
		return null;
	}

	@Override
	public Agent[] selectArray(final int n) {
		Jamel.notUsed();
		return null;
	}

	/**
	 * Returns one employer selected at random.
	 * Used by firms when copying wages.
	 * 
	 * @return one employer selected at random.
	 */
	public Employer selectEmployer() {
		final int sizes[] = new int[this.employers.size()];
		int index = 0;
		int size = 0;
		for (Sector sector : this.employers) {
			size+=((BasicSector)sector).size();
			sizes[index]=size;
			index++;
		}
		
		final int alea = getRandom().nextInt(size);
		int selected = -1;
		for (int i=0;i<sizes.length;i++) {
			if (alea<sizes[i]) {
				selected = i;
				break;
			}
		}
		
		if (selected==-1) {
			throw new RuntimeException("No employer sector or employer sectors are empty.");
		}
		
		return (Employer) this.employers.get(selected).select();
	}

	@Override
	public List<? extends Agent> selectList(int n) {
		Jamel.notUsed();
		return null;
	}

}
