package jamel.basic;

import jamel.basic.util.BasicPeriod;
import jamel.basic.util.Dispatcher;
import jamel.basic.util.JamelParameters;
import jamel.util.Circuit;
import jamel.util.Period;
import jamel.util.Sector;
import jamel.util.Timer;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.JTree;

/**
 * A basic class of the Circuit.
 */
public class BasicCircuit extends Circuit {

	/**
	 * A convenient class to store String constants.
	 */
	private static class KEY {

		/** The key to designate the circuit in the parameters file. */
		private static final String CIRCUIT = "Circuit";

		/** The key to designate the phases in the parameters file. */
		private static final String PHASES = "phases";

		/** The key to designate the redirections in the parameters file. */
		private static final String REDIRECT = "redirect";

		/** The key to designate the sectors in the parameters file. */
		private static final String SECTORS = "sectors";

		/** The key to designate the random seed in the parameters file. */
		public static final String RANDOM_SEED = "randomSeed";

	}

	/**
	 * Represents a phase of the circuit.
	 */
	private interface Phase {

		/**
		 * Returns the name of the phase.
		 * @return a string.
		 */
		String getName();

		/**
		 * Returns the sector linked to this phase.
		 * @return a sector.
		 */
		Sector getSector();

	}

	/** The request dispatcher. */
	private final Dispatcher dispatcher = new Dispatcher() {

		/**
		 * A redirection.
		 */
		class Redirection {
			public final String method;
			public final Sector sector;
			public Redirection(Sector sector, String method) {
				this.sector = sector;
				this.method = method;
			}
		}

		/** The redirections. */
		private final Map<String,Redirection> redirections = new TreeMap<String,Redirection>();

		/**
		 * Called when no method reflects the specified request in the dispatcher.
		 * The dispatcher then tries to redirect the request to an other sector.
		 * If there is no recorded redirection for this request, the dispatcher look for an adequate redirection in the parameters.
		 * Ultimately, if no adequate redirection is found, a <code>RuntimeException</code> is generated. 
		 * @return an object.
		 */
		@Override
		protected Object redirect(String request) {
			final Object result;
			if (redirections.containsKey(request)) {
				result = redirections.get(request).sector.forward(redirections.get(request).method, getArgs());
			}
			else {
				final String def1  = getParameter(KEY.CIRCUIT,KEY.REDIRECT,request);
				if (def1==null) {
					throw new RuntimeException("Unknown request: "+request);
				}
				final String[] definition = def1.split("\\.",2);
				if (sectors.containsKey(definition[0])){
					final Sector sector = sectors.get(definition[0]);
					final String method = definition[1];
					this.redirections.put(request,new Redirection(sector,method));
					result = sector.forward(method,this.getArgs());
				}
				else {
					throw new RuntimeException("Unknown sector: "+definition[0]);
				}
			}
			return result;
		}

		@SuppressWarnings("unused")
		public void dataKeys() {
			final Object[] labels = this.getArgs();
			for(Object key:labels) {
				final String[] words = ((String)key).split("\\.",2);
				sectors.get(words[0]).forward("addDataKey", words[1]);// TODO Utiliser un appel direct pour to ça
			}
		}

		@SuppressWarnings("unused")
		public JTree getJTreeViewOfParameters() {
			return jamelParameters.getJTree();			
		}

		@SuppressWarnings("unused")
		public String htmlParameters() {
			return jamelParameters.toHtml();			
		}

		@SuppressWarnings("unused")
		public void pause() {
			pause=true;			
		}

		@SuppressWarnings("unused")
		public void unpause() {
			pause=false;			
		}

	};

	/** The parameters of the simulation. */
	private final JamelParameters jamelParameters ;

	/** A flag that indicates if the simulation is paused or not. */
	private boolean pause;

	/** The phases of the circuit. */
	private final LinkedList<Phase> phases = new LinkedList<Phase>();

	/** A flag that indicates if the simulation is running or not. */
	private boolean run = true;

	/** The sectors of the circuit. */
	private final LinkedHashMap<String,Sector> sectors = new LinkedHashMap<String,Sector>();

	/**
	 * Creates a new basic circuit.
	 * @param jamelParameters a map of parameters for the new circuit.
	 */
	public BasicCircuit(final JamelParameters jamelParameters) {
		super(new Timer() {

			/** The current period value. */
			private Period current = new BasicPeriod(-1);

			@Override
			public Period getPeriod() {
				return this.current;
			}

			@Override
			public void next() {
				this.current=this.current.getNext();
			}

		},
		new Random() {

			private static final long serialVersionUID = 1L;

			{this.setSeed(Integer.parseInt(jamelParameters.get(KEY.CIRCUIT,KEY.RANDOM_SEED)));}

		}
				);
		this.jamelParameters = jamelParameters;

		this.initSectors();
		this.initPhases();
	}

	/**
	 * Executes the events of the simulation.
	 */
	private void doEvents() {
		final String event = this.jamelParameters.get("Circuit.events."+getCurrentPeriod().getValue());
		if (event!=null) {
			final String[] events = JamelParameters.split(event,",");
			for(final String string:events) {

				if (string.equals("pause")) {
					this.pause=true;
					for(Sector sector:sectors.values()) {
						sector.pause();
					}
				}

				else if (string.startsWith("change.")) {
					final String[] truc1 = JamelParameters.split(string.substring(7),"=");
					final String[] truc2 = truc1[0].split("\\.", 2);
					this.jamelParameters.put(truc1[0], truc1[1]);
					this.sectors.get(truc2[0]).forward("change in parameters");					
				}

				else {
					final String[] truc1 = JamelParameters.split(string,"=");
					final String[] truc2 = truc1[0].split("\\.", 2);
					this.sectors.get(truc2[0]).forward(truc2[1],truc1[1]);						
				}

			}

		}
	}

	/**
	 * Executes a period of the circuit.
	 */
	private void doPeriod() {
		nextPeriod();
		this.doEvents();
		for(Phase phase:phases) {
			if (phase.getSector().doPhase(phase.getName())) {}
			else {
				throw new RuntimeException("Failure phase <"+phase.toString()+">");
			};
		}
	}

	/**
	 * Initializes the different phases of the circuit period.
	 */
	private void initPhases() {
		String phases = this.getParameter(KEY.CIRCUIT,KEY.PHASES);
		try {
			if (phases==null) {
				throw new RuntimeException(KEY.CIRCUIT+"."+KEY.PHASES+" is missing");
			}
			phases=phases.replaceAll(" ","");
			if (phases.isEmpty()) {
				throw new RuntimeException(KEY.CIRCUIT+"."+KEY.PHASES+" is empty");
			}
			for (String string:phases.split(",")) {
				String[] word = string.split("\\.", 2);
				final Sector sector = this.sectors.get(word[0]);
				if (sector==null) {
					throw new RuntimeException(KEY.CIRCUIT+"."+KEY.PHASES+": Syntax error in <"+string+">: unknown sector <"+word[0]+">" );				
				}
				if (word.length!=2) {
					throw new RuntimeException(KEY.CIRCUIT+"."+KEY.PHASES+": Syntax error in <"+string+">: expected form is <Sector.phaseName>");
				}
				final String phaseName = word[1];
				this.phases.add(new Phase() {

					@Override
					public String getName() {
						return phaseName;
					}

					@Override
					public Sector getSector() {
						return sector;
					}

					@Override
					public String toString() {
						return sector.getName()+"."+phaseName;
					}

				});
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"<html>Error while creating the circuit.<br>"
							+ "Error while creating the phases of the circuit period.<br>"
							+ e.getMessage()
							+ ".<html>",
							"Error",
							JOptionPane.ERROR_MESSAGE);			
			this.run=false;
			// TODO revoir complètement le traitement des erreurs de syntaxe du scénario.
		}			
	}

	/**
	 * Initializes the sectors of the circuit.
	 */
	private void initSectors() {
		String sectors = this.getParameter(KEY.CIRCUIT,KEY.SECTORS);
		if (sectors==null) {
			throw new RuntimeException(KEY.SECTORS+" not found");
		}
		sectors=sectors.replaceAll(" ","");
		if (sectors.isEmpty()) {
			throw new RuntimeException(KEY.SECTORS+" is empty");
		}
		final String[] sectors2 = sectors.split(",");
		for (String sectorName:sectors2) {
			String sectorQualifiedName = this.jamelParameters.get(sectorName+".type");				
			if (sectorQualifiedName==null) {
				throw new RuntimeException(sectorName+".type not found");
			}
			sectorQualifiedName.trim();
			Sector sector;
			try {
				sector = (Sector) Class.forName(sectorQualifiedName,false,ClassLoader.getSystemClassLoader()).getConstructor(String.class,Circuit.class).newInstance(sectorName,this);
				this.sectors.put(sectorName, sector);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new RuntimeException("Error while creating "+sectorName+" as "+sectorQualifiedName);					
			} catch (SecurityException e) {
				e.printStackTrace();
				throw new RuntimeException("Error while creating "+sectorName+" as "+sectorQualifiedName);					
			} catch (InstantiationException e) {
				e.printStackTrace();
				throw new RuntimeException("Error while creating "+sectorName+" as "+sectorQualifiedName);					
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Error while creating "+sectorName+" as "+sectorQualifiedName);					
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Error while creating "+sectorName+" as "+sectorQualifiedName);					
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				throw new RuntimeException("Error while creating "+sectorName+" as "+sectorQualifiedName);					
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("Error while creating "+sectorName+" as "+sectorQualifiedName);					
			}
		}
	}

	@Override
	public Object forward(String request, Object ... args) {
		return this.dispatcher.forward(request, args);
	}

	@Override
	public String getParameter(String... keys) {
		return this.jamelParameters.get(keys);
	}

	@Override
	public String[] getParameterArray(String... keys) {
		return this.jamelParameters.getArray(keys);
	}

	@Override
	public String[] getStartingWith(String prefix) {
		return this.jamelParameters.getStartingWith(prefix);
	}

	@Override
	public boolean isPaused() {
		return pause;
	}

	/**
	 * Runs the simulation.
	 */
	@Override
	public void run() {
		while (this.run) {
			if (!this.pause){
				this.doPeriod();
			}
			else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
		}
		// Ciao bye bye.
	}

}

// ***
