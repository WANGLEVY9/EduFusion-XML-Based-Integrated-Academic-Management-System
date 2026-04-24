package edu.fusion.clienta;

import edu.fusion.clienta.ui.LoginFrameA;

import javax.swing.SwingUtilities;

public class ClientAApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrameA().setVisible(true));
    }
}
