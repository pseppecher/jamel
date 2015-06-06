package jamelV3.basic.gui;

import java.util.List;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;


/**
 * An abstract dynamic {@link XYZDataset}.
 * Subclasses must provide an implementation of the {@link DynamicData#update()} method.
 */
public abstract class BasicXYZDataset implements DynamicData,XYZDataset {
	
	/** The series key. */
	private final String seriesKey;

	/** 
	 * The data.
	 * Each element contains an array of length 3, 
	 * the first element containing the x-value, 
	 * the second containing the y-value 
	 * and the third containing the z-values.
	 */
	protected List<XYZItem> data = null;// new ArrayList<XYZItem>();
	
	/**
	 * Creates a new BasicXYZDataset.
	 * @param seriesKey the key for the series.
	 */
	public BasicXYZDataset(String seriesKey) {
		this.seriesKey=seriesKey;
	}

	@Override
	public void addChangeListener(DatasetChangeListener listener) {
		// Does nothing.
	}

	@Override
	public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
	}

	@Override
	public DatasetGroup getGroup() {
		return null;
	}

	@Override
	public int getItemCount(int series) {
		final int result;
		if (this.data==null) {
			result=0;
		}
		else {
			result=this.data.size();
		}
		return result;
	}

	@Override
	public int getSeriesCount() {
		return 1;
	}

	@Override
	public Comparable<?> getSeriesKey(int series) {
		final String result;
		if (series==0) {
			result = this.seriesKey;
		}
		else {
			result = null;
		}
		return result;
	}

	@Override
	public Number getX(int series, int item) {
        return new Double(getXValue(series, item));
	}

	@Override
	public double getXValue(int series, int item) {
		if (series!=0) {
			throw new IllegalArgumentException("bad series value: "+series);
		}
		return this.data.get(item).getXValue();
	}

	@Override
	public Number getY(int series, int item) {
        return new Double(getYValue(series, item));
	}

	@Override
	public double getYValue(int series, int item) {
		if (series!=0) {
			throw new IllegalArgumentException("bad series value: "+series);
		}
		return this.data.get(item).getYValue();
	}

	@Override
	public Number getZ(int series, int item) {
        return new Double(getZValue(series, item));
	}

	@Override
	public double getZValue(int series, int item) {
		if (series!=0) {
			throw new IllegalArgumentException("bad series value: "+series);
		}
		return this.data.get(item).getZValue();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int indexOf(Comparable seriesKey) {
		final int result;
		if (this.seriesKey.equals(seriesKey)) {
			result=0;
		}
		else {
			result=-1;
		}
		return result;
	}

	@Override
	public void removeChangeListener(DatasetChangeListener listener) {
		// Does nothing.
		
	}

	@Override
	public void setGroup(DatasetGroup group) {
		// Does nothing.		
	}

}

// ***