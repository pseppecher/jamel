package jamel.basic.data;

import jamel.util.Circuit;
import jamel.util.FileParser;
import jamel.util.Sector;

import java.awt.Component;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jfree.data.xy.XYSeries;

/**
 * The sector for managing data.
 */
public class DataManager implements Sector {

	/**
	 * A class to store string constants.
	 */
	private static class KEY {

		/** The "XYSeries" key. */ 
		private static final String XYSeries = "XYSeries";

	}

	/**
	 * The balance sheet matrix of the economy.
	 */
	private interface BalanceSheetMatrix {

		/**
		 * Returns a html representation of the balance sheet matrix.
		 * @return a html representation of the balance sheet matrix.
		 */
		String toHtml();

	}

	/** The number format. */
	final private static NumberFormat nf = NumberFormat.getInstance(Locale.US);

	/** The balance sheet matrix. */
	private final BalanceSheetMatrix balanceSheetMatrix;

	/** The balance sheet panel. */
	private final JEditorPane balanceSheetPanel;

	/** The circuit. */
	private final Circuit circuit;

	/** dataSet */
	private final Map<String,Double> dataSet = new TreeMap<String,Double>() {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		@Override
		public Double get(Object key) {
			String key2 = (String) key;
			Double val;
			if (key2.startsWith("-")) {
				val = super.get(key2.substring(1));
				if (val!=null) {
					val=-val;
				}
			}
			else {
				val = super.get(key2);
			}
			return val;
		}

	};

	/** The sector name. */
	private final String name;

	/** A collection of ?. */
	private final TreeMap<String,String[]> ratios = new TreeMap<String,String[]>();

	/** A collection of ?. */
	private final TreeMap<String,String> raw = new TreeMap<String,String>();

	/** A collection of XYSeries. */
	private final TreeMap<String,XYSeries> series = new TreeMap<String,XYSeries>();

	/** The specification. */
	private Map<String, String> details;

	/**
	 * Creates a new sector for data management.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public DataManager(String name, Circuit circuit) {
		this.name = name;
		this.circuit = circuit;
		this.details = getDataDescription();
		this.initSeries();
		this.balanceSheetMatrix = createBalanceSheetMatrix();
		this.balanceSheetPanel = createBalanceSheetPanel();
	}

	/**
	 * Returns a map that contains the description of the data. 
	 * @return a map that contains the description of the data.
	 */
	private Map<String,String> getDataDescription() {
		final String fileName = circuit.getParameter(name,"details");
		final Map<String,String> map;
		try {
			map = FileParser.parse(fileName);
			return map;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			new RuntimeException("Data description not found.");
		}
		return null;
	}

	/**
	 * Creates the balance sheet matrix.
	 * @return the new balance sheet matrix.
	 */
	private BalanceSheetMatrix createBalanceSheetMatrix() {
		final String[] sectors = FileParser.toArray(details.get("sfc.sectors"));
		final String rows[] = FileParser.toArray(details.get("sfc.rows"));
		
		final HashMap<String,String> sfcMap = new HashMap<String,String>();
		for (String sector:sectors) {
			for (String row:rows) {
				final String val = details.get("sfc."+sector+"."+row);
				if (val!=null) {
					sfcMap.put(sector+"."+row, val);
				}
			}
		}
		
		final BalanceSheetMatrix matrix = new BalanceSheetMatrix () {

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
							final Double value = dataSet.get(key);
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

		};
		return matrix;
	}

	/**
	 * Returns a JPanel containing the balance Sheet Panel.
	 * @return a JPanel.
	 */
	private JEditorPane createBalanceSheetPanel() {
		final JEditorPane jEditorPane = new JEditorPane() {
			private static final long serialVersionUID = 1L;
			{
				this.setContentType("text/html");
				this.setText(balanceSheetMatrix.toHtml());
				this.setEditable(false);
			}};
			return jEditorPane;
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
	private void initSeries() {

		final Set<String> dataToCollect = new TreeSet<String>();

		// Initializes the raw series.
		for(String key:details.keySet()) {
			if (key.startsWith("series.")) {
				final String shortKey = key.split("\\.", 2)[1];
				final String param = this.details.get(key);
				this.raw.put(shortKey, param);
				this.series.put(shortKey, new XYSeries(shortKey,false));
				dataToCollect.add(param);				
			}
		}
		
		// Initializes the ratios.
		for(String key:details.keySet()) {
			if (key.startsWith("ratios.")) {
				final String shortKey = key.split("\\.", 2)[1];
				final String[] param = FileParser.toArray(this.details.get(key));
				this.ratios.put(shortKey, param);
				this.series.put(shortKey, new XYSeries(shortKey,false));
				dataToCollect.add(param[0]);
				dataToCollect.add(param[1]);
			}
		}
		
		this.circuit.forward("dataKeys", dataToCollect.toArray());
		
	}

	/**
	 * Updates series.
	 * Called at the closure of the period. 
	 */
	private void updateSeries() {
		final int period = Circuit.getCurrentPeriod().getValue();
		for (Entry<String,String>entry:raw.entrySet()) {
			final Number value = this.dataSet.get(entry.getValue());
			if (value!=null) {
				final XYSeries series = this.series.get(entry.getKey());
				series.add(period, value);
			}
			else {
				//throw new RuntimeException("Not found: "+entry.getValue()); TODO à revoir
			}
		}
		for (Entry<String,String[]>entry:ratios.entrySet()) {
			final String[] datakey=entry.getValue();
			final Number val0 = this.dataSet.get(datakey[0]);
			final Number val1 = this.dataSet.get(datakey[1]);
			if (val0== null || val1==null) {
				//throw new RuntimeException("Null value for "+entry.getKey()+": "+datakey[0]+"="+val0+", "+datakey[1]+"="+val1);
			}
			else
				if ((Double)val1!=0) { // prevents division by zero
					this.series.get(entry.getKey()).add(period, ((Double)val0)/((Double)val1));									
				}
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
			final String text=balanceSheetMatrix.toHtml();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					balanceSheetPanel.setText(text);
				}
			});			
			this.dataSet.clear();
		}
		else {
			throw new IllegalArgumentException("Unknown phase <"+phase+">");
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object forward(String request, Object ... args) {
		final Object result;
		if (request.equals("getSeries")) {
			result = this.getSeries((String) args[0],(String) args[1]);;
		}
		else if (request.equals("putData")) {
			this.dataSet.putAll((Map<String, Double>) args[0]);
			result = null;
		}
		else if (request.equals("getBalanceSheetPanel")) {
			final Component pane = new JScrollPane(balanceSheetPanel);
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
