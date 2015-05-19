package jamel.basic.data;

import jamel.Simulator;
import jamel.basic.data.dataSets.BasicMacroDataset;
import jamel.basic.data.dataSets.JamelXYSeries;
import jamel.basic.data.dataSets.MacroDataset;
import jamel.basic.data.dataSets.SectorDataset;
import jamel.basic.data.util.AbstractBalanceSheetMatrix;
import jamel.basic.data.util.AbstractChartManager;
import jamel.basic.data.util.AbstractDataValidator;
import jamel.basic.data.util.BalanceSheetMatrix;
import jamel.basic.data.util.ChartManager;
import jamel.basic.data.util.DataValidator;
import jamel.util.Circuit;
import jamel.util.Sector;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.xml.sax.SAXException;

/**
 * The sector for managing data.
 */
public class DataManager implements Sector {

	/**
	 * A convenient class to store String constants.
	 */
	@SuppressWarnings("javadoc") private static class KEY {
		public static final String MATRIX_FILE_CONFIG = "config.matrix";
		public static final String VALIDATION_FILE_CONFIG = "config.validation";
	}

	/** The chart manager. */
	private final ChartManager chartManager;

	/** The balance sheet matrix. */
	private final BalanceSheetMatrix balanceSheetMatrix;

	/** The circuit. */
	private final Circuit circuit;

	/** The data validator. */
	final private DataValidator dataValidator;

	/** The macro dataset. */
	private final MacroDataset macroDataset = new BasicMacroDataset();

	/** The sector name. */
	private final String name;

	/** Description of the scatter series. */
	private final Map<String,String[]> scatterSeriesDescription = new LinkedHashMap<String,String[]>();

	/** A collection of XYSeries. */
	private final Map<String,JamelXYSeries> series = new LinkedHashMap<String,JamelXYSeries>();

	/** Description of the time series. */
	private final List<String> timeSeriesDescription = new LinkedList<String>();

	/**
	 * Creates a new sector for data management.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public DataManager(String name, Circuit circuit) {
		this.name = name;
		this.circuit = circuit;
		this.chartManager = getNewChartManager();
		if (this.chartManager!=null) {
			for(final Component panel: this.chartManager.getChartPanelList()) {
				circuit.forward("addPanel", panel);				
			}
		}
		this.balanceSheetMatrix = getNewBalanceSheetMatrix();
		if (this.balanceSheetMatrix!=null) {
			circuit.forward("addPanel", this.balanceSheetMatrix.getPanel());
		}
		this.dataValidator = getNewDataValidator();
		if (this.dataValidator!=null) {
			circuit.forward("addPanel", this.dataValidator.getPanel());
		}
	}

	/**
	 * Creates the balance sheet matrix.
	 * @return the new balance sheet matrix.
	 */
	private BalanceSheetMatrix getNewBalanceSheetMatrix() {		
		BalanceSheetMatrix chartManager;
		final String fileName = circuit.getParameter(name,KEY.MATRIX_FILE_CONFIG);
		if (fileName != null) {
			final File file = new File(Simulator.getScenarioFile().getParent()+"/"+fileName);
			try {
				chartManager = new AbstractBalanceSheetMatrix(file){
					@Override protected Double getValue(String key) {
						return macroDataset.get(key);
					}
				};
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				chartManager = null;
			}
		}
		else {
			chartManager = null;
		}
		return chartManager;
	}

	/**
	 * Creates and returns a new chart manager.
	 * @return a new chart manager.
	 */
	private ChartManager getNewChartManager() {
		ChartManager chartManager;
		final String fileName = circuit.getParameter(name,"config.charts");
		if (fileName != null) {
			final File file = new File(Simulator.getScenarioFile().getParent()+"/"+fileName);
			try {
				chartManager = new AbstractChartManager(file){
					@Override protected XYSeries getScatterSeries(String seriesX, String seriesY) {
						return DataManager.this.getScatterSeries(seriesX,seriesY);
					}
					@Override protected XYSeries getSeries(String serieskey) {
						return DataManager.this.getSeries(serieskey);
					}
				};
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				chartManager = null;
			} catch (SAXException e) {
				e.printStackTrace();
				chartManager = null;
			} catch (IOException e) {
				e.printStackTrace();
				chartManager = null;
			}
		}
		else {
			chartManager = null;
		}
		return chartManager;
	}

	/**
	 * Creates and returns a new data validator.
	 * @return a new data validator.
	 */
	private DataValidator getNewDataValidator() {
		DataValidator result;
		final String validationConfigFileName = circuit.getParameter(DataManager.this.name,KEY.VALIDATION_FILE_CONFIG);
		if (validationConfigFileName!=null) {
			final File file = new File(Simulator.getScenarioFile().getParent()+"/"+validationConfigFileName);
			result = new AbstractDataValidator(file){
				@Override public Double getValue(String key) {
					return DataManager.this.macroDataset.get(key);
				}
			};
		}
		else {
			result=null;
		}
		return result;
	}

	/**
	 * Creates and returns a new scatter series.
	 * @param name the name of the series.
	 * @param keyX the key for x data.
	 * @param keyY the key for y data.
	 * @return a new scatter series.
	 */
	private XYSeries getNewScatterSeries(String name, String keyX, String keyY) {
		final JamelXYSeries result = new JamelXYSeries(name);
		final String[] val = {keyX, keyY};
		this.scatterSeriesDescription.put(name, val);
		this.series.put(name, result);				
		return result;
	}

	/**
	 * Creates and returns a new series.
	 * @param name the name of the series.
	 * @return a new series.
	 */
	private XYSeries getNewSeries(String name) {
		final JamelXYSeries result = new JamelXYSeries(name);
		this.timeSeriesDescription.add(name);
		this.series.put(name, result);				
		return result;
	}

	/**
	 * Returns a scatter series.
	 * @param seriesX the key for X data. 
	 * @param seriesY the key for Y data.
	 * @return the scatter series.
	 */
	private XYSeries getScatterSeries(String seriesX, String seriesY) {
		final XYSeries result;
		final String key = seriesX+"&"+seriesY;
		XYSeries series=this.series.get(key);
		if (series!=null) {
			result=series;
		} else {
			result=getNewScatterSeries(key,seriesX,seriesY);
		}
		return result;
	}

	/**
	 * Returns a series.
	 * @param seriesKey the key whose associated series is to be returned. 
	 * @return the series.
	 */
	private XYSeries getSeries(String seriesKey) {
		final XYSeries result;
		XYSeries series=this.series.get(seriesKey);
		if (series!=null) {
			result=series;
		} else {
			result=getNewSeries(seriesKey);
		}
		return result;
	}

	/**
	 * Updates series.
	 * Called at the closure of the period. 
	 */
	private void updateSeries() {

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override public void run() {
					final int period = Circuit.getCurrentPeriod().intValue();

					for (String seriesName:timeSeriesDescription) {
						final Double value = macroDataset.get(seriesName);
						if (value!=null) {
							DataManager.this.series.get(seriesName).add(period, value);
						}
						else {
							//throw new RuntimeException("Not found: "+seriesName); // TODO à revoir
						}
					}

					for (Entry<String,String[]>entry:scatterSeriesDescription.entrySet()) {
						final List<XYDataItem> data = macroDataset.getScatter(entry.getValue()[0],entry.getValue()[1]);
						series.get(entry.getKey()).update(data );
					}

				}
			}) ;
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException();
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
			if (this.balanceSheetMatrix!=null) {
				this.balanceSheetMatrix.update();
			}
			if (this.dataValidator!=null) {
				this.dataValidator.CheckConsistency();
			}
			this.macroDataset.clear();
		}
		else {
			throw new IllegalArgumentException("Unknown phase <"+phase+">");
		}
		return true;
	}

	@Override
	public Object forward(String request, Object ... args) {

		if (request==null) {
			throw new IllegalArgumentException("The request is null.");			
		}

		final Object result;

		if (request.equals("marker")) {
			this.chartManager.addMarker((String) args[0]);
			result = null;
		}

		else if (request.equals("putData")) {
			// TODO vérifier que les données ne sont pas déjà présentes.
			this.macroDataset.putData((String) args[0], (SectorDataset) args[1]);
			result = null;
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
