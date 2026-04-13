package main;

import gui.MainFrame;
import javax.swing.SwingUtilities;

/**
 * Main entry point — launches the Swing GUI.
 *
 * The original CLI logic (services, controllers) is unchanged.
 * MainFrame initialises all services and shows the LoginPanel.
 * All GUI ↔ Logic wiring happens inside the gui/ package.
 */
public class Main {
    public static void main(String[] args) {
        // Run on the Event Dispatch Thread as required by Swing
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
