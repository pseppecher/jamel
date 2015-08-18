package jamel.jamel.widgets;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A basic implementation of <code>AssetPortfolio</code>.
 */
public final class BasicAssetPortfolio implements AssetPortfolio {

	/** The set of assets. */
	private final Set<Asset> assets = new HashSet<Asset>();

	@Override
	public void add(Asset asset) {
		if (this.assets.contains(asset)) {
			throw new RuntimeException("This asset is already owned.");
		}
		this.assets.add(asset);
	}

	@Override
	public boolean contains(Asset asset) {
		return this.assets.contains(asset);
	}

	@Override
	public List<Asset> getList() {
		return new LinkedList<Asset>(assets);
	}

	@Override
	public long getNetValue() {
		long value = 0;
		final List<Asset> cancelled = new LinkedList<Asset>();
		for (Asset asset : assets) {
			if (asset.isCancelled()) {
				cancelled.add(asset);
			}
			else {
				value += asset.getBookValue();
			}
		}
		for (Asset asset:cancelled) {
			assets.remove(asset);
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
