package org.example.client.view.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.client.view.gui.controller.FxController;

public class FxApp extends Application {

    private static String host =  "localhost";

    public static void setHost(String host) {
        FxApp.host = host;
    }


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                FxApp.class.getResource("/java/org/example/client/view/gui/fxml/main-scene.fxml")
        );
        Parent root = loader.load();

        FxController controller = loader.getController();
        controller.setHost(host);

        stage.setTitle("MESOS - Client GUI");
        stage.setScene(new Scene(root, 470, 200));
        stage.setResizable(false);
        stage.show();
    }


}
