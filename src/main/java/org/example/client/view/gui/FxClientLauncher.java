package org.example.client.view.gui;

import javafx.application.Application;

public final class FxClientLauncher {

    private FxClientLauncher() {}

    public static void launchClient(String host) {
        FxClientApp.setHost(host);
        Application.launch(FxClientApp.class);
    }
}
