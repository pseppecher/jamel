package jamel.basic;

import jamel.Simulator;
import jamel.basic.data.BasicDataManager;
import jamel.basic.gui.BasicChartManager;
import jamel.basic.gui.ChartManager;
import jamel.basic.gui.ControlPanel;
import jamel.basic.gui.GUI;
import jamel.basic.sector.Phase;
import jamel.basic.sector.Sector;
import jamel.basic.util.BasicTimer;
import jamel.basic.util.InitializationException;
import jamel.basic.util.Period;
import jamel.basic.util.Timer;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A basic class of the {@link Circuit}.
 */
public class BasicCircuit implements Circuit {

	/**
	 * Initializes and returns a new chart manager.
	 * @param dataManager the parent data manager.
	 * @param settings a XML element with the settings.
	 * @param path the path of the scenario file.
	 * @return a new chart manager.
	 * @throws InitializationException If something goes wrong.
	 */
	private static ChartManager getNewChartManager(final BasicDataManager dataManager, Element settings, String path) throws InitializationException {
		ChartManager chartManager = null;
		final String fileName = settings.getAttribute("chartsConfigFile");
		if (fileName != null) {
			final File file = new File(path+"/"+fileName);
			final Element root;
			try {
				root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement();
			} catch (Exception e) {
				throw new InitializationException("Something goes wrong while creating the ChartManager.", e);
			}
			if (!"charts".equals(root.getNodeName())) {
				throw new InitializationException("The root node of the scenario file must be named <charts>.");
			}			
			try {
				chartManager = new BasicChartManager(root,dataManager);
			} catch (Exception e) {
				throw new InitializationException("Something goes wrong while parsing this file: "+file.getAbsolutePath(),e);
			}
		}
		else {
			chartManager = null;
		}
		return chartManager;
	}

	/**
	 * Returns the events.
	 * @param params a XML element with the description of the events.
	 * @return a map that contains the events.
	 * @throws InitializationException If something goes wrong.
	 */
	private static Map<Integer,List<Element>> getNewEvents(Element params) throws InitializationException {
		final HashMap<Integer,List<Element>> map = new HashMap<Integer,List<Element>>();
		final Element eventsNode = (Element) params.getElementsByTagName("events").item(0);
		if (eventsNode!=null) {
			final NodeList eventsList = eventsNode.getChildNodes();
			for (int i = 0; i<eventsList.getLength(); i++) {
				final Node item = eventsList.item(i);  
				if (item.getNodeType()==Node.ELEMENT_NODE) {
					final Element element = (Element) item;
					final String periodKey = element.getAttribute("period");	
					if ("".equals(periodKey)) {
						throw new InitializationException("Malformed event: Missing attribute: period");					
					}
					final Integer period = Integer.parseInt(periodKey);
					if (map.containsKey(period)) {
						final List<Element> list = map.get(period);
						list.add(element);
					}
					else {			
						final List<Element> list = new LinkedList<Element>();
						list.add(element);
						map.put(period, list);
					}
				}
			}			
		}
		return map;
	}

	/**
	 * Initializes and returns a new GUI.
	 * @param name the name of the new GUI.
	 * @param controlPanel the control panel.
	 * @return the new GUI.
	 */
	private static GUI getNewGUI(String name, Component controlPanel) {
		return new GUI(name,controlPanel);
	}

	/**
	 * Initializes and returns a new info panel.
	 * @return a new info panel.
	 */
	private static Component getNewInfoPanel() {
		final Component jEditorPane = new JEditorPane() {
			{
				String infoString = readResourceFile("info.html");
				this.setContentType("text/html");
				this.setText("<center><h3>Jamel v3 beta ("+Simulator.version+")</h3>"+infoString+"</center>");
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
		pane.setName("Info");
		return pane;
	}

	/**
	 * Initializes and returns the list of phases of the circuit period.
	 * @param param a XML element with the phases description.
	 * @param sectors a collection (a Map<name,sector>)of sectors.
	 * @return a list of phases.
	 * @throws InitializationException If an <code>InitializationException</code> occurs.
	 */
	private static LinkedList<Phase> getNewPhases(Map<String, Sector> sectors, Element param) throws InitializationException {
		final LinkedList<Phase> result = new LinkedList<Phase>();
		final Element phasesNode = (Element) param.getElementsByTagName("phases").item(0);
		final NodeList phases = phasesNode.getChildNodes();
		for (int i = 0; i<phases.getLength(); i++) {
			final Node node = phases.item(i); 
			if (node.getNodeType()==Node.ELEMENT_NODE) {
				final Element element = (Element) node;
				final String sectorName = element.getNodeName();
				final String phaseName = ((Element)phases.item(i)).getAttribute("action");
				final Sector sector = sectors.get(sectorName);
				if (sector==null) {
					throw new InitializationException("Error while parsing phases: sector: "+sectorName+" (sector not found)." );				
				}
				final Phase newPhase = sector.getPhase(phaseName);
				if (newPhase==null) {
					throw new InitializationException("Error while parsing phases: phase: \""+phaseName+"\" (sector: \""+sectorName+"\").");				
				}
				result.add(newPhase);

			}
		}
		return result;
	}

	/**
	 * Returns a new Random.
	 * @param settings a XML element that contains the settings.
	 * @return a new Random.
	 * @throws InitializationException If something goes wrong.
	 */
	private static Random getNewRandom(Element settings) throws InitializationException {
		Random result = null;
		final String randomSeed = settings.getAttribute("randomSeed");
		if ("".equals(randomSeed)) {
			throw new InitializationException("Circuit settings: Missing attribute: \"randomSeed\"");
		}
		try {
			final int seed = Integer.parseInt(randomSeed);
			result = new Random(seed);
		} catch (NumberFormatException e) {
			throw new InitializationException("Something went wrong while parsing the tag \"randomSeed\".",e);			
		}
		return result;
	}

	/**
	 * Initializes and returns the sectors.
	 * @param circuit the circuit.
	 * @param params the parameters.
	 * @return a map <name of the sector, sector>.
	 * @throws InitializationException If something goes wrong.
	 */
	private static LinkedHashMap<String,Sector> getNewSectors(Circuit circuit, Element params) throws InitializationException {
		final LinkedHashMap<String,Sector> result = new LinkedHashMap<String,Sector>();
		final Element sectorsNode = (Element) params.getElementsByTagName("sectors").item(0);
		final NodeList sectorsList = sectorsNode.getChildNodes();
		for (int i = 0; i<sectorsList.getLength(); i++) {
			final Node item = sectorsList.item(i);  
			if (item.getNodeType()==Node.ELEMENT_NODE) {
				final Element element = (Element) item;
				final String sectorName = element.getNodeName();
				final String sectorQualifiedName = element.getAttribute("type");	
				if (sectorQualifiedName==null) {
					throw new InitializationException(sectorName+".type not found");					
				}
				Sector sector = null;
				try {
					sector = (Sector) Class.forName(sectorQualifiedName,false,ClassLoader.getSystemClassLoader()).getConstructor(String.class,Circuit.class).newInstance(sectorName,circuit);
					result.put(sectorName, sector);
				} catch (Exception e) {
					throw new InitializationException("Error while creating \""+sectorName+"\" as \""+sectorQualifiedName+"\"", e);					
				}
			}
		}
		return result;
	}

	/**
	 * Returns the length of time to sleep in milliseconds.
	 * @param settings  an XML element that contains the circuit settings.
	 * @return the length of time to sleep in milliseconds.
	 */
	private static Integer getSleep(Element settings) {
		Integer result;
		final String sleep = settings.getAttribute("sleep");
		if ("".equals(sleep)) {
			result=null;
		}
		else {
			result = Integer.parseInt(sleep);
		}
		return result;
	}

	/**
	 * Initializes the sectors.
	 * Must be called only after creating each sector.
	 * @param sectors the sectors to be initialized.
	 * @param params a XML element that contains the parameters of each sector.
	 * @throws InitializationException If something goes wrong.
	 */
	private static void initSectors(Map<String, Sector> sectors, Element params) throws InitializationException {
		final NodeList nodeList = params.getElementsByTagName("sectors").item(0).getChildNodes();
		for(int i=0; i<nodeList.getLength(); i++) {
			final Node node = nodeList.item(i);
			if (node.getNodeType()==Node.ELEMENT_NODE) {
				final Element sectorNode = (Element) node;
				final Sector sector = sectors.get(sectorNode.getNodeName());
				sector.init(sectorNode);
			}
		}
	}

	/**
	 * Reads the specified file in resource and returns its contain as a String.
	 * @param filename  the name of the resource file.
	 * @return a String.
	 */
	private static String readResourceFile(String filename) {
		BufferedReader br=new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/"+filename)));
		String line;		
		String string="";
		final String rc = System.getProperty("line.separator");
		try {
			while ((line=br.readLine())!=null){
				string+=line+rc;
			}
			br.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return string;
	}

	/**
	 * Causes this thread to sleep (temporarily cease execution) for the specified number of milliseconds.
	 * Useful to slow down the simulation.
	 * @param millis  the length of time to sleep in milliseconds.
	 */
	private static void sleep(Integer millis) {
		if (millis!=null) {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				e.printStackTrace();			
			}			
		}
	}

	/**
	 * Returns a XML element that contains the settings of the circuit.
	 * @param params a XML element that contains the settings.
	 * @return a XML element that contains the settings of the circuit.
	 */
	protected static Element getSettings(Element params) {
		final Element settings = (Element) params.getElementsByTagName("settings").item(0);
		// TODO tester la prŽsence de settings
		return settings;
	}

	/** The chart manager. */
	private ChartManager chartManager;

	/** controlPanel */
	private final ControlPanel controlPanel;

	/** The events. */
	private final Map<Integer, List<Element>> events;

	/** The name of the scenario. */
	final private String name;

	/** A flag that indicates if the simulation is paused or not. */
	private boolean pause;

	/** The phases of the circuit. */
	private final List<Phase> phases;

	/** The random. */
	private final Random random;

	/** A flag that indicates if the simulation is running or not. */
	private boolean run = true;

	/** The sectors of the circuit. */
	private final Map<String,Sector> sectors;

	/** The length of time to sleep in milliseconds. */
	final private Integer sleep;

	/** The macroeconomic data manager. */
	protected final BasicDataManager dataManager;

	/** The graphical user interface. */
	protected final GUI gui;

	/** The timer. */
	protected final BasicTimer timer;

	/**
	 * Creates a new basic circuit.
	 * @param elem an XML element with the parameters for the new circuit.
	 * @param path the path to the scenario file.
	 * @param name the name of the scenario file.
	 * @throws InitializationException If something goes wrong.
	 */
	public BasicCircuit(final Element elem, String path, String name) throws InitializationException {
		this.name=name;
		final String title=elem.getAttribute("title");
		this.timer = new BasicTimer(0);
		final Element settings = getSettings(elem);
		this.random = getNewRandom(settings);
		this.sleep = getSleep(settings);
		this.sectors = getNewSectors(this, elem);
		initSectors(this.sectors,elem);
		this.phases = getNewPhases(this.sectors, elem);
		this.events = getNewEvents(elem);
		this.controlPanel = getNewControlPanel();
		this.dataManager = getNewDataManager(settings, timer, path, name);
		this.chartManager = getNewChartManager(this.dataManager, settings, path);
		this.gui = getNewGUI(title + " ("+name+")",this.controlPanel);
		this.gui.addPanel(this.chartManager.getPanelList());
		this.gui.addPanel(getNewInfoPanel());
	}

	/**
	 * Executes the events of the simulation.
	 */
	private void doEvents() {
		final List<Element> eventList = this.events.get(this.timer.getPeriod().intValue());
		if (eventList!=null) {
			for(Element event:eventList) {
				final String markerMessage = event.getAttribute("marker");
				if (!"".equals(markerMessage)) {
					this.chartManager.addMarker(markerMessage,this.timer.getPeriod().intValue());
				}
				final String sectorName = event.getAttribute("sector");
				if ("".equals(sectorName)) {
					this.doEvents(event);
				}
				else {
					final Sector sector = this.sectors.get(sectorName);
					sector.doEvent(event);
				}
			}
		}
	}

	/**
	 * Executes the specified event.
	 * @param event a XML element that describes the event to be executed.
	 */
	private void doEvents(Element event) {
		if (event.getNodeName().equals("pause")) {
			this.pause=true;
			this.controlPanel.repaint();
		}		
	}

	/**
	 * Pause.
	 */
	private void doPause() {
		while (this.pause) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();			
			}
		}
	}

	/**
	 * Executes a period of the circuit.
	 */
	private void doPeriod() {
		this.updateData();
		sleep(this.sleep);
		this.doEvents();
		this.doPause();
		timer.next();
		for(Phase phase:phases) {
			phase.run();
		}
	}

	/**
	 * Creates and returns the control panel.
	 * @return the control panel.
	 */
	private ControlPanel getNewControlPanel() {
		return new ControlPanel(this);
	}

	/**
	 * @param settings a XML element with the settings.
	 * @param timer the timer.
	 * @param path the path of scenario file.
	 * @param name the name of the scenario file.
	 * @return a new basic data manager.
	 * @throws InitializationException If something goes wrong.
	 */
	protected BasicDataManager getNewDataManager(Element settings, Timer timer, String path, String name) throws InitializationException {
		return new BasicDataManager(settings, timer, path, name);
	}

	/**
	 * Updates data at the beginning of the period.
	 */
	protected void updateData() {
		for(Sector sector:this.sectors.values()) {
			this.dataManager.putData(sector.getName(), sector.getDataset());
		}
		this.dataManager.update();
	}

	@Override
	public Period getCurrentPeriod() {
		return this.timer.getPeriod();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Random getRandom() {
		return this.random;
	}

	@Override
	public Sector getSector(String name) {
		return this.sectors.get(name);
	}

	@Override
	public long getSimulationID() {
		return Simulator.getSimulationID();
	}

	@Override
	public Timer getTimer() {
		return this.timer;
	}

	@Override
	public boolean isPaused() {
		return pause;
	}

	@Override
	public void pause(boolean b) {
		this.pause=b;
	}

	/**
	 * Runs the simulation.
	 */
	@Override
	public void run() {
		while (this.run) {
			this.doPeriod();
		}
		// Ciao bye bye.
	}

	@Override
	public void warning(String message, String toolTipText) {
		this.controlPanel.warning(message,toolTipText);
	}

}

// ***
