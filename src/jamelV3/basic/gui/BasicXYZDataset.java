package jamelV3.basic.gui;

import org.jfree.data.DomainOrder;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;


/**
 * An abstract dynamic {@link XYZDataset}.
 * Subclasses must provide an implementation of the {@link DynamicData#update()} method.
 */
public abstract class BasicXYZDataset extends DefaultXYZDataset implements DynamicData {
	
	@Override
	public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
	}

}

// ***