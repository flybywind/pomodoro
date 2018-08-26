package app.flybywind.pomodoro;

import app.flybywind.pomodoro.util.Util;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller implements Initializable{
    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

    @FXML
    private TextField todoInput;
    @FXML
    private TabPane pomodoroTabs;
    private Map<String, Pair<Integer, Tab>> tabMap = new HashMap<>();
    private static Map<String, PomodoroTab> pomMap = new HashMap<>();

    static public void stop() {
        for (Map.Entry<String, PomodoroTab> e : pomMap.entrySet()) {
            e.getValue().end();
        }
    }
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.log(Level.INFO, "Url = " + location + ", ResouceBundle = " + resources);
        todoInput.setText("todo1");
        pomodoroTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // end old tab timer and begin new one
            LOGGER.log(Level.INFO, "switch tab between [" + oldValue + "] and [" + newValue + "].");
            try {
                if (oldValue != null) {
                    (pomMap.get(oldValue.getText())).end();
                }
                (pomMap.get(newValue.getText())).begin();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, Util.getFullStackTrace(e));
            }
        });
    }
    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
       if (keyEvent.getCode() == KeyCode.ENTER && keyEvent.getSource().equals(todoInput)) {
           String pomodoroName = todoInput.getText();
           LOGGER.log(Level.INFO, "enter pressed, create pomodoro: " + pomodoroName);
           PomodoroTab pom = new PomodoroTab(pomodoroName);
           Pair<Integer, Tab> oldTabWithIndex = tabMap.get(pomodoroName);
           pomMap.put(pomodoroName, pom);
           if (oldTabWithIndex != null) {
               int pos = oldTabWithIndex.getKey();
               oldTabWithIndex.getValue().setContent(pom);
               pomodoroTabs.getSelectionModel().select(pos);
           } else {
               Tab tab = new Tab(pomodoroName);
               LOGGER.info("create tab: " + tab);
               tab.setContent(new ScrollPane(pom));

               int pos = tabMap.size();
               tabMap.put(pomodoroName, new Pair<>(pos, tab));
               pomodoroTabs.getTabs().add(tab);
               pomodoroTabs.getSelectionModel().select(pos);
           }
       }

    }
}
