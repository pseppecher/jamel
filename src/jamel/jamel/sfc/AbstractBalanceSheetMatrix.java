package jamel.jamel.sfc;

import jamel.basic.util.InitializationException;
import jamel.basic.util.Timer;

import java.awt.Component;
import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An abstract macroeconomic balance sheet matrix.
 */
public abstract class AbstractBalanceSheetMatrix implements BalanceSheetMatrix {

	/**
	 * A convenient class to store String constants.
	 */
	@SuppressWarnings("javadoc")
	private static class KEY {
		public static final String KEY = "key";
		public static final String ROW = "row";
		public static final String SECTOR = "sector";
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

	/** The rows */
	private  String[] rows;

	/** The sectors */
	private  String[] sectors;

	/** A map that associate a key ("Sector.Row") with the definition of an aggregate data. */
	private final HashMap<String,String> sfcMap = new HashMap<String,String>();

	/** The short title of the matrix. */
	private String shortTitle;

	/** The timer. */
	private final Timer timer;

	/** The title of the matrix.*/
	private String title;

	/**
	 * Creates a new balance sheet matrix.
	 * @param file an XML file that contains the balance sheet configuration.
	 * @param timer the timer.
	 * @throws InitializationException If something goes wrong.
	 */
	public AbstractBalanceSheetMatrix(final File file, final Timer timer) throws InitializationException  {
		this.timer = timer;
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(file);
			final Element root = document.getDocumentElement();
			if (!"matrix".equals(root.getNodeName())) {
				throw new InitializationException("The root node of the file must be named <matrix>.");
			}
			this.shortTitle = root.getAttribute("shortTitle");
			this.title = root.getAttribute("title");
			final NodeList sectorNodeList = root.getElementsByTagName(KEY.SECTOR);
			this.sectors = new String[sectorNodeList.getLength()];
			for (int i = 0; i<sectorNodeList.getLength(); i++) {
				this.sectors[i]= ((Element) sectorNodeList.item(i)).getAttribute(KEY.KEY);
			}
			final NodeList rowNodeList = root.getElementsByTagName(KEY.ROW);
			this.rows = new String[rowNodeList.getLength()];
			for (int i = 0; i<rowNodeList.getLength(); i++) {
				Element row = (Element) rowNodeList.item(i);
				this.rows[i] = row.getAttribute("key");
				final NodeList childs = row.getChildNodes();
				for(int j = 0; j<childs.getLength(); j++) {
					if (childs.item(j).getNodeType()==Node.ELEMENT_NODE) {
						final Element item = (Element) childs.item(j);
						final String value = item.getAttribute("val");
						final String sector = item.getTagName();
						sfcMap.put(sector+"."+this.rows[i], value);
					}
				}
			}
		}
		catch (Exception e) {
			throw new InitializationException("Something went wrong while parsing the file \""+file+"\"",e);
		}
	}

	/**
	 * Returns the value for this key.
	 * @param key the key of the value to return.
	 * @return the value for this key.
	 */
	protected abstract Double getValue(String key);

	@Override
	public Component getPanel() {
		final Component pane = new JScrollPane(this.contentPane);
		pane.setName(this.shortTitle);
		return pane;
	}

	@Override
	public String toHtml() {

		final StringBuffer table = new StringBuffer();
		final int period = timer.getPeriod().intValue();
		final int colspan = sectors.length+2;
		table.append("<STYLE TYPE=\"text/css\">.boldtable, .boldtable TD, .boldtable TH" +
				"{font-family:sans-serif;font-size:12pt;}" +
				"</STYLE>");		
		table.append("<BR><BR><BR><BR><TABLE border=0 align=center class=boldtable cellspacing=10>");
		table.append("<CAPTION>"+this.title+" (period "+ period +")</CAPTION>");
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
					final Double value = getValue(key);
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
			@Override
			public void run() {
				contentPane.setText(text);
			}
		});			
	}

}

// ***
