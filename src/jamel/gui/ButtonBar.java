package jamel.gui;

import jamel.Circuit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 */
@SuppressWarnings("serial")
class ButtonBar extends JPanel {

	/** exportButton */
	private JButton exportButton = new JButton("Export");	

	/** maxDateField */
	private JTextField maxDateField = new JTextField (9) ;

	/** minDateField */
	private JTextField minDateField = new JTextField (9);

	/** pauseButton */
	private JButton pauseButton = new JButton("Pause");

	/** playButton */
	private JButton playButton = new JButton("Run");

	/** quitButton */
	private JButton quitButton = new JButton("Quit");

	/** The application window. */
	final private JamelWindow window ;

	/**
	 * Creates a new button bar.
	 * @param mainWindow the window.
	 */
	public ButtonBar(JamelWindow mainWindow) {
		super() ;
		this.window = mainWindow ;
		initToolBar() ;
		pause(true);
	}

	/**
	 * Initialise la barre de boutons
	 */
	private void initToolBar() {
		playButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				pause(false) ; 
			} 
		}) ;
		playButton.setToolTipText("Start Simulation") ;
		playButton.setEnabled(false);
		pauseButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				pause(true);
			} 
		}) ;
		pauseButton.setToolTipText("Pause Simulation") ;
		pauseButton.setEnabled(false);
		exportButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				pause(true);
				window.exportLatexReport();
			} 
		}) ;
		exportButton.setToolTipText("Export Simulation Report") ;
		quitButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				System.exit(0);
			} 
		}) ;
		quitButton.setToolTipText("Quit Simulation") ;
		minDateField.setText("Min Date") ;
		minDateField.setEditable(false) ;
		minDateField.setHorizontalAlignment(SwingConstants.RIGHT) ;
		minDateField.setColumns(5) ;
		maxDateField.setText("Max Date") ;
		maxDateField.setEditable(false) ;
		maxDateField.setHorizontalAlignment(SwingConstants.RIGHT) ;
		maxDateField.setColumns(5) ;
		add(pauseButton) ;
		add(playButton) ;
		add(newViewMenuBar()) ;
		add(minDateField) ;
		add(maxDateField) ;
		add(exportButton) ;
		add(quitButton) ;
	}

	/**
	 * Returns a menu to select the size of the view.
	 * @return a JMenuBar.
	 */
	private JMenuBar newViewMenuBar() {
		JMenu zoomMenu = new JMenu("View") ;
		final JMenuItem zoomItem1 = new JCheckBoxMenuItem ("1 month") ;
		final JMenuItem zoomItem12 = new JCheckBoxMenuItem ("12 months") ;
		final JMenuItem zoomItem120 = new JCheckBoxMenuItem ("10 years") ;
		final JMenuItem zoomItem300 = new JCheckBoxMenuItem ("25 years") ;
		final JMenuItem zoomItem600 = new JCheckBoxMenuItem ("50 years") ;
		final JMenuItem zoomItem1200 = new JCheckBoxMenuItem ("100 years") ;
		final JMenuItem zoomItemAll = new JMenuItem ("All Period") ;
		zoomItem1.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent e){
				zoomItem1.setSelected(true);
				zoomItem12.setSelected(false);
				zoomItem120.setSelected(false);
				zoomItem300.setSelected(false);
				zoomItem600.setSelected(false);
				zoomItem1200.setSelected(false);
				zoomItemAll.setSelected(false);
				window.zoom(1) ;}
		});
		zoomItem12.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent e){ 
				zoomItem1.setSelected(false);
				zoomItem12.setSelected(true);
				zoomItem120.setSelected(false);
				zoomItem300.setSelected(false);
				zoomItem600.setSelected(false);
				zoomItem1200.setSelected(false);
				zoomItemAll.setSelected(false);
				window.zoom(12) ;}
		});
		zoomItem120.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent e){
				zoomItem1.setSelected(false);
				zoomItem12.setSelected(false);
				zoomItem120.setSelected(true);
				zoomItem300.setSelected(false);
				zoomItem600.setSelected(false);
				zoomItem1200.setSelected(false);
				zoomItemAll.setSelected(false);
				window.zoom(120) ;}
		});
		zoomItem300.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent e){ 
				zoomItem1.setSelected(false);
				zoomItem12.setSelected(false);
				zoomItem120.setSelected(false);
				zoomItem300.setSelected(true);
				zoomItem600.setSelected(false);
				zoomItem1200.setSelected(false);
				zoomItemAll.setSelected(false);
				window.zoom(300) ;}
		});
		zoomItem600.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent e){ 
				zoomItem1.setSelected(false);
				zoomItem12.setSelected(false);
				zoomItem120.setSelected(false);
				zoomItem300.setSelected(false);
				zoomItem600.setSelected(true);
				zoomItem1200.setSelected(false);
				zoomItemAll.setSelected(false);
				window.zoom(600) ;}
		});
		zoomItem1200.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent e){ 
				zoomItem1.setSelected(false);
				zoomItem12.setSelected(false);
				zoomItem120.setSelected(false);
				zoomItem300.setSelected(false);
				zoomItem600.setSelected(false);
				zoomItem1200.setSelected(true);
				zoomItemAll.setSelected(false);
				window.zoom(1200) ;}
		});
		zoomItemAll.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent e){ 
				zoomItem1.setSelected(false);
				zoomItem12.setSelected(false);
				zoomItem120.setSelected(false);
				zoomItem300.setSelected(false);
				zoomItem600.setSelected(false);
				zoomItem1200.setSelected(false);
				zoomItemAll.setSelected(false);
				window.zoomAll() ;}
		});
		JMenuBar zoomMenuBar = new JMenuBar() ;
		zoomMenu.add(zoomItem1) ;
		zoomMenu.add(zoomItem12) ;
		zoomMenu.add(zoomItem120) ;
		zoomMenu.add(zoomItem300) ;
		zoomMenu.add(zoomItem600) ;
		zoomMenu.add(zoomItem1200) ;
		zoomMenu.add(zoomItemAll) ;
		zoomMenuBar.add(zoomMenu) ;
		return zoomMenuBar ;
	}

	/**
	 *  
	 */
	@SuppressWarnings("unused")
	private void terminate() {
		pauseButton.setEnabled(false) ;
		pauseButton.setSelected(false) ;
		playButton.setEnabled(false) ;
		playButton.setSelected(false) ;
	}

	/**
	 * Sets the state of the simulation (paused or running).
	 * @param b the state to set.
	 */
	void pause(boolean b) {
		Circuit.pause(b) ;
	}

	/**
	 * Sets the text in the minDate and maxDate fields.
	 * @param start the start of the view. 
	 * @param end the end of the view.
	 */
	void setMinMax(String start, String end) {
		minDateField.setText(start) ;
		maxDateField.setText(end) ;
	}

	/**
	 * Updates the pause/run buttons.
	 */
	void updatePauseButton() {
		final boolean b = Circuit.isPaused();
		pauseButton.setEnabled(!b) ;
		pauseButton.setSelected(b) ;
		playButton.setEnabled(b) ;
		playButton.setSelected(!b) ;
		this.window.updatePanel(null);
	}
}














