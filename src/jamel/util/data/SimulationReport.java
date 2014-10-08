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
 * [JAMEL uses JFreeChart, copyright by Object Refinery Limited and Contributors. See <http://www.jfree.org>.]
 */

package jamel.util.data;

import jamel.Jamel;
import jamel.gui.charts.JamelChart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.jfree.chart.ChartUtilities;

/**
 * A class for exporting simulation reports at Latex format.
 */
public class SimulationReport {

	/** Chart height. */
	private static final int height = 300;

	/** The line separator. */
	private final static String rc = System.getProperty("line.separator");

	/** Chart width. */
	private static final int width = 500;

	/** The end. */
	private Date end;

	/** The list of the k */
	final private ArrayList<String> panelKeys = new ArrayList<String>();

	/** List of the chart panels. */
	private final HashMap<String,List<JamelChart>> panelList=new HashMap<String,List<JamelChart>>();

	/** The parameters. */
	private Map<String, String> parameters;

	/** The start. */
	private Date start;

	/** The title of the report. */
	private String title;

	/**
	 * Exports the parameters toward the given writer.
	 * @param writer  the writer.
	 * @throws IOException  if there is an I/O error.
	 */
	private void exportParameters(FileWriter writer) throws IOException {
		final Map<String,String> bank= new TreeMap<String,String>();
		final Map<String,String> firms= new TreeMap<String,String>();
		final Map<String,String> households= new TreeMap<String,String>();
		final Map<String,String> others= new TreeMap<String,String>();
		for(Entry<String,String> entry:parameters.entrySet()) {
			final String key = entry.getKey();
			final String val = entry.getValue();
			if (key.startsWith("Bank")) {
				bank.put(key,val);
			}
			else if (key.startsWith("Firms")) {
				bank.put(key,val);
			}
			else if (key.startsWith("Households")) {
				bank.put(key,val);
			}
			else {
				bank.put(key,val);
			}
		}
		exportParameters(writer,bank);
		exportParameters(writer,firms);
		exportParameters(writer,households);
		exportParameters(writer,others);
	}

	/**
	 * Exports the given parameters toward the given writer.
	 * @param writer  the destination writer.
	 * @param map  the parameters to export.
	 * @throws IOException  if there is an I/O error.
	 */
	private void exportParameters(FileWriter writer, Map<String,String> map) throws IOException {
		if (!map.isEmpty()) {
			//writer.write("\\begin{table}[h!]"+rc);
			writer.write("\\begin{tabular}{lr}"+rc);
			writer.write("\\hline"+rc);
			writer.write("parameter & value \\\\"+rc);
			writer.write("\\hline"+rc);
			for(Entry<String,String> entry:parameters.entrySet()) {
				writer.write(entry.getKey()+"&"+entry.getValue()+"\\\\"+rc);
			}
			writer.write("\\hline"+rc);
			writer.write("\\end{tabular}"+rc);		
			writer.write("\\clearpage"+rc+rc);		
			//writer.write("\\end{table}"+rc+rc);
		}		
	}

	/**
	 * Adds a chart to the report.
	 * @param panel  the name of the panel of the chart.
	 * @param chart  the chart to add.
	 */
	public void addChart(String panel, JamelChart chart) {
		if (chart!=null){
			if (!this.panelKeys.contains(panel)) {
				this.panelKeys.add(panel);
				this.panelList.put(panel,new LinkedList<JamelChart>());
			}
			this.panelList.get(panel).add(chart);
		}
	}

	/**
	 * Exports the report.
	 */
	public void export() {
		final String header = Jamel.readFile("latexHeader.txt");
		final File outputDirectory = new File("exports/"+title+"-"+(new Date()).getTime());
		outputDirectory.mkdir();
		try {
			final FileWriter writer = new FileWriter(new File(outputDirectory,"Report-"+title+".tex"));
			writer.write(header);
			writer.write("\\title{"+title+"}"+rc);
			writer.write("\\date{"+((new Date()).toString())+"}"+rc);
			writer.write("\\maketitle"+rc+rc);
			writer.write("\\centering"+rc+rc);
			for (String tabTitle:this.panelKeys) {
				final List<JamelChart> currentTab = panelList.get(tabTitle);
				try {
					writer.write("\\clearpage"+rc+rc);
					writer.write("\\begin{figure}"+rc);
					int chartIndex=0;
					for (JamelChart chart:currentTab) {
						if (chart != null) {
							final String chartTitle = chart.getTitle().getText();
							try {
								chart.setTitle("");
								final String title2 = chartTitle.replace("&", "and");
								chart.setTimeRange(start, end) ;
								final String imageName = (tabTitle+"-"+chartIndex+"-"+chartTitle+".png").replace(" ", "_").replace("&", "and");
								ChartUtilities.saveChartAsPNG(new File(outputDirectory,imageName), chart, width, height);
								writer.write("\\begin{minipage}[b]{0.33\\linewidth}"+rc);			
								writer.write("\\centering"+rc);			
								writer.write("\\scalebox{0.35}{\\includegraphics{"+imageName+"}}"+rc);
								writer.write("\\subcaption{"+title2+"}"+rc);			
								writer.write("\\end{minipage}"+rc);			
								chart.setTitle(chartTitle);
								chartIndex++;
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
					}
					writer.write("\\caption{"+tabTitle+"}"+rc);	
					writer.write("\\end{figure}"+rc+rc);
				} catch (ClassCastException e) {
				}
			}
			writer.write("\\clearpage"+rc+rc);
			exportParameters(writer);
			writer.write("\\end{document}"+rc);	
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * Sets the start and the end of the report. 
	 * @param start  the date of the start.
	 * @param end  the date of the end.
	 */
	public void setDates(Date start, Date end) {
		this.start=start;
		this.end=end;
	}

	/**
	 * Sets the parameters of the simulation.
	 * @param parameters  an Map that contains the keys and the values.
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Sets the title of the report.
	 * @param title  the title to set.
	 */
	public void setTitle(String title) {
		this.title=title;
	}	

}
