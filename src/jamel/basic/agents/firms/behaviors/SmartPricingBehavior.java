package jamel.basic.agents.firms.behaviors;
import jamel.util.Circuit;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.Random;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A pricing behavior that uses a trial and error process to find the good price, observing both sales and inventories.<p>
 * (from SmartPricingManager in Jamel-1).
 */
public class SmartPricingBehavior {

	/**
	 * For testing purposes only.
	 * @param args unused.
	 */
	@SuppressWarnings("serial")
	public static void main(String[] args) {
		final XYSeries targetSeries = new XYSeries("Target",false);
		final XYSeries priceSeries = new XYSeries("Price",false);
		new JFrame() {{
			this.setMinimumSize(new Dimension(400,200));
			this.setPreferredSize(new Dimension(800,400));
			this.pack();
			this.setExtendedState(Frame.MAXIMIZED_BOTH);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			final XYSeriesCollection dataset = new XYSeriesCollection() {{
				this.addSeries(targetSeries);
				this.addSeries(priceSeries);
			}};
			final JFreeChart chart = ChartFactory.createXYLineChart("Behavior test", "Periods", "", dataset);
			final ChartPanel chartPanel = new ChartPanel(chart);
			this.getContentPane().add(chartPanel);
			this.setVisible(true);
		}} ;
		final Random random = new Random();
		final float priceFlexibility = 0.02f;
		final int unitCost = 100;
		final SmartPricingBehavior pricingBehavior = new SmartPricingBehavior();
		pricingBehavior.setPrice(unitCost, priceFlexibility);
		double target = 2000;
		for (int count=0;count<10000;count++) {
			target=target+target*0.1*(random.nextFloat()-0.495f);
			float inventoryRatio;
			double salesRatio;
			if (pricingBehavior.getPrice()<target) {
				inventoryRatio = 0.9f;
				salesRatio = 1f;
			}
			else {
				inventoryRatio = 1.1f;
				salesRatio = 0.9f;				
			}
			pricingBehavior.updatePrice(inventoryRatio, salesRatio, priceFlexibility, unitCost);
			targetSeries.add(count, target);
			priceSeries.add(count,pricingBehavior.getPrice());
		}
		
	}

	/** The higher price. */
	private Double highPrice = null;

	/** The lower price. */
	private Double lowPrice = null;

	/** The price. */
	private Double price;

	/**
	 * Returns a new price chosen at random in the given interval.
	 * @param lowPrice  the lower price
	 * @param highPrice  the higher price
	 * @return the new price.
	 */
	private double getNewPrice(Double lowPrice, Double highPrice) {
		if (lowPrice>highPrice) {
			throw new IllegalArgumentException("lowPrice > highPrice.");
		}
		return lowPrice+Circuit.getRandom().nextFloat()*(highPrice-lowPrice);
	}
	
	/**
	 * Sets the price.
	 * @param price the price to be set.
	 * @param priceFlexibility the price flexibility.
	 */
	public void setPrice(double price, float priceFlexibility) {
		this.price = price;
		this.highPrice = (1f+priceFlexibility)*this.price;
		this.lowPrice = (1f-priceFlexibility)*this.price;
	}

	/**
	 * Returns the price.
	 * @return the price.
	 */
	public Double getPrice() {
		final Double result;
		if (this.price==null) {
			result=null;
		}
		else {
			result=new Double(this.price);
		}
		return result;
	}
	
	/**
	 * Updates the price.
	 * @param inventoryRatio the inventory ratio.
	 * @param salesRatio the ratio volume of sales to volume of commodities offered.
	 * @param priceFlexibility the price flexibility.
	 * @param unitCost the unit cost 
	 * = "The cost incurred by a company to produce, store and sell one unit of a particular product."
	 * @See <a href="http://www.investopedia.com/terms/u/unitcost.asp">www.investopedia.com/terms/u/unitcost.asp</a>

	 */
	public void updatePrice(double inventoryRatio, Double salesRatio, float priceFlexibility, double unitCost) {
		if (this.price==null) {
			if (!Double.isNaN(unitCost)) {
				this.setPrice(unitCost,priceFlexibility);
			}
		}
		if (this.price!=null && salesRatio!=null) {
			if ((salesRatio==1)) {
				this.lowPrice = this.price;
				if (inventoryRatio<1) {
					this.price = getNewPrice(this.lowPrice,this.highPrice);
				}
				this.highPrice =  this.highPrice*(1f+priceFlexibility);
			}
			else {
				this.highPrice = this.price;
				if (inventoryRatio>1) {
					this.price = getNewPrice(this.lowPrice,this.highPrice);
				}					
				this.lowPrice =  this.lowPrice*(1f-priceFlexibility);
			}
		}
	}

}

// ***
