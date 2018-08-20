package app.flybywind.pomodoro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class Main extends Application {
    static private final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String appCssUrl = Main.class.getResource("pom-style.css").toExternalForm();
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Pomodoro");
        Scene scene = new Scene(root, 600, 510);
        scene.getStylesheets().add(appCssUrl);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
