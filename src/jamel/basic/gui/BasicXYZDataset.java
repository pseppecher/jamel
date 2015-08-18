package jamel.basic.gui;

import org.jfree.data.DomainOrder;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;


/**
 * An abstract dynamic {@link XYZDataset}.
 * Subclasses must provide an implementation of the {@link Updatable#update()} method.
 */
public abstract class BasicXYZDataset extends DefaultXYZDataset implements Updatable {
	
	@Override
	public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
	}

}

// ***