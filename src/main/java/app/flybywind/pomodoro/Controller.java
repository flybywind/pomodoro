package app.flybywind.pomodoro;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller implements Initializable{
    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

    @FXML
    private TextField todoInput;

    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.log(Level.INFO, "Url = " + location + ", ResouceBundle = " + resources);
        todoInput.setText("hello world!");
    }
    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
       if (keyEvent.getCode() == KeyCode.ENTER) {
           LOGGER.log(Level.INFO, "enter pressed");
       }
    }
}
