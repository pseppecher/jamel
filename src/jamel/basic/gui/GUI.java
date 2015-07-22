package jamel.basic.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 *  The graphical user interface.
 */
public class GUI {

	/**
	 * The Jamel window.
	 */
	private final class JamelWindow extends JFrame {

		/** The control panel. */
		private final Component controlPanel;

		/** The tabbedPane. */
		private final JTabbedPane tabbedPane = new JTabbedPane() ;

		{
			this.setMinimumSize(new Dimension(400,200));
			this.setPreferredSize(new Dimension(800,400));
			this.pack();
			this.setExtendedState(Frame.MAXIMIZED_BOTH);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
		}

		/**
		 * Creates a new window.
		 * @param title the title to be displayed in the frame's border.
		 * @param controlPanel the control panel.
		 */
		private JamelWindow(String title, Component controlPanel) {
			super();
			this.getContentPane().add(tabbedPane);
			this.controlPanel = controlPanel;
			this.getContentPane().add(controlPanel,"South");
			controlPanel.repaint();
			this.setTitle(title);			
			this.setVisible(true);
		}

		/**
		 * Adds a component to the tabbed pane.
		 * @param component the component to be added.
		 */
		private void addPanel(Component component) {
			this.tabbedPane.add(component);
		}

		/**
		 * Adds a component at the specified tab index. 
		 * @param component the component to be added.
		 * @param index the position to insert this new tab.
		 */
		private void addPanel(Component component, int index) {
			this.tabbedPane.add(component, index);
		}

		/**
		 * Updates the control panel.
		 */
		private void pause() {
			this.controlPanel.repaint();
		}

		/**
		 * Gets the number of components in the tabbed panel.
		 * @return  the number of components in the tabbed panel.
		 */
		public int getTabCount() {
			return this.tabbedPane.getComponentCount();
		}

	}

	/** The window. */
	private final JamelWindow window;

	/**
	 * Creates a new graphical user interface.
	 * @param name the name of the interface.
	 * @param controlPanel the control panel.
	 */
	public GUI(String name, Component controlPanel) {
		this.window = new JamelWindow(name,controlPanel);
	}

	/**
	 * Adds a component to the tabbed pane of the window.
	 * @param panel the panel to be added.
	 */
	public void addPanel(final Component panel) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.addPanel(panel);
			}
		});			
	}

	/**
	 * Adds a component at the specified tab index.
	 * @param panel  the component to be added.
	 * @param index  the position to insert this new tab.
	 */
	public void addPanel(final Component panel, final int index) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.addPanel(panel,index);
			}
		});			
	}

	/**
	 * Adds the specified components to the tabbed pane of the window.
	 * @param panels the components to be added.
	 */
	public void addPanel(final Component[] panels) {
		if (panels == null) {
			throw new RuntimeException("Panels is null");
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (Component panel: panels) {
					window.addPanel(panel);
				}
			}
		});			
	}

	/**
	 * Gets the number of components in the tabbed panel.
	 * @return the number of components in the tabbed panel.
	 */
	public int getTabCount() {
		return this.window.getTabCount() ;
	}

	/**
	 * Pauses the window.
	 */
	public void pause() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.pause();		
			}
		});			
	}

}

// ***
