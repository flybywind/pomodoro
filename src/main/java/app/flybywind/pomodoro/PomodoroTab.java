package app.flybywind.pomodoro;

import com.sun.jmx.remote.internal.ArrayQueue;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 创建番茄tab
 * 公用一个番茄map，因为一次只能有一个番茄在计时
 */
public class PomodoroTab extends BorderPane {
    private String pomodoroName;
    final static private Label commandLabel;
    final static private int PomodoroTimeLength = 25*60000; // 25分钟
    final static private int PomodoroBreakLength = 5*60000; //  5分钟
    static {
        commandLabel = new Label("操作");
        commandLabel.setFont(new Font(20));
    }
    private GridPane pomodoroListPane = new GridPane();
    private Queue<PomodoroItem> todoItems = new LinkedBlockingQueue<>();
    private ListView<String> dropDownCommand = new ListView<>();
    private int todoNum = 0;
    final private IntegerProperty isStoped = new SimpleIntegerProperty(0);
    public PomodoroTab(String name) {
        pomodoroName = name;
        dropDownCommand.getItems().addAll("暂停", "重启", "结束");
        dropDownCommand.setBorder(new Border(new BorderStroke(
                Color.GRAY, BorderStrokeStyle.SOLID,null,
                new BorderWidths(0, 0, 2, 0))));
        dropDownCommand.setOnMouseClicked(event -> {
            //todo
            System.out.println("source = " + event.getSource() + ",\ntarget = " + event.getTarget());
        });
        isStoped.addListener(((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0) {
                todoItems.peek().stop();
            } else {
                todoItems.add(new PomodoroItem(pomodoroListPane));
            }
        }));
        HBox head = new HBox(commandLabel, dropDownCommand);
        head.setSpacing(3);
        this.setTop(head);
        pomodoroListPane.setVgap(5);
        pomodoroListPane.setHgap(2);
        pomodoroListPane.getStyleClass().add("pomodoro-list");
        this.setCenter(pomodoroListPane);
        startOnePomodoro();
    }

    public void stopLastPomodoro() {
        todoItems.peek().stop();
    }

    public void startOnePomodoro() {
        todoItems.add(new PomodoroItem(pomodoroListPane));
    }
    class PomodoroItem {
        final ProgressIndicator pomodoroProgIndicator = new ProgressIndicator(0),
                breakProgIndicator = new ProgressIndicator(0);
        DoubleProperty pomodoroTimeProg, breakTimeProg;
        final int TimerInterval = 3000;
        Timer pomodoroTimer = new Timer();

        PomodoroItem(GridPane grid) {
            HBox hbox = new HBox(pomodoroProgIndicator, breakProgIndicator);
            pomodoroProgIndicator.getStyleClass().add("pomodoro-indicator");
            pomodoroProgIndicator.getStyleClass().add("break-indicator");
            final RadioButton radioBtn = new RadioButton("pending");
            radioBtn.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                if (oldValue == false) {
                    stop();
                    radioBtn.setDisable(true);
                }
            }));
            grid.add(radioBtn, 0, todoNum);
            grid.add(hbox, 1, todoNum);
            ++todoNum;
            hbox.setSpacing(20);
            pomodoroTimeProg = new SimpleDoubleProperty(0);
            breakTimeProg = new SimpleDoubleProperty(0);
            pomodoroProgIndicator.progressProperty().bind(pomodoroTimeProg);
            breakProgIndicator.progressProperty().bind(breakTimeProg);

            pomodoroTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    double pomVal =  pomodoroTimeProg.doubleValue();
                    if (pomVal + TimerInterval >= PomodoroTimeLength) {
                        pomodoroProgIndicator.progressProperty().unbind();
                        pomodoroProgIndicator.setProgress(1);
                        double breakVal = breakTimeProg.doubleValue();
                        if (breakVal + TimerInterval >= PomodoroBreakLength) {
                            breakProgIndicator.progressProperty().unbind();
                            breakProgIndicator.setProgress(1);
                            pomodoroTimer.cancel();
                        } else {
                            breakTimeProg.setValue(breakVal + TimerInterval);
                        }
                    } else {
                        pomodoroTimeProg.setValue(pomVal + TimerInterval);
                    }
                }
            }, 0, TimerInterval);
        }
        void stop() {
            pomodoroProgIndicator.progressProperty().unbind();
            breakProgIndicator.progressProperty().unbind();
            pomodoroTimer.cancel();
        }
        void incrPomodoroTime(int i) {
            double v = pomodoroTimeProg.get();
            v = (v + i)/PomodoroTimeLength;
            pomodoroTimeProg.setValue(v);
        }
        void incrBreakTime(int i) {
            double v = breakTimeProg.get();
            v = (v + i)/PomodoroBreakLength;
            breakTimeProg.setValue(v);
        }
    }
}
