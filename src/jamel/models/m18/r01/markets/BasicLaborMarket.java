package jamel.models.m18.r01.markets;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jamel.Jamel;
import jamel.data.DynamicSeries;
import jamel.data.Expression;
import jamel.models.m18.r01.firms.Firm;
import jamel.models.m18.r01.households.BasicWorker;
import jamel.models.util.Employer;
import jamel.models.util.Worker;
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
public class BasicLaborMarket extends JamelObject implements Sector {

	/**
	 * The employers.
	 */
	final private List<Employer> employers = new LinkedList<>();

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
	final private List<Worker> workers = new LinkedList<>();

	/**
	 * Creates a new basic sector.
	 * 
	 * @param params
	 *            the parameters of the sector.
	 * 
	 * @param simulation
	 *            the parent simulation.
	 */
	@SuppressWarnings("unchecked")
	public BasicLaborMarket(final Parameters params, final Simulation simulation) {
		super(simulation);
		this.params = params;
		this.name = this.params.getAttribute("name");
		final String employerString = this.params.getString("parameters.employers");
		if (employerString == null || employerString.isEmpty()) {
			throw new RuntimeException("Missing or empty employers attribute.");
		}
		for (final String employerSector : employerString.split(",")) {
			final List<Employer> newList = (List<Employer>) this.getSimulation().getSector(employerSector.trim())
					.selectAll();
			this.employers.addAll(newList);
			for (Employer employer : newList) {
				((Firm) employer).setLaborMarket(this);
			}
		}
		this.workers.addAll(
				(List<Worker>) this.getSimulation().getSector(this.params.getString("parameters.workers")).selectAll());
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
					return BasicLaborMarket.this;
				}

				@Override
				public void run() {

					final long start = System.currentTimeMillis();

					final List<Worker> jobSeekers = new LinkedList<>();
					for (Worker worker : workers) {
						if (!worker.isEmployed()) {
							jobSeekers.add(worker);
						}
					}
					final List<Employer> employers2 = new LinkedList<>();
					for (Employer employer : employers) {
						if (employer.getJobOffer() != null && !employer.getJobOffer().isEmpty()) {
							employers2.add(employer);
						}
					}

					Collections.shuffle(employers2, getRandom());

					for (Employer employer : employers2) {
						if (jobSeekers.size() == 0) {
							break;
						}
						if (employer.getJobOffer() == null) {
							throw new RuntimeException("Inconsistency");
						}
						final int size = employer.getJobOffer().size();

						for (int i = 0; i < size * 3; i++) {
							final BasicWorker jobSeeker = (BasicWorker) jobSeekers
									.get(getRandom().nextInt(jobSeekers.size()));
							jobSeeker.addJobOffer(employer.getJobOffer());
						}

					}

					for (Worker jobSeeker : jobSeekers) {
						((BasicWorker) jobSeeker).chooseJob();
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
		return this.employers.get(getRandom().nextInt(this.employers.size()));
	}

	@Override
	public List<? extends Agent> selectList(int n) {
		Jamel.notUsed();
		return null;
	}

}
