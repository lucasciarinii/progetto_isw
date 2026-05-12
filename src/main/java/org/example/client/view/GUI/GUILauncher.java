package org.example.client.view.GUI;

import javafx.application.Application;
import org.example.network.CommunicationProtocol;

public final class GUILauncher {

    private GUILauncher() {}

    public static void launchGuiClient(String host, CommunicationProtocol protocol, int port) {
        GUIApp.setHost(host);
        GUIApp.setProtocol(protocol);
        GUIApp.setPort(port);
        Application.launch(GUIApp.class);
    }
}
