package jamel;

import org.jfree.data.xy.XYSeries;

/**
 * A {@link XYSeries} that implements the {@link Updatable} interface.
 */
public class DynamicXYSeries extends XYSeries implements Updatable {

	private Expression x;
	private Expression y;

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
		// TODO duplicate values ne devrait pas toujours être allowed ?
		super(xExp.toString() + "," + yExp.toString(), false);
		this.x = xExp;
		this.y = yExp;
	}

	@Override
	public void update() {
		// TODO tester ici les limites start et end ainsi que le modulo
		final Double xValue = this.x.getValue();
		final Double yValue = this.y.getValue();
		if (xValue != null && yValue != null) {
			this.add(xValue, yValue);
		}
	}

}
