package org.example.client.view.gui;

import javafx.application.Application;

public final class FxLauncher {

    private FxLauncher() {}

    public static void launchClient(String host) {
        FxApp.setHost(host);
        Application.launch(FxApp.class);
    }
}
