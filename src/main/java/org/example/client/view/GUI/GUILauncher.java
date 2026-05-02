package org.example.client.view.GUI;

import javafx.application.Application;

public final class GUILauncher {

    private GUILauncher() {}

    public static void launchGuiClient(String host) {
        GUIApp.setHost(host);
        Application.launch(GUIApp.class);
    }
}
