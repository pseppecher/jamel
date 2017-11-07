package jamel.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.title.TextTitle;

import jamel.Jamel;
import jamel.util.ArgChecks;
import jamel.util.Parameters;

/**
 * A convenient extension of ChartPanel.
 */
public class JamelChartPanel extends ChartPanel implements Updatable {

	/**
	 * Background color
	 */
	private static final Color background = JamelColor.getColor("background");

	/**
	 * The tooltip dismiss delay, in ms.
	 */
	private static final int setDismissDelay = 10000;

	/**
	 * Constructs a panel that displays the specified chart.
	 * 
	 * @param chart
	 *            the chart to be displayed.
	 */
	public JamelChartPanel(JamelChart chart) {
		super(chart);
		this.setBackground(background);
		this.setDismissDelay(setDismissDelay);
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
	 *            the output file ({@code null} not permitted).
	 * @param params
	 *            formating parameters.
	 */
	public void export(final File file, final Parameters params) {
		ArgChecks.nullNotPermitted(file, "file");
		final int w;
		final int h;
		if (params != null) {
			w = Integer.parseInt(params.getAttribute("width"));
			h = Integer.parseInt(params.getAttribute("height"));
		} else {
			w = this.getWidth();
			h = this.getHeight();
		}

		// Chart formating

		final TextTitle chartTitle = this.getChart().getTitle();
		this.getChart().setTitle((String) null);
		final BlockFrame legendFrame = this.getChart().getLegend().getFrame();
		this.getChart().getLegend().setFrame(BlockBorder.NONE);

		try {

			// Export to pdf

			final Class<?> pdfDocClass = Class.forName("com.orsonpdf.PDFDocument");
			final Object pdfDoc = pdfDocClass.newInstance();
			final Method m = pdfDocClass.getMethod("createPage", Rectangle2D.class);
			final Rectangle2D rect = new Rectangle(w, h);
			final Object page = m.invoke(pdfDoc, rect);
			final Method m2 = page.getClass().getMethod("getGraphics2D");
			final Graphics2D g2 = (Graphics2D) m2.invoke(page);
			g2.setRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true);
			final Rectangle2D drawArea = new Rectangle2D.Double(0, 0, w, h);
			this.getChart().draw(g2, drawArea);
			final Method m3 = pdfDocClass.getMethod("writeToFile", File.class);
			m3.invoke(pdfDoc, file);

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException ex) {
			throw new RuntimeException(ex);
		}

		// Chart unformating

		this.getChart().setTitle(chartTitle);
		this.getChart().getLegend().setFrame(legendFrame);

	}

	@Override
	public void update() {
		((JamelChart) this.getChart()).update();
	}

	@Override
	public void paintComponent(Graphics g) {
		try {
			super.paintComponent(g);
		} catch (Exception e) {
			Jamel.println("Something went wrong with this chart: "+this.getChart().getTitle().getText());
			e.printStackTrace();
		}
	}
}

// ***
