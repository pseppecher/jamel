package jamel.basic.data;

import jamel.Simulator;
import jamel.util.Circuit;
import jamel.util.FileParser;
import jamel.util.Sector;

import java.awt.Component;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

/**
 * The sector for managing data.
 */
public class DataManager implements Sector {

	/**
	 * The balance sheet matrix of the economy.
	 */
	private interface BalanceSheetMatrix {

		/**
		 * Returns a panel containing a representation of the balance sheet matrix.
		 * @return a panel containing a representation of the balance sheet matrix.
		 */
		Component getPanel();

		/**
		 * Returns a html representation of the balance sheet matrix.
		 * @return a html representation of the balance sheet matrix.
		 */
		String toHtml();

		/**
		 * Updates the representation of the balance sheet matrix.
		 */
		void update();

	}

	/**
	 * A class to store string constants.
	 */
	private static class KEY {

		/** The "XYSeries" key. */ 
		private static final String XYSeries = "XYSeries";

	}

	/** The number format. */
	final private static NumberFormat nf = NumberFormat.getInstance(Locale.US);

	/** The balance sheet matrix. */
	private final BalanceSheetMatrix balanceSheetMatrix;

	/** The circuit. */
	private final Circuit circuit;

	/** The macro dataset. */
	private final MacroDataset macroDataset = new BasicMacroDataset();

	/** The sector name. */
	private final String name;

	/** Description of the ratio series. */
	private final TreeMap<String,String[]> ratios = new TreeMap<String,String[]>();

	/** Description of the raw series. */
	private final TreeMap<String,String> raw = new TreeMap<String,String>();

	/** Description of the scatter series. */
	private final TreeMap<String,String[]> scatter = new TreeMap<String,String[]>();

	/** A collection of XYSeries. */
	private final TreeMap<String,JamelXYSeries> series = new TreeMap<String,JamelXYSeries>();

	/**
	 * Creates a new sector for data management.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public DataManager(String name, Circuit circuit) {
		this.name = name;
		this.circuit = circuit;
		this.initSeries();
		this.balanceSheetMatrix = createBalanceSheetMatrix();
	}

	/**
	 * Creates the balance sheet matrix.
	 * @return the new balance sheet matrix.
	 */
	private BalanceSheetMatrix createBalanceSheetMatrix() {

		final Map<String, String> matrixConfig = getConfig("config.matrix");

		final String[] sectors = FileParser.toArray(matrixConfig.get("sectors"));

		final String rows[] = FileParser.toArray(matrixConfig.get("rows"));

		final HashMap<String,String> sfcMap = new HashMap<String,String>();
		for (String sector:sectors) {
			for (String row:rows) {
				final String val = matrixConfig.get(sector+"."+row);
				if (val!=null) {
					sfcMap.put(sector+"."+row, val);
				}
			}
		}

		// A JPanel containing the balance Sheet Panel.
		final JEditorPane jEditorPane = new JEditorPane() {
			private static final long serialVersionUID = 1L;
			{
				this.setContentType("text/html");
				this.setText("Hello world");
				this.setEditable(false);
			}
		};

		final BalanceSheetMatrix matrix = new BalanceSheetMatrix () {

			private final JEditorPane balanceSheetPanel = jEditorPane;

			@Override
			public Component getPanel() {
				return this.balanceSheetPanel;
			}

			@Override
			public String toHtml() {

				final StringBuffer table = new StringBuffer();
				final int period = Circuit.getCurrentPeriod().getValue();
				table.append("<STYLE TYPE=\"text/css\">.boldtable, .boldtable TD, .boldtable TH" +
						"{font-family:sans-serif;font-size:12pt;}" +
						"</STYLE>");		
				table.append("<BR><BR><BR><BR><TABLE border=0 align=center class=boldtable cellspacing=10>");
				table.append("<CAPTION>Balance sheet matrix (period "+ period +")</CAPTION>");
				table.append("<TR><TD colspan=5><HR>");		

				table.append("<TR><TH WIDTH=120>");
				for(String sector:sectors) {
					table.append("<TH WIDTH=110 align=right>" + sector);
				}
				table.append("<TH WIDTH=110 align=right>" + "Sum");
				table.append("<TR><TD colspan=5><HR>");

				final HashMap<String,Double> sumSector = new HashMap<String,Double>();
				for (String sector:sectors) {
					sumSector.put(sector, 0.);
				}
				sumSector.put("sum", 0.);

				for(String row:rows) {
					table.append("<TR><TH>" + row);
					double sum = 0l;
					for(String sector:sectors) {
						final String key = sfcMap.get(sector+"."+row);
						table.append("<TD align=right>");						
						if (key!=null) {
							final Double value = macroDataset.get(key);
							if (value !=null) {
								table.append(nf.format(value));
								sum+=value;
								sumSector.put(sector, sumSector.get(sector)+value);
							}
							else {
								table.append(key+" not found");							
							}
						}
					}
					table.append("<TD align=right>"+nf.format(sum));
					sumSector.put("sum", sumSector.get("sum")+sum);
				}

				table.append("<TR><TD colspan=5><HR>");
				table.append("<TR><TH>Sum");
				for (String sector:sectors) {
					table.append("<TD align=right>"+nf.format(sumSector.get(sector)));
				}
				table.append("<TD align=right>"+nf.format(sumSector.get("sum")));
				table.append("<TR><TD colspan=5><HR>");
				table.append("</TABLE>");
				return table.toString();
			}

			@Override
			public void update() {
				final String text=this.toHtml();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						balanceSheetPanel.setText(text);
					}
				});			
			}

		};
		return matrix;
	}

	/**
	 * Returns a map that contains the description of the configuration. 
	 * @param key the key.
	 * @return a map that contains the description of the configuration.
	 */
	private Map<String,String> getConfig(String key) {
		final String fileName = circuit.getParameter(name,key);
		Map<String,String> map = null;
		try {
			map = FileParser.parse(fileName);
		} catch (FileNotFoundException e) {
			Simulator.showErrorDialog("DataManager: Data description not found.");
			e.printStackTrace();
			new RuntimeException("DataManager: Data description not found.");
		}
		return map;
	}

	/**
	 * Returns a series.
	 * @param message a string defining the data to return (XYSeries or other ?).
	 * @param args the key whose associated value is to be returned. 
	 * @return the series.
	 */
	private XYSeries getSeries(String message, String args) {
		final XYSeries result;
		if (message.equals(KEY.XYSeries)) {
			XYSeries series=this.series.get(args);
			if (series!=null) {
				result=series;
			} else {
				result=null;
			}
		} else {
			throw new RuntimeException("Not yet implemented: "+message);
			// TODO IMPLEMENT le cas où on veut autre chose que des XYseries...
		}
		return result;
	}

	/**
	 * Initializes the series and ratios.
	 */
	private void initSeries() { // TODO: CLEAN UP

		final Map<String, String> config = getConfig("config.data");

		final Set<String> dataToCollect = new TreeSet<String>();

		// Initializes the raw series.
		for(String key:config.keySet()) {
			if (key.startsWith("series.")) {
				final String shortKey = key.split("\\.", 2)[1];
				final String param = config.get(key);
				this.raw.put(shortKey, param);
				this.series.put(shortKey, new JamelXYSeries(shortKey));
				dataToCollect.add(param);				
			}
		}

		// Initializes the ratios.
		for(String key:config.keySet()) {
			if (key.startsWith("ratios.")) {
				final String shortKey = key.split("\\.", 2)[1];
				final String[] param = FileParser.toArray(config.get(key));
				this.ratios.put(shortKey, param);
				this.series.put(shortKey, new JamelXYSeries(shortKey));
				dataToCollect.add(param[0]);
				dataToCollect.add(param[1]);
			}
		}

		// Initializes the scatters.
		for(String key:config.keySet()) {
			if (key.startsWith("scatterSeries.")) {
				final String shortKey = key.split("\\.", 2)[1];
				final String[] param = FileParser.toArray(config.get(key));
				this.scatter.put(shortKey, param);
				this.series.put(shortKey, new JamelXYSeries(shortKey));
			}
		}

	}

	/**
	 * Updates series.
	 * Called at the closure of the period. 
	 */
	private void updateSeries() {

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override public void run() {
					final int period = Circuit.getCurrentPeriod().getValue();

					for (Entry<String,String>entry:raw.entrySet()) {
						final Double value = macroDataset.get(entry.getValue());
						if (value!=null) {
							DataManager.this.series.get(entry.getKey()).add(period, value);
						}
						else {
							//throw new RuntimeException("Not found: "+entry.getValue()); TODO à revoir
						}
					}

					for (Entry<String,String[]>entry:ratios.entrySet()) {
						final String[] datakey=entry.getValue();
						final Number val0 = macroDataset.get(datakey[0]);
						final Number val1 = macroDataset.get(datakey[1]);
						if (val0== null || val1==null) {
							//throw new RuntimeException("Null value for "+entry.getKey()+": "+datakey[0]+"="+val0+", "+datakey[1]+"="+val1);
						}
						else
							if ((Double)val1!=0) { // prevents division by zero
								series.get(entry.getKey()).add(period, ((Double)val0)/((Double)val1));									
							}
					}

					for (Entry<String,String[]>entry:scatter.entrySet()) {
						final List<XYDataItem> data = macroDataset.getScatter(entry.getValue()[0],entry.getValue()[1],entry.getValue()[2]);
						series.get(entry.getKey()).update(data );
					}

				}
			}) ;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		/*for(Entry<String,XYSeries> entry: this.series.entrySet()){
			final String key=entry.getKey();
			if (key.contains("&&")) {
				final String serieskey[]=key.split("&&",2);
				final XYSeries series0 = this.series.get(serieskey[0]);
				final XYSeries series1 = this.series.get(serieskey[1]);
				if (series0!=null && series1!=null) {
					final Period period = this.circuit.getPeriod();
					final int index0 = series0.getItemCount()-1;
					final int index1 = series1.getItemCount()-1;
					if (index0>=0 && index1>=0) {
						final Number t0 = series0.getX(index0);
						final Number t1 = series1.getX(index1);
						if (period.equals(t0.intValue()) && period.equals(t1.intValue())) {
							entry.getValue().add(series0.getY(index0), series1.getY(index1));
						}
					}
				}
			}
		}*/

	}

	@Override
	public boolean doPhase(String phase) {
		if (phase.equals("closure")) {
			updateSeries();
			balanceSheetMatrix.update();
			this.macroDataset.clear();
		}
		else {
			throw new IllegalArgumentException("Unknown phase <"+phase+">");
		}
		return true;
	}

	@Override
	public Object forward(String request, Object ... args) {

		final Object result;

		if (request.equals("getSeries")) {
			result = this.getSeries((String) args[0],(String) args[1]);;
		}

		else if (request.equals("putData")) {
			this.macroDataset.putData((String) args[0], (SectorDataset) args[1]);
			result = null;
		}

		else if (request.equals("getBalanceSheetPanel")) {
			final Component pane = new JScrollPane(this.balanceSheetMatrix.getPanel());
			pane.setName("Balance sheet");
			result = pane;
		}

		else {
			throw new IllegalArgumentException("Unknown request <"+request+">");			
		}

		return result;

	}

	/**
	 * Returns the sector name.
	 * @return the sector name.
	 */
	@Override
	public String getName() {
		return name;
	}	

	@Override
	public void pause() {
		// Does nothing.		
	}

}

// ***
