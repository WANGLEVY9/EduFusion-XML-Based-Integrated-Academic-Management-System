package edu.fusion.clientc;

import edu.fusion.clientc.ui.LoginFrameC;

import javax.swing.SwingUtilities;

public class ClientCApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrameC().setVisible(true));
    }
}
