package org.example.client.view.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.client.view.GUI.GUIController.GUIController;

public class GUIApp extends Application {

    private static String host =  "localhost";

    public static void setHost(String host) {
        GUIApp.host = host;
    }


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                GUIApp.class.getResource("/java/org/example/client/view/GUI/fxml/main-scene.fxml")
        );
        Parent root = loader.load();

        GUIController controller = loader.getController();
        controller.setHost(host);

        stage.setTitle("MESOS - Client GUI");
        stage.setScene(new Scene(root, 470, 200));
        stage.setResizable(false);
        stage.show();
    }


}
