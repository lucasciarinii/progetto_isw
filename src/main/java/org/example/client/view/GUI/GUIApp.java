package org.example.client.view.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.client.view.GUI.GUIController.GUILoginController;
import org.example.network.CommunicationProtocol;

public class GUIApp extends Application {

    // ── Runtime config (set by launcher) ───────────────────────────────────────
    private static String host =  "localhost";
    private static CommunicationProtocol protocol = CommunicationProtocol.RMI;
    private static int port = 1099;

    // ── Injected startup options ───────────────────────────────────────────────
    public static void setHost(String host) {
        GUIApp.host = host;
    }

    public static void setProtocol(CommunicationProtocol protocol) {
        GUIApp.protocol = protocol;
    }

    public static void setPort(int port) {
        GUIApp.port = port;
    }


    // ── JavaFX entry point ─────────────────────────────────────────────────────
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                GUIApp.class.getResource("/fxml/login.fxml")
        );
        Parent root = loader.load();

        GUILoginController controller = loader.getController();
        controller.setHost(host);
        controller.setProtocol(protocol);
        controller.setPort(port);
        controller.setStage(stage);

        stage.setTitle("MESOS - Client GUI");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setWidth(1367);
        stage.setHeight(768);
        stage.centerOnScreen();
        stage.show();

    }


}
