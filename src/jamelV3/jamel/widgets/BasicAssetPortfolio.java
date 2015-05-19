package jamelV3.jamel.widgets;

import java.util.HashSet;
import java.util.Set;

/**
 * A basic implementation of <code>AssetPortfolio</code>.
 */
public final class BasicAssetPortfolio implements AssetPortfolio {

	/** The set of assets. */
	private final Set<Asset> assets = new HashSet<Asset>();

	@Override
	public void add(Asset asset) {
		if(this.assets.contains(asset)){
			throw new RuntimeException("This asset is already owned.");
		}
		this.assets.add(asset);
	}

	@Override
	public boolean contains(Asset asset) {
		return this.assets.contains(asset);
	}

	@Override
	public long getNetValue() {
		long value = 0;
		for(Asset asset:assets) {
			value += asset.getBookValue();
		}
		return value;
	}

	@Override
	public void remove(Asset asset) {
		if (!this.assets.contains(asset)) {
			throw new RuntimeException("Asset Not found.");
		}
		this.assets.remove(asset);
	}
}

// ***
