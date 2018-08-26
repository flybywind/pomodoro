package app.flybywind.pomodoro;

import app.flybywind.pomodoro.util.Util;
import com.google.common.collect.Iterators;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller implements Initializable{
    private static final Logger LOGGER = Util.getLogger(Controller.class);

    @FXML
    private TextField todoInput;
    @FXML
    private TabPane pomodoroTabs;
    private Map<String, Tab> tabMap = new LinkedHashMap<>();
    private static Map<String, PomodoroTab> pomMap = new HashMap<>();

    static public void stop() {
        for (Map.Entry<String, PomodoroTab> e : pomMap.entrySet()) {
            e.getValue().end();
        }
    }
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.log(Level.FINEST, "Url = " + location + ", ResouceBundle = " + resources);
        todoInput.setText("todo1");
        pomodoroTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // end old tab timer and begin new one
            LOGGER.log(Level.INFO, "switch tab between [" + oldValue + "] and [" + newValue + "].");
            try {
                if (oldValue != null) {
                    (pomMap.get(oldValue.getText())).end();
                }
                if (newValue != null) {
                    (pomMap.get(newValue.getText())).begin();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, Util.getFullStackTrace(e));
            }
        });
    }
    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
       if (keyEvent.getCode() == KeyCode.ENTER && keyEvent.getSource().equals(todoInput)) {
           final String pomodoroName = todoInput.getText();
           LOGGER.log(Level.INFO, "enter pressed, create pomodoro: " + pomodoroName);
           PomodoroTab pom = new PomodoroTab(pomodoroName);
           Tab oldTab = tabMap.get(pomodoroName);
           pomMap.put(pomodoroName, pom);
           if (oldTab != null) {
               oldTab.setContent(pom);
               Iterator<Map.Entry<String, Tab>> iter = tabMap.entrySet().iterator();
               int pos = Iterators.indexOf(iter, e -> e.getKey().equalsIgnoreCase(pomodoroName));
               pomodoroTabs.getSelectionModel().select(pos);
           } else {
               Tab tab = new Tab(pomodoroName);
               tab.setOnClosed(evt -> {
                   Iterator<Map.Entry<String, Tab>> iter = tabMap.entrySet().iterator();
                   int pos = Iterators.indexOf(iter, e -> e.getKey().equalsIgnoreCase(pomodoroName));
                   tabMap.remove(pomodoroName);
                   int sz = tabMap.size();
                   if (pos < sz) {
                       LOGGER.log(Level.FINEST, "open next tab");
                       pomodoroTabs.getSelectionModel().select(pos);
                   } else if (sz > 0){
                       LOGGER.log(Level.FINEST, "open first tab");
                       pomodoroTabs.getSelectionModel().select(0);
                   } else {
                       // all tabs closed, do nothing.
                   }
               });
               LOGGER.info("create tab: " + tab);
               tab.setContent(new ScrollPane(pom));

               int pos = tabMap.size();
               tabMap.put(pomodoroName, tab);
               pomodoroTabs.getTabs().add(tab);
               pomodoroTabs.getSelectionModel().select(pos);
           }
       }

    }
}
