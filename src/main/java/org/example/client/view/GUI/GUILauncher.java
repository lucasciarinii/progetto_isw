package org.example.client.view.GUI;

import javafx.application.Application;
import org.example.network.CommunicationProtocol;

/**
 * Entry point helper for starting the GUI client.
 */
public final class GUILauncher {

    private GUILauncher() {}

    /**
     * Configures the GUI application and launches JavaFX.
     *
     * @param host     the server host
     * @param protocol the communication protocol
     */
    public static void launchGuiClient(String host, CommunicationProtocol protocol) {
        GUIApp.setHost(host);
        GUIApp.setProtocol(protocol);
        Application.launch(GUIApp.class);
    }
}
