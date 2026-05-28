package org.example.client.view.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.client.view.GUI.GUIController.GUILoginController;
import org.example.network.CommunicationProtocol;

/**
 * JavaFX application for the GUI client.
 */
public class GUIApp extends Application {

    // ── Runtime config (set by launcher) ───────────────────────────────────────
    private static String host =  "localhost";
    private static CommunicationProtocol protocol = CommunicationProtocol.RMI;

    // ── Injected startup options ───────────────────────────────────────────────
    /** @param host the server host */
    public static void setHost(String host) {
        GUIApp.host = host;
    }

    /** @param protocol the communication protocol */
    public static void setProtocol(CommunicationProtocol protocol) {
        GUIApp.protocol = protocol;
    }


    // ── JavaFX entry point ─────────────────────────────────────────────────────
    /**
     * Loads the login scene and configures the GUI controller.
     */
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                GUIApp.class.getResource("/fxml/login.fxml")
        );
        Parent root = loader.load();

        GUILoginController controller = loader.getController();
        controller.setHost(host);
        controller.setProtocol(protocol);
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
