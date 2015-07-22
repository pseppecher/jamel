package jamel.jamel.sfc;

import java.io.File;

import org.w3c.dom.Element;

import jamel.basic.BasicCircuit;
import jamel.basic.data.MacroDatabase;
import jamel.basic.util.InitializationException;
import jamel.basic.util.Timer;

/**
 * A basic circuit with stock-flow consistent extensions.
 */
public class SFCCircuit extends BasicCircuit {
	
	/**
	 * Creates and returns a new balance sheet matrix.
	 * @param settings the settings.
	 * @param timer the timer.
	 * @param path the path to the scenario file.
	 * @param macroDatabase the macroeconomic database.
	 * @return the new balance sheet matrix.
	 * @throws InitializationException If something goes wrong.
	 */
	private static BalanceSheetMatrix getNewBalanceSheetMatrix(Element settings, Timer timer, String path, MacroDatabase macroDatabase) throws InitializationException {		
		final BalanceSheetMatrix balanceSheetMatrix;
		final String fileName = settings.getAttribute("sfcMatrixConfigFile");
		if ("".equals(fileName)) {
			throw new InitializationException("Missing attribute: sfcMatrixConfigFile");
		}
		final File file = new File(path+"/"+fileName);
		balanceSheetMatrix = new BasicBalanceSheetMatrix(file,timer,macroDatabase);
		return balanceSheetMatrix;
	}

	/**
	 * Creates and returns a new data validator.
	 * @param settings the settings.
	 * @param timer the timer.
	 * @param path the path to the scenario file.
	 * @param macroDatabase the macroeconomic database.
	 * @return a new data validator.
	 * @throws InitializationException If something goes wrong.
	 */
	private static DataValidator getNewDataValidator(Element settings, Timer timer, String path, MacroDatabase macroDatabase) throws InitializationException {
		DataValidator result;
		final String fileName = settings.getAttribute("validationConfigFile");
		if ("".equals(fileName)) {
			throw new InitializationException("Missing attribute: validationConfigFile");
		}
		final File file = new File(path+"/"+fileName);
		result = new BasicDataValidator(file,timer,macroDatabase);
		return result;
	}

	/** The {@link BalanceSheetMatrix}. */
	private final BalanceSheetMatrix balanceSheetMatrix;
	
	/** The {@link DataValidator}. */
	private final DataValidator dataValidator;

	/**
	 * Creates a new basic circuit.
	 * @param elem a XML element with the parameters for the new circuit.
	 * @param path the path to the scenario file.
	 * @param name the name of the scenario file.
	 * @throws InitializationException If something goes wrong.
	 */
	public SFCCircuit(Element elem, String path, String name) throws InitializationException {
		super(elem, path, name);
		final Element settings = getSettings(elem);
		this.balanceSheetMatrix = getNewBalanceSheetMatrix(settings, timer, path, this.dataManager.getMacroDatabase());
		this.dataValidator = getNewDataValidator(settings, timer, path, this.dataManager.getMacroDatabase());
		final int tabCount = this.gui.getTabCount();
		this.gui.addPanel(this.dataValidator.getPanel(),tabCount-1);
		this.gui.addPanel(this.balanceSheetMatrix.getPanel(),tabCount-1);
	}

	@Override
	protected void updateData() {
		super.updateData();
		this.balanceSheetMatrix.update();
		this.dataValidator.checkConsistency();
	}

}

// ***
