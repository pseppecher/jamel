package jamel.v170804.gui;

import org.jfree.data.xy.XYSeries;

import jamel.util.Expression;

/**
 * An extension of {@link XYSeries}.
 */
public class DynamicXYSeries extends XYSeries {

	/**
	 * The conditions (a list of expressions).
	 */
	final private Expression[] conditions;

	/**
	 * The expression for the x values.
	 */
	final private Expression x;

	/**
	 * The expression for the y values.
	 */
	final private Expression y;

	/**
	 * Constructs a new empty series, with the auto-sort flag set as
	 * <code>false</code>, and duplicate values allowed.
	 * 
	 * @param xExp
	 *            the series key (<code>null</code> not permitted).
	 * @param yExp
	 *            the series key (<code>null</code> not permitted).
	 */
	public DynamicXYSeries(Expression xExp, Expression yExp) {
		this(xExp, yExp, null);
	}

	/**
	 * Constructs a new empty series, with the auto-sort flag set as
	 * <code>false</code>, and duplicate values allowed.
	 * 
	 * @param xExp
	 *            the series key (<code>null</code> not permitted).
	 * @param yExp
	 *            the series key (<code>null</code> not permitted).
	 * @param conditions
	 *            a list of conditions (<code>null</code> permitted).
	 */
	public DynamicXYSeries(Expression xExp, Expression yExp, Expression[] conditions) {
		super(xExp.toString() + "," + yExp.toString(), false);
		this.x = xExp;
		this.y = yExp;
		if (conditions == null) {
			this.conditions = new Expression[0];
		} else {
			this.conditions = conditions;
		}
		this.setDescription("x = " + this.x.toString() + ", y = " + this.y.toString());
	}

	/**
	 * Updates the series, by adding new data items.
	 * 
	 * @param refereshCharts
	 *            a flag that indicates if the charts must be refreshed.
	 */
	public void update(boolean refereshCharts) {
		boolean update = true;
		for (int i = 0; i < this.conditions.length; i++) {
			if (conditions[i].getValue() != 1) {
				update = false;
				break;
			}
		}
		if (update) {
			try {
				final Double xValue = this.x.getValue();
				final Double yValue = this.y.getValue();
				this.add(xValue, yValue);
			} catch (Exception e) {
				throw new RuntimeException("Something went wrong while updating the series: " + this.getDescription(),
						e);
			}
		}
		if (refereshCharts) {
			this.setNotify(true);
			this.setNotify(false);
		}
	}

}
