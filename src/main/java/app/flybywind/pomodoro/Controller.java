package app.flybywind.pomodoro;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;

import java.awt.event.ActionEvent;
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

    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.log(Level.INFO, "Url = " + location + ", ResouceBundle = " + resources);
        todoInput.setText("todo1");
        pomodoroTabs.selectionModelProperty().addListener((observable, oldValue, newValue) -> {
            // stop old tab timer and start new one
            ((PomodoroTab)oldValue.getSelectedItem().getContent()).stopLastPomodoro();
            ((PomodoroTab)newValue.getSelectedItem().getContent()).startOnePomodoro();
        });
    }
    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
       if (keyEvent.getCode() == KeyCode.ENTER && keyEvent.getSource().equals(todoInput)) {
           String pomodoroName = todoInput.getText();
           LOGGER.log(Level.INFO, "enter pressed, create pomodoro: " + pomodoroName);
           PomodoroTab pom = new PomodoroTab(pomodoroName);
           Pair<Integer, Tab> oldTabWithIndex = tabMap.get(pomodoroName);
           if (oldTabWithIndex != null) {
               int pos = oldTabWithIndex.getKey();
               oldTabWithIndex.getValue().setContent(pom);
               pomodoroTabs.getSelectionModel().select(pos);
           } else {
               Tab tab = new Tab();
               tab.setContent(pom);
               int pos = tabMap.size();
               tabMap.put(pomodoroName, new Pair<>(pos, tab));
               pomodoroTabs.getTabs().add(tab);
               pomodoroTabs.getSelectionModel().select(pos);
           }
       }

    }
}
