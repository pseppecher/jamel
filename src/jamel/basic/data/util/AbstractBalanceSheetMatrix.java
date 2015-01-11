package jamel.basic.data.util;

import jamel.util.Circuit;
import jamel.util.FileParser;

import java.awt.Component;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

@SuppressWarnings("javadoc")
public abstract class AbstractBalanceSheetMatrix implements BalanceSheetMatrix {
	
	/**
	 * A convenient class to store String constants.
	 */
	private static class KEY {
		public static final String MATRIX_ROWS = "rows";
		public static final String MATRIX_SECTORS = "sectors";
	}

	/** The number format. */
	final private static NumberFormat nf = NumberFormat.getInstance(Locale.US);

	/** 
	 * A JPanel containing the balance sheet.
	 */
	private final JEditorPane contentPane = new JEditorPane() {
		private static final long serialVersionUID = 1L;
		{
			this.setContentType("text/html");
			this.setText("This is the balance sheet panel.");
			this.setEditable(false);
		}
	};

	private final String[] rows;
	
	private final String[] sectors;
	
	private final HashMap<String,String> sfcMap = new HashMap<String,String>();

	public AbstractBalanceSheetMatrix(String fileName) throws FileNotFoundException {
		Map<String, String> matrixConfig = FileParser.parseMap(fileName);
		sectors = FileParser.toArray(matrixConfig.get(KEY.MATRIX_SECTORS));
		rows = FileParser.toArray(matrixConfig.get(KEY.MATRIX_ROWS));
		for (String sector:sectors) {
			for (String row:rows) {
				final String val = matrixConfig.get(sector+"."+row);
				if (val!=null) {
					sfcMap.put(sector+"."+row, val);
				}
			}
		}	
	}

	protected abstract Double get(String key);

	@Override
	public Component getPanel() {
		final Component pane = new JScrollPane(this.contentPane);
		pane.setName("Balance sheet");
		return pane;
	}

	@Override
	public String toHtml() {

		final StringBuffer table = new StringBuffer();
		final int period = Circuit.getCurrentPeriod().getValue();
		final int colspan = sectors.length+2;
		table.append("<STYLE TYPE=\"text/css\">.boldtable, .boldtable TD, .boldtable TH" +
				"{font-family:sans-serif;font-size:12pt;}" +
				"</STYLE>");		
		table.append("<BR><BR><BR><BR><TABLE border=0 align=center class=boldtable cellspacing=10>");
		table.append("<CAPTION>Balance sheet matrix (period "+ period +")</CAPTION>");
		table.append("<TR><TD colspan="+colspan+"><HR>");		

		table.append("<TR><TH WIDTH=120>");
		for(String sector:sectors) {
			table.append("<TH WIDTH=110 align=center>" + sector);
		}
		table.append("<TH WIDTH=110 align=right>" + "Sum");
		table.append("<TR><TD colspan="+colspan+"><HR>");

		final HashMap<String,Double> sumSector = new HashMap<String,Double>();
		for (String sector:sectors) {
			sumSector.put(sector, 0.);
		}
		sumSector.put("sum", 0.);

		for(String row:rows) {
			table.append("<TR><TH>" + row);
			double sum = 0l;
			for(String sector:sectors) {
				final String key = this.sfcMap.get(sector+"."+row);
				table.append("<TD align=right>");						
				if (key!=null) {
					final Double value = get(key);
					if (value !=null) {
						table.append(nf.format(value));
						sum+=value;
						sumSector.put(sector, sumSector.get(sector)+value);
					}
					else {
						table.append(key+" not found");							
					}
				}
			}
			table.append("<TD align=right>"+nf.format(sum));
			sumSector.put("sum", sumSector.get("sum")+sum);
		}

		table.append("<TR><TD colspan="+colspan+"><HR>");
		table.append("<TR><TH>Sum");
		for (String sector:sectors) {
			table.append("<TD align=right>"+nf.format(sumSector.get(sector)));
		}
		table.append("<TD align=right>"+nf.format(sumSector.get("sum")));
		table.append("<TR><TD colspan="+colspan+"><HR>");
		table.append("</TABLE>");
		return table.toString();
	}

	@Override
	public void update() {
		final String text=this.toHtml();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				contentPane.setText(text);
			}
		});			
	}

}
