package edu.fusion.clientb;

import edu.fusion.clientb.ui.LoginFrameB;

import javax.swing.SwingUtilities;

public class ClientBApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrameB().setVisible(true));
    }
}
