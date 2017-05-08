package jamel.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.util.ParamChecks;

/**
 * A convenient extension of ChartPanel.
 */
public class JamelChartPanel extends ChartPanel {

	/** Background color */
	private static final Color background = JamelColor.getColor("background");

	/**
	 * Constructs a panel that displays the specified chart.
	 * 
	 * @param chart
	 *            the chart to be displayed.
	 */
	public JamelChartPanel(JamelChart chart) {
		super(chart);
		this.setBackground(background);
	}

	/**
	 * Adds a marker to the chart.
	 * 
	 * @param marker
	 *            the marker to be added (<code>null</code> not permitted).
	 */
	public void addMarker(ValueMarker marker) {
		((JamelChart) this.getChart()).addTimeMarker(marker);
	}

	/**
	 * Writes the current chart to the specified file in PDF format. This
	 * will only work when the OrsonPDF library is found on the classpath.
	 * Reflection is used to ensure there is no compile-time dependency on
	 * OrsonPDF (which is non-free software).
	 * 
	 * @param file
	 *            the output file (<code>null</code> not permitted).
	 * @param w
	 *            the chart width.
	 * @param h
	 *            the chart height.
	 */
	public void writeAsPDF(File file, int w, int h) {
		ParamChecks.nullNotPermitted(file, "file");
		try {
			Class<?> pdfDocClass = Class.forName("com.orsonpdf.PDFDocument");
			Object pdfDoc = pdfDocClass.newInstance();
			Method m = pdfDocClass.getMethod("createPage", Rectangle2D.class);
			Rectangle2D rect = new Rectangle(w, h);
			Object page = m.invoke(pdfDoc, rect);
			Method m2 = page.getClass().getMethod("getGraphics2D");
			Graphics2D g2 = (Graphics2D) m2.invoke(page);
			g2.setRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true);
			Rectangle2D drawArea = new Rectangle2D.Double(0, 0, w, h);
			this.getChart().draw(g2, drawArea);
			Method m3 = pdfDocClass.getMethod("writeToFile", File.class);
			m3.invoke(pdfDoc, file);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

}

// ***
