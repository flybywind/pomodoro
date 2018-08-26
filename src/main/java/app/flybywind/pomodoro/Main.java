package app.flybywind.pomodoro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static final String appCssUrl;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Pomodoro");
        Scene scene = new Scene(root, 600, 510);
        scene.getStylesheets().add(appCssUrl);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public  void stop() {
        Controller.stop();
    }
    static {
        String p = Main.class.getResource("logging.properties").getFile();
        System.setProperty("java.util.logging.config.file", p);
        appCssUrl = Main.class.getResource("pom-style.css").toExternalForm();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
