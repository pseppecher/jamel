package jamel.basic;

import jamel.Simulator;
import jamel.basic.util.BasicTimer;
import jamel.basic.util.JamelParameters;
import jamel.basic.util.JamelParameters.Param;
import jamel.util.Circuit;
import jamel.util.FileParser;
import jamel.util.Sector;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A basic class of the Circuit.
 */
public class BasicCircuit extends Circuit {

	/**
	 * Dispatches messages between the different components of the circuit.
	 */
	private class Dispatcher {
		
		/**
		 * A redirection.
		 */
		public class Redirection {
			/** Message. */
			public final String message;
			/** The recipient sector. */
			public final Sector sector;
			/**
			 * Creates a new redirection.
			 * @param sector the recipient.
			 * @param message the message.
			 */
			public Redirection(Sector sector, String message) {
				this.sector = sector;
				this.message = message;
			}
		}

		/** The redirections. */
		private final Map<String,Redirection> redirections = new HashMap<String,Redirection>();

		/**
		 * Forwards a request.
		 * @param request the request to be forwarded.
		 * @param args additional arguments.
		 * @return an object.
		 */
		private Object forward(String request, Object[] args) {
			
			final Object result;
			
			if (request==null) {
				throw new IllegalArgumentException("The request is null.");
			}
			
			else if (request.equals("pause")) {
				pause = true;
				result = null;
			}
			
			else if (request.equals("unpause")) {
				pause = false;
				result = null;
			}
			
			else if (redirections.containsKey(request)) {
				result = redirections.get(request).sector.forward(redirections.get(request).message, args);
			}
			
			else {
				final String def1  = getParameter(KEY.CIRCUIT,KEY.REDIRECT,request);
				if (def1==null) {
					throw new IllegalArgumentException("Unknown request: "+request);
				}
				final String[] definition = def1.split("\\.",2);
				if (sectors.containsKey(definition[0])){
					final Sector sector = sectors.get(definition[0]);
					final String method = definition[1];
					this.redirections.put(request,new Redirection(sector,method));
					result = sector.forward(method,args);
				}
				else {
					throw new IllegalArgumentException("Unknown sector: "+definition[0]);
				}
			}
			return result;
		}

	}

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

		/** The title of the info panel. */
		public static final String INFO = "Info";

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

	/**
	 * Returns a new info panel.
	 * @return a new info panel.
	 */
	private static Component getNewInfoPanel() {
		final Component jEditorPane = new JEditorPane() {
			private static final long serialVersionUID = 1L;
			{
				String infoString = FileParser.readResourceFile("info.html");
				this.setContentType("text/html");
				this.setText("<center><h3>Jamel2 ("+Simulator.version+")</h3>"+infoString+"</center>");
				this.setEditable(false);
				this.addHyperlinkListener(new HyperlinkListener() {
					@Override
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
							try {
								java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
							} catch (Exception ex) {
								ex.printStackTrace();
							}	        
					}
				});
			}
		};
		final JScrollPane pane = new JScrollPane(jEditorPane);
		pane.setName(KEY.INFO);
		return pane;
	}

	/** 
	 * The request dispatcher.
	 */
	private final Dispatcher dispatcher = new Dispatcher();

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

	/** The start. */
	private final Date start;

	/**
	 * Creates a new basic circuit.
	 * @param jamelParameters a map of parameters for the new circuit.
	 */
	public BasicCircuit(final JamelParameters jamelParameters) {
		super(
				new BasicTimer(-1),
				new Random() {
					private static final long serialVersionUID = 1L;
					{this.setSeed(Integer.parseInt(jamelParameters.get(KEY.CIRCUIT,KEY.RANDOM_SEED)));}}
				);
		this.start = new Date();
		this.jamelParameters = jamelParameters;
		this.initSectors();
		this.initPhases();
		this.forward("addPanel", this.getNewParamPanel()); // TODO ne pas utiliser forward
		this.forward("addPanel", getNewInfoPanel()); // TODO ne pas utiliser forward
	}

	/**
	 * Executes the events of the simulation.
	 */
	private void doEvents() {
		final String event = this.jamelParameters.get("Circuit.events."+getCurrentPeriod().intValue());
		if (event!=null) {
			final String[] events = JamelParameters.split(event,",");
			for(final String string:events) {

				if (string.equals("pause")) {
					System.out.println("Duration: "+((new Date()).getTime()-start.getTime())/1000+" s.");
					this.pause=true;
					for(Sector sector:sectors.values()) {
						sector.pause();
					}
				}

				else if (string.startsWith("change.")) {
					final String[] truc1 = JamelParameters.split(string.substring(7),"=");
					final String[] truc2 = truc1[0].split("\\.", 2);
					this.jamelParameters.put(truc1[0], truc1[1]);
					final Sector sector = this.sectors.get(truc2[0]);
					if (sector!=null) {
						sector.forward("change in parameters");					
					}
					else {
						throw new RuntimeException("Sector not found: "+truc2[0]);
					}
				}

				else {
					final String[] truc1 = JamelParameters.split(string,"=");
					final String[] truc2 = truc1[0].split("\\.", 2);
					final Sector sector = this.sectors.get(truc2[0]);
					if (sector!=null) {
						sector.forward(truc2[1],truc1[1]);
					}
					else {
						throw new RuntimeException("Error while parsing the event <"+event+">: Sector not found <"+truc2[0]+">.");
					}
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
			}
		}
	}

	/**
	 * Returns a new parameters panel.
	 * @return a new parameters panel.
	 */
	@SuppressWarnings("serial")
	private Component getNewParamPanel() {
		return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT){{
			this.setBorder(null);
			this.setDividerSize(5);
			final JTextArea fieldDetailsTextArea = new JTextArea() {{
				this.setEditable(false);
				this.setBackground(Color.WHITE);
			}};
			final JPanel rightPanel = new JPanel() {{
				this.setLayout(new FlowLayout(FlowLayout.LEFT));
				this.add(fieldDetailsTextArea);
				this.setBackground(Color.WHITE);
			}};
			final JPanel leftPanel = new JPanel() {{
				this.setLayout(new FlowLayout(FlowLayout.LEFT));
				final JTree jTree = jamelParameters.getJTree();
				jTree.addTreeSelectionListener(new TreeSelectionListener() {
					@Override
					public void valueChanged(TreeSelectionEvent e) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
						if (node != null) {
							final Param nodeInfo = (Param) node.getUserObject();
							fieldDetailsTextArea.setText(nodeInfo.getValue());
						}
					}
				});
				this.add(jTree);
				this.setBackground(Color.white);
			}};
			this.setName("Parameters");
			this.setResizeWeight(0.3);
			this.setLeftComponent(new JScrollPane(leftPanel));
			this.setRightComponent(new JScrollPane(rightPanel));
		}};
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
