/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2014, Pascal Seppecher and contributors.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/javadoc/index.html>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JAMEL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.]
 * [JAMEL uses JFreeChart, copyright by Object Refinery Limited and Contributors. 
 * JFreeChart is distributed under the terms of the GNU Lesser General Public Licence (LGPL). 
 * See <http://www.jfree.org>.]
 */

package jamel;

import jamel.agents.firms.FirmSector;
import jamel.agents.households.HouseholdsSector;
import jamel.agents.roles.AccountHolder;
import jamel.spheres.monetarySphere.Account;
import jamel.spheres.monetarySphere.Bank;
import jamel.util.BalanceSheetMatrix;
import jamel.util.data.CrossSectionSeries;
import jamel.util.data.GlobalDataset;
import jamel.util.data.PeriodDataset;
import jamel.util.data.TimeseriesCollection;
import jamel.util.data.YearDataset;

import java.security.InvalidKeyException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.jfree.data.xy.XYSeries;
import org.jfree.date.MonthConstants;

/**
 * An abstraction of the macro-economic circuit.
 */
public abstract class Circuit extends JamelObject {

	/** The circuit. */
	protected static Circuit circuit;

	@SuppressWarnings("javadoc")
	protected static final String CMD_EXPORT_DATA_BEGIN = "export.data.begin";

	@SuppressWarnings("javadoc")
	protected static final String CMD_EXPORT_DATA_END = "export.data.end";

	@SuppressWarnings("javadoc")
	protected static final String CMD_EXPORT_DATA_OF_EACH_FIRM = "export.data.firms";

	@SuppressWarnings("javadoc")
	protected static final String CMD_EXPORT_DATA_OF_EACH_HOUSEHOLD = "export.data.households";

	@SuppressWarnings("javadoc")
	protected static final String CMD_PAUSE = "pause()";

	@SuppressWarnings("javadoc")
	protected static final String CMD_SET = "set";

	@SuppressWarnings("javadoc")
	protected static final String CMD_SET_FIRMS = "Firms";

	@SuppressWarnings("javadoc")
	protected static final String CMD_SET_HOUSEHOLDS = "Households";

	@SuppressWarnings("javadoc")
	protected static final String CMD_SET_RANDOM_SEED = "RandomSeed";

	@SuppressWarnings("javadoc")
	protected static final String CMD_STOP = "stop()";

	@SuppressWarnings("javadoc")
	protected static final String MONTH = "month";

	@SuppressWarnings("javadoc")
	protected static final String SEPARATOR = ",";

	@SuppressWarnings("javadoc")
	protected static final String YEAR = "year";

	@SuppressWarnings("javadoc")
	public static final String GET_HTML_MATRIX = "GetHtmlMatrix";

	@SuppressWarnings("javadoc")
	public static final String SELECT_A_CAPITAL_OWNER = "SelectACapitalOwnerAtRandom";

	@SuppressWarnings("javadoc")
	public static final String SELECT_A_HOUSEHOLD = "SelectAHouseholdAtRandom";

	@SuppressWarnings("javadoc")
	public static final String SELECT_A_LIST_OF_FIRMS = "SelectAListOfFirmsAtRandom";

	@SuppressWarnings("javadoc")
	public static final String SELECT_A_PROVIDER_OF_FINAL_GOODS = "SelectAProviderOfFinalGoodsAtRandom";

	@SuppressWarnings("javadoc")
	public static final String SELECT_A_PROVIDER_OF_RAW_MATERIAL = "SelectAProviderOfRawMaterialsAtRandom";

	@SuppressWarnings("javadoc")
	public static final String SELECT_A_WAGE = "SelectAWageAtRandom";

	@SuppressWarnings("javadoc")
	public static final String SELECT_AN_EMPLOYER = "SelectAnEmployerAtRandom";

	/**
	 * Displays a warning message.
	 * @param string the message.
	 * @param string2 the cause.
	 */
	static private void warningDialog(String string, String string2) {
		JOptionPane.showMessageDialog(null,
				"<html>" +
						string+
						"Cause: "+string2+"<br>"+
						"Please see server.log for more details.</html>",
						"Warning",
						JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Returns the specified cross section series.
	 * @param xLabel  the x label.
	 * @param yLabel  the y label.
	 * @return the specified cross section series.
	 */
	public static XYSeries getCrossSectionSeries(String xLabel, String yLabel) {
		return circuit.crossSectionSeries.get(xLabel, yLabel);
	}

	/**
	 * Returns the specified cross section series.
	 * @param xLabel  the x label.
	 * @param yLabel  the y label.
	 * @param zLabel  the z label.
	 * @return the specified cross section series.
	 */
	public static double[][] getCrossSectionSeries(String xLabel, String yLabel, String zLabel) {
		return circuit.crossSectionSeries.get(xLabel, yLabel, zLabel);
	}

	/**
	 * Returns a new bank account for the given account holder.
	 * @param holder - the new account holder.
	 * @return the new account.
	 */
	public static Account getNewAccount(AccountHolder holder) {
		return circuit.bank.getNewAccount(holder);
	}

	/**
	 * Returns the parameter to which the specified key is mapped, or null if the circuit contains no mapping for the key.
	 * @param key the key whose associated parameter is to be returned.
	 * @return the parameter to which the specified key is mapped, or null if the circuit contains no mapping for the key.
	 */
	public static String getParameter(String key) {
		final String value = circuit.parameters.get(key);
		if (value==null) {
			final String message = "Missing parameter: \""+key+"\"";
			JOptionPane.showMessageDialog(null,
					message,
					"Scenario error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(message);
		}
		return value;
	}

	/**
	 * Returns the parameters.
	 * @return the parameters. 
	 */
	public static Map<String, String> getParameters() {
		return(circuit.parameters);// TODO faudrait renvoyer une copie plut™t que l'original.
	}

	/**
	 * Returns the specified resource.
	 * @param key  the key of the resource to return.
	 * @return the resource.
	 */
	public static Object getResource(String key) {
		return circuit.getResource2(key);
	}

	/**
	 * Returns <code>true</code> if the simulation is paused, <code>false</code> otherwise.
	 * @return a boolean.
	 */
	public static boolean isPaused() {
		return circuit.simulator.isPaused();
	}

	/**
	 * Adds a new event to the scenario.
	 * @param instruction  a string that contains the description of the event.
	 */
	public static void newEvent(String instruction) {
		circuit.addEvent(instruction);
	}

	/**
	 * Sets the sate of the simulation (paused or running).
	 * @param b a flag that indicates whether or not the simulation must be paused.
	 */
	public static void pause(boolean b) {
		if (circuit!=null) {
			circuit.simulator.pause(b);
		}
	}	

	/**
	 * Prints a String in the console panel.
	 * @param message the String to be printed.
	 */
	public static void println(String message) {
		circuit.simulator.println(message);
	}

	/** The bank. */
	protected Bank bank;

	/** The cross-section series. */
	protected CrossSectionSeries crossSectionSeries;

	/** The data ready for export. */
	final protected List<String> export = new LinkedList<String>();

	/** The keys of the data to export. */
	protected String[] exportDataKeys=null;

	/** The type of data to export (YEAR or MONTH). */
	protected String exportDataPeriod=null;

	/** The sector of the firms. */
	final protected FirmSector firms;

	/** The sector of the households */
	protected HouseholdsSector households;

	/** The balance sheet matrix. */
	protected BalanceSheetMatrix matrix = new BalanceSheetMatrix();

	/** The collection of the data of the circuit, in a chronological order. */
	protected final LinkedList<PeriodDataset> monthlyData = new LinkedList<PeriodDataset>();

	/** The parameters of the simulation. */
	protected final Map<String,String> parameters = new TreeMap<String,String>();

	/** The list of events for the circuit. */
	final protected LinkedList<String> scenario = new LinkedList<String>();

	/** The simulator. */
	protected Jamel simulator;

	/** The time series collection. */
	protected TimeseriesCollection timeSeries;

	/** A dataset that contains aggregate data of the last year. */
	protected YearDataset yearData;

	/**
	 * Creates a new circuit.
	 * @param aSimulator  the simulator.
	 */
	public Circuit(Jamel aSimulator) {
		circuit = this;
		this.simulator = aSimulator;
		this.crossSectionSeries = new CrossSectionSeries();
		this.timeSeries = new TimeseriesCollection();
		this.bank = new Bank();
		this.households = new HouseholdsSector();
		this.firms = getNewFirmsSector();
		initWindow();
	}


	/**
	 * Closes the circuit.<br>
	 * Each sector composing the circuit is closed and data are updated.
	 */
	protected void close() {
		final PeriodDataset periodDataset = new PeriodDataset();
		periodDataset.randomSeed = Integer.parseInt(this.parameters.get(CMD_SET_RANDOM_SEED));
		this.monthlyData.add(periodDataset);
		if (this.monthlyData.size()>12) {
			this.monthlyData.removeFirst();
			if (this.monthlyData.size()>12) 
				throw new RuntimeException("Too many dataset.");
		}
		households.close(periodDataset) ;
		firms.close(periodDataset) ;
		bank.close(periodDataset) ;	
		periodDataset.updateRatios() ;
		this.timeSeries.updateSeries(periodDataset);
		if (getCurrentPeriod().isMonth(MonthConstants.DECEMBER)){
			this.yearData = new YearDataset(yearData);
			this.yearData.update(this.monthlyData);
			this.timeSeries.updateSeries(yearData);
		}
		exportData();
		matrix.update(periodDataset);		
		if (bank.isBankrupt())
			simulator.systemicFailure();
	}

	/**
	 * Executes the events of the simulation.
	 */
	protected void doEvents() {
		final LinkedList<String> events = getCurrentEvents();
		if (!events.isEmpty()) {
			for (String string: events){
				if (string.equals(CMD_PAUSE)) {
					simulator.pause(true);
				}
				else if (string.equals(CMD_STOP)) {
					simulator.stop();
				}
				else {
					try {
						String[] event = Jamel.parseInstruction(string);
						if (event[0].equals(CMD_SET)) {
							setParameters(event[1]);
						}
						else if (event[0].equals("newHouseholds")) {
							this.households.newHouseholds(event[1].split(","));
						}
						else if (event[0].equals("newFirms")) {
							this.firms.newFirms(event[1]);				
						}
						/*else if (event[0].equals("exportAnnualData")) { DELETE
							simulator.export(this.yearData,event[1]);
						}*/
						/*else if (event[0].equals("exportMonthlyData")) { DELETE
							simulator.export(this.monthlyData.getLast(),event[1]);
						}*/
						else if (event[0].equals(CMD_EXPORT_DATA_BEGIN)) {
							if (this.exportDataKeys!=null) {
								throw new IllegalArgumentException("Data exportation is already defined.");
							} else {
								final String[] truc=event[1].split(SEPARATOR,2);
								this.exportDataPeriod=truc[0];
								this.exportDataKeys=truc[1].split(SEPARATOR);
								this.simulator.export(truc[1]);// Exports headers.
							}
						}
						/*else if (event[0].equals("print")) { DELETE
							this.simulator.export(event[1]);
						}*/
						else if (event[0].equals(CMD_EXPORT_DATA_OF_EACH_FIRM)) {
							this.simulator.export(event[1]);
							String[] data=this.firms.getFirmsData(event[1]);
							for(String item:data) {
								if (item!=null) {
									this.simulator.export(item);
								}
							}
						}
						else if (event[0].equals(CMD_EXPORT_DATA_OF_EACH_HOUSEHOLD)) {
							this.simulator.export(event[1]);
							String[] data=this.households.getHouseholdsData(event[1]);
							for(String item:data) {
								if (item!=null) {
									this.simulator.export(item);
								}
							}
						}
						else if (event[0].equals("printFirm")) {
							final String[] truc = event[1].split(":");
							String data=this.firms.getFirmData(truc[0],truc[1]);
							if (data!=null) {
								this.simulator.export(data);
							}
							final String s=getCurrentPeriod().getFuturePeriod(1).toString()+".printFirm("+event[1]+")";
							this.scenario.addLast(s);
						}
						else if (event[0].startsWith("window")) {
							simulator.doEvent(event[0],event[1]);
						}
						else if (event[0].equals("setVerbose")) {
							firms.setVerbose(event[1]);
						}
						else 
							throw new RuntimeException("Unknown event \""+event[0]+"\".");
					} catch (NumberFormatException e) {
						e.printStackTrace();
						warningDialog("Circuit event: Error in the instruction \""+string+"\".<br>Date: "+getCurrentPeriod().toString()+".<br>",e.toString());
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						warningDialog("Circuit event: Error in the instruction \""+string+"\".<br>Date: "+getCurrentPeriod().toString()+".<br>",e.toString());
					} catch (SecurityException e) {
						e.printStackTrace();
						warningDialog("Circuit event: Error in the instruction \""+string+"\".<br>Date: "+getCurrentPeriod().toString()+".<br>",e.toString());
					}
				}
			}
		}
	}

	/**
	 * Exports data in an external file.
	 */
	protected void exportData() {
		if (this.exportDataKeys!=null) {
			if (this.exportDataPeriod.equals(MONTH)) {
				this.recordData(this.monthlyData.getLast(), exportDataKeys);
			}
			else if (this.exportDataPeriod.equals(YEAR)) {
				if (getCurrentPeriod().isMonth(MonthConstants.DECEMBER)) {
					this.recordData(this.yearData, exportDataKeys);
				}
			}
			else {
				throw new RuntimeException("Unknown period: "+this.exportDataPeriod);
			}
		}
	}

	/**
	 * Returns the list of events for the current period.
	 * @return a list of string.
	 */
	protected LinkedList<String> getCurrentEvents() {
		final String period = getCurrentPeriod().toString();
		final LinkedList<String> pList = new LinkedList<String>();
		for (String line: this.scenario) {
			final String[] temp = line.split("\\.", 2);
			if (temp.length==2) {
				if (temp[0].equals(period)) {
					pList.add(temp[1]);
				}
			}
			else {
				throw new RuntimeException("Error in scenario: this doesn't make sens: \""+line+"\"" );				
			}
		}
		return pList;
	}

	/**
	 * Returns a new firms sector.
	 * @return a <code>FirmSector</code>.
	 */
	abstract protected FirmSector getNewFirmsSector();

	/**
	 * Returns the resource specified.
	 * @param key  the key of the resource to return.
	 * @return an object.
	 */
	protected abstract Object getResource2(String key);

	/**
	 * Initializes the simulation window.
	 */
	abstract protected void initWindow();

	/**
	 * Opens the circuit.
	 */
	protected void open() {
		doEvents();
		firms.open() ;
		bank.open() ;
		households.open() ;
	}

	/**
	 * Records the data for future export.
	 * @param data  the record of data.
	 * @param keys  the keys of the values to export.
	 */
	protected void recordData(GlobalDataset data, String[] keys) {
		String line = "";
		for (String key:keys) {
			String value=null;
			if (key.startsWith("%")) {
				try {
					value=data.getFieldValue(key.substring(1)).toString();
				} catch (NoSuchFieldException e) {
					value="No Such Field: "+key;
				}
			}
			else {
				value=key;
			}
			line+=(value+",");
		}
		export.add(line);
	}

	/**
	 * Sets the parameters of the circuit.
	 * @param instructions  an array of strings that contain parameters.
	 */
	protected void setParameters (String instructions) {
		try {
			final String[] line = instructions.split(":",2);
			if (line.length==1) {
				final String[] parameter = line[0].split("=",2);
				this.parameters.put(parameter[0], parameter[1]);		
			} 
			else if (line.length==2){
				final String radical = line[0];
				final String[] values = line[1].split(",");
				for(final String param:values) {
					final String[] parameter = param.split("=",2);
					this.parameters.put(radical+"."+parameter[0], parameter[1]);
				}
			}
			else {
				throw new IllegalArgumentException("There is something wrong in \""+instructions+"\"");				
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("There is something wrong in \""+instructions+"\"");
		}		
	}

	/**
	 * Adds an event to the scenario.
	 * @param instruction  a string that contains the description of an event.
	 * @throws IllegalArgumentException  if the syntax of <code>instruction</code> is not correct. 
	 */
	public void addEvent(String instruction) throws IllegalArgumentException {
		final String[] temp1 = instruction.split("\\.", 2);
		final String[] temp2 = temp1[0].split("-", 2);
		if (temp1.length!=2||temp2.length!=2||temp2[0].length()!=4||temp2[1].length()!=2) {
			throw new IllegalArgumentException("This doesn't make sens: \""+instruction+"\"" );
		}
		this.scenario.add(instruction);
	}

	/**
	 * Executes a period a the circuit (one month). 
	 */
	abstract public void doPeriod();

	/**
	 * Returns the data to export.
	 * @return a list of strings representing the data.
	 */
	public List<String> getExportData() {
		return this.export;
	}

	/**
	 * Initializes the circuit.
	 * @param key a string that contains an initialization command.
	 * @param val the value to set.
	 * @throws InvalidKeyException  if the key is unknown.
	 */
	public void init(String key, String val) throws InvalidKeyException {
		if (key.equals(CMD_SET_RANDOM_SEED)) {
			final Integer seed = Integer.parseInt(val);
			setRandom(new Random(seed));
			this.parameters.put(CMD_SET_RANDOM_SEED, val);
		}
		else if (key.equals(CMD_SET_FIRMS)) {
			this.firms.setFirmType(val);
		}
		else if (key.equals(CMD_SET_HOUSEHOLDS)) {
			this.households.setHouseholdsType(val);
		}
		else if (key.startsWith("Firms")||key.startsWith("Households")||key.startsWith("Bank")) {
			setParameters(key+"="+val);
		} else {
			throw new InvalidKeyException("unexpected command \""+key+"\"");
		}
	}

}
