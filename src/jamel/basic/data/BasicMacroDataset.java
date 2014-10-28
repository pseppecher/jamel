package jamel.basic.data;

import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYDataItem;

/**
 * A basic dataset for macro-economic data.
 */
@SuppressWarnings("serial")
public class BasicMacroDataset extends HashMap<String,SectorDataset> implements MacroDataset {

	@Override
	public Double get(String key) {
		final Double result;
		final boolean positive;
		if (key.startsWith("-")) {
			positive=false;
			key=key.substring(1);
		}
		else {
			positive=true;
		}
		final Double val;
		final String[] keys = key.split("\\.",2);
		final SectorDataset sectorDataset = super.get(keys[0]);
		if (sectorDataset!=null) {
			val = sectorDataset.get(keys[1]);
		}
		else {
			val=null;
		}
		if (val!=null && !positive) {
			result=-val;
		}
		else {
			result=val;
		}
		return result;
	}

	@Override
	public List<XYDataItem> getScatter(String sector, String xKey, String yKey) {
		final List<XYDataItem> result;
		final String[] word = sector.split("\\.",2); 
		final SectorDataset sectorDataset = super.get(word[0]);
		if (sectorDataset!=null) {
			result = sectorDataset.getScatter(word[1],xKey,yKey);
		}
		else {
			result=null;
		}		
		return result;
	}

	@Override
	public void putData(String sector, SectorDataset sectorDataset) {
		this.put(sector, sectorDataset);
	}

}

// ***
