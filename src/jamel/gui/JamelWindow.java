package jamel.gui;

import jamel.Circuit;
import jamel.JamelObject;
import jamel.gui.charts.JamelChart;
import jamel.gui.charts.industry.*;
import jamel.gui.charts.income.*;
import jamel.gui.charts.labor.*;
import jamel.gui.charts.money.*;
import jamel.gui.charts.others.*;
import jamel.gui.charts.sectorFinal.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.time.Month;
import org.jfree.ui.TextAnchor;

/**
 * The window.
 */
public class JamelWindow extends JFrame {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/**
	 * Returns the industry panel.
	 * @return the industry panel.
	 */
	private static Component getIndustryPanel() {
		final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
		panel.add(new BankruptcyRate());
		panel.add(new CapacityUtilization());
		panel.add(new Markup());
		panel.add(new InventoryLevel());
		panel.add(new RelativePrices());
		panel.add(new VacancyRates());
		panel.add(new Firms());
		panel.add(new Profits());
		panel.add(new Workers());
		//panel.add(new ProfitsRatio());
		//panel.add(new ChartPanel(null));
		//panel.add(new MarkupTarget());
		return panel;
	}

	/**
	 * Returns the labor panel.
	 * @return the labor panel.
	 */
	private static Component getLaborPanel() {
		final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
		panel.add(new LaborMarket());
		panel.add(new UnemploymentRate());
		panel.add(new BeveridgeCurve());
		panel.add(new JobVacancies());
		//panel.add(new UnemploymentTypes());
		panel.add(new UnemploymentDuration());
		panel.add(new JobSeekers());
		panel.add(new Unemployment());
		panel.add(new VacancyRate());
		panel.add(new ChartPanel(null));
		return panel;
	}

	/**
	 * Returns the main panel.
	 * @return the main panel.
	 */
	private static Component getMainPanel() {
		final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
		panel.add(new Prices());
		panel.add(new FinalGoodsMarket());
		panel.add(new InventoryVolumeFinal());

		panel.add(new Wages());
		panel.add(new BeveridgeCurve());
		panel.add(new BankRatios());
		
		panel.add(new InflationUnemployment());
		panel.add(new PhillipsCurve());
		panel.add(new Distribution());
		//panel.add(new LaborMarket());
		//panel.add(new Income());
		return panel;
	}

	/**
	 * Returns the money panel.
	 * @return the money panel.
	 */
	private static Component getMoneyPanel() {
		final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
		panel.add(new BankRatios());
		panel.add(new BankDividend());
		panel.add(new Bankruptcies());
		panel.add(new Money());
		panel.add(new MoneyVelocity());
		panel.add(new ChartPanel(null));
		panel.add(new ChartPanel(null));
		panel.add(new ChartPanel(null));
		panel.add(new ChartPanel(null));
		return panel;
	}

	/** The button bar. */
	private final ButtonBar buttonBar;

	/** The console panel. */
	private JEditorPane consolePane;

	/** The text of the console panel. */
	private final StringBuffer consoleText = new StringBuffer();

	/** A String that gives some infos about Jamel. */
	private final String infoString = 
			"<h3>Jamel (20130918 - Morra)</h3>" +
			"<h1>Java Agent-based MacroEconomic Laboratory</h1>"+
			"&copy; Pascal Seppecher 2007-2013. All rights reserved.<br>"+
			"<a href=\"http://p.seppecher.free.fr\">http://p.seppecher.free.fr</a>";

	/** The matrix panel. */
	private JEditorPane matrixPane;

	/** The tabbed pane. */
	private final JTabbedPane tabbedPane ;

	/** The view manager. */
	private final ViewManager viewManager;

	/**
	 * 
	 */
	public JamelWindow() {
		viewManager = new ViewManager() ;
		tabbedPane = new JTabbedPane() ;
		setVisible(false);
		setMinimumSize(new Dimension(400,200));
		setPreferredSize(new Dimension(800,400));
		pack();
		setExtendedState(Frame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE) ;
		// ********
		getContentPane().add( tabbedPane ) ;
		this.tabbedPane.add("Main",getMainPanel());
		this.tabbedPane.add("Industry",getIndustryPanel());
		this.tabbedPane.add("Labor",getLaborPanel());
		this.tabbedPane.add("Money",getMoneyPanel());
		this.tabbedPane.add("Matrix",getMatrixPanel());
		this.tabbedPane.add("Console",getConsolePanel());
		this.tabbedPane.add("Info",getInfoPanel());
		this.buttonBar = new ButtonBar(this) ;
		getContentPane().add( this.buttonBar, "South" );
		this.buttonBar.pause(false);
	}

	/**
	 * Creates and returns the consol panel. 
	 * @return the consol panel.
	 */
	private JScrollPane getConsolePanel() {
		consolePane = new JEditorPane("text/html","<h2>The console panel.</h2>");
		consolePane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(consolePane) ;
		return scrollPane;
	}

	/**
	 * Returns the info panel.
	 * @return the info panel.
	 */
	private Component getInfoPanel() {
		final JEditorPane editorPane = new JEditorPane("text/html","<center>"+this.infoString+"</center>");
		editorPane.addHyperlinkListener(new HyperlinkListener()
		{
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
					try {
						java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (Exception ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(null,
								"<html>" +
								"Error.<br>"+
								"Cause: "+e.toString()+".<br>"+
								"Please see server.log for more details.</html>",
								"Warning",
								JOptionPane.WARNING_MESSAGE);
					}	        
				}
		});		
		editorPane.setEditable(false);
		final JScrollPane scrollPane = new JScrollPane(editorPane) ;
		return scrollPane;
	}

	/**
	 * Returns the matrix panel.
	 * @return the matrix panel.
	 */
	private JScrollPane getMatrixPanel() {
		matrixPane = new JEditorPane("text/html","<H2>The balance sheet matrix panel.</H2>");
		matrixPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(matrixPane) ;
		return scrollPane;
	}

	/**
	 * Adds a marker to all time charts.
	 * @param label the label of the marker.
	 * @param aMonth the month of the marker.
	 */
	public void addMarker(String label, Month aMonth) {
		final ValueMarker marker = new ValueMarker(aMonth.getFirstMillisecond()) ;
		marker.setLabel(label);
		marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		marker.setOutlinePaint(Color.WHITE);
		final int max = tabbedPane.getTabCount();
		for (int i = 0; i<max; i++) { 
			final Component currentTab = tabbedPane.getComponentAt(i);
			if (JPanel.class.isInstance(currentTab)) {
				int chartPanelCount = ((JPanel)currentTab).getComponentCount() ;
				for (int index=0 ; index<chartPanelCount ; index++) {
					final JamelChart chart = (JamelChart) (((ChartPanel)((JPanel)currentTab).getComponent(index)).getChart()) ;
					if (chart!=null) 
						chart.addMarker(marker);
				}
			}
		}

	}

	/**
	 * Exports a rapport.
	 */
	public void exportRapport() {
		final int max = tabbedPane.getTabCount();
		final ProgressMonitor progressMonitor = new ProgressMonitor(this,
				"Exporting",
				"", 0,max);
		progressMonitor.setMillisToDecideToPopup(0);
		final String rc = System.getProperty("line.separator");
		final File outputDirectory = new File("exports/"+this.getTitle()+"-"+(new Date()).getTime());
		outputDirectory.mkdir();
		try {
			final FileWriter writer = new FileWriter(new File(outputDirectory,"Rapport.html"));
			writer.write("<HTML>"+rc);
			writer.write("<HEAD>");
			writer.write("<TITLE>"+this.getTitle()+"</TITLE>"+rc);
			writer.write("</HEAD>"+rc);
			writer.write("<BODY>"+rc);
			writer.write("<H1>"+this.getTitle()+"</H1>"+rc);
			writer.write("<HR>"+rc);
			final Date start = viewManager.getStart().getDate();
			final Date end = viewManager.getEnd().getDate();
			for (int tabIndex = 0; tabIndex < max; tabIndex ++) {
				try {
					final JPanel currentTab = (JPanel)tabbedPane.getComponentAt(tabIndex) ;
					final String tabTitle = tabbedPane.getTitleAt(tabIndex);
					writer.write("<H2>"+tabTitle+"</H2>"+rc);
					writer.write("<TABLE>"+rc);
					final int chartPanelCount = currentTab.getComponentCount() ;
					for (int chartIndex=0 ; chartIndex<chartPanelCount ; chartIndex++) {
						if ((chartIndex==3)|(chartIndex==6)) writer.write("<TR>"+rc);
						final ChartPanel aChartPanel = (ChartPanel)currentTab.getComponent(chartIndex);
						final JamelChart chart = (JamelChart) aChartPanel.getChart() ;
						if (chart != null) {
							final String chartTitle = chart.getTitle().getText();
							if (!chartTitle.equals("Empty")) {
								try {
									chart.setTitle("");
									chart.setTimeRange(start, end) ;
									String imageName = (tabTitle+"-"+chartIndex+"-"+chartTitle+".png").replace(" ", "_").replace("&", "and");
									ChartUtilities.saveChartAsPNG(new File(outputDirectory,imageName), chart, aChartPanel.getWidth(), aChartPanel.getHeight());
									writer.write("<TD><IMG src=\""+imageName+"\" title=\""+chartTitle+"\">"+rc);			
									chart.setTitle(chartTitle);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
					writer.write("</TABLE>"+rc);
					writer.write("<HR>"+rc);
				} catch (ClassCastException e) {
				}
				progressMonitor.setProgress(tabIndex);
			}
			writer.write("<H2>Scenario</H2>"+rc);
			writer.write(this.consoleText.toString());
			writer.write("</BODY>"+rc);
			writer.write("</HTML>"+rc);
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		progressMonitor.close();
	}

	/**
	 * Shows a dialog that indicates the bank failure.
	 */
	public void failure() {
		println("<font color=red>"+JamelObject.getCurrentPeriod().toString()+" Bank Failure</font>");
		buttonBar.pause(true);
		JOptionPane.showMessageDialog(this, "Bank Failure", "Failure", JOptionPane.WARNING_MESSAGE) ;
	}

	/**
	 * Prints a String in the console panel.
	 * @param s the String to print.
	 */
	public void println(final String s) {
		final String cr = "<br>";//System.getProperty("line.separator" );
		if (SwingUtilities.isEventDispatchThread()) {
			consoleText.append(s);
			consoleText.append(cr);
			consolePane.setText(consoleText.toString());
		}
		else {
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					consoleText.append(s);
					consoleText.append(cr);
					consolePane.setText(consoleText.toString());
				}}
					);
		}
	}

	/**
	 * Sets the chart in the specified panel.
	 * @param tabIndex the index of the tab to customize.
	 * @param panelIndex the id of the ChartPanel to customize.
	 * @param chartPanelName the name of the ChartPanel to set.
	 * @throws ClassNotFoundException ...
	 * @throws NoSuchMethodException ... 
	 * @throws InvocationTargetException ...  
	 * @throws IllegalAccessException ...
	 * @throws InstantiationException ...
	 * @throws SecurityException ...
	 * @throws IllegalArgumentException ... 
	 */
	public void setChart(int tabIndex, int panelIndex, String chartPanelName) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {		
			final ChartPanel chartPanel = (ChartPanel) Class.forName(chartPanelName,false,ClassLoader.getSystemClassLoader()).getConstructor().newInstance();
			((ChartPanel) ((JPanel) tabbedPane.getComponent(tabIndex)).getComponent(panelIndex)).setChart(chartPanel.getChart());
	}

	/**
	 * Sets the selected index for the tabbedpane.
	 * @param index the index to be selected.
	 */
	public void setSelectedTab(int index) {
		this.tabbedPane.setSelectedIndex(index);		
	}

	/**
	 * 
	 */
	public void update() {
		viewManager.update() ;
		this.buttonBar.setMinMax(viewManager.getStart().toString(),viewManager.getEnd().toString()) ;
		final int max = tabbedPane.getTabCount();
		for (int i = 0; i<max; i++) { 
			final Component currentTab = tabbedPane.getComponentAt(i);
			if (JPanel.class.isInstance(currentTab)) {
				int chartPanelCount = ((JPanel)currentTab).getComponentCount() ;
				for (int index=0 ; index<chartPanelCount ; index++) {
					JamelChart chart = (JamelChart) (((ChartPanel)((JPanel)currentTab).getComponent(index)).getChart()) ;
					if (chart != null) {
						chart.setTimeRange(viewManager.getStart().getDate(), viewManager.getEnd().getDate()) ;
					}
				}
			}
		}
		this.matrixPane.setText(Circuit.getCircuit().getHtmlMatrix());
	}

	/**
	 * Sets the zoom.
	 * @param z the zoom to set.
	 */
	public void zoom(int z) {
		viewManager.setRange(z) ;
		update() ;		
	}

	/**
	 * 
	 */
	public void zoomAll() {
		viewManager.update() ;
		viewManager.zoomAll() ;
		update() ;
	}	

}
