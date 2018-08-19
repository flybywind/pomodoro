package app.flybywind.pomodoro;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 创建番茄tab
 * 公用一个番茄map，因为一次只能有一个番茄在计时
 */
public class PomodoroTab extends BorderPane {
    final private Logger LOGGER = Logger.getLogger(PomodoroItem.class.getName());
    private String pomodoroName;
    final private Label commandLabel;
    final static private int PomodoroTimeLength = 60000/3; // 25分钟
    final static private int PomodoroBreakLength = 60000/10; //  5分钟
    private GridPane pomodoroListPane = new GridPane();
    private Queue<PomodoroItem> todoItems = new LinkedBlockingQueue<>();
    private ComboBox dropDownCommand = new ComboBox();
    private int todoNum = 0;
    final private IntegerProperty isStoped = new SimpleIntegerProperty(0);
    public PomodoroTab(String name) {
        pomodoroName = name;
        commandLabel = new Label("操作");
        commandLabel.setFont(new Font(20));
        dropDownCommand.getItems().addAll("进行", "暂停", "重启", "结束");
        dropDownCommand.getSelectionModel().select(0);
        dropDownCommand.setBorder(new Border(new BorderStroke(
                Color.GRAY, BorderStrokeStyle.SOLID,null,
                new BorderWidths(0, 0, 2, 0))));
        dropDownCommand.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.equals("暂停")) {
                end();
            } else if (newValue.equals("结束")) {
                kill();
            } else if (newValue.equals("重启") || newValue.equals("进行")) {
                begin();
            }
        }));
        isStoped.addListener(((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0) {
                stopLastPomodoro();
            } else {
                startOnePomodoro();
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

        // todo: no effective
        setOnKeyPressed(event -> {
            // begin
            if (event.getCode() == KeyCode.B) {
                if (isStoped.get() > 1) {
                    begin();
                }
            }
            // end
            if (event.getCode() == KeyCode.E) {
                if (isStoped.get() <= 0) {
                    end();
                }
            }

            if (event.getCode() == KeyCode.K) {
                kill();
            }
        });
    }

    public void end() {
        isStoped.setValue(1);
    }
    public void begin() {
        isStoped.setValue(0);
    }
    public void kill() {
        dropDownCommand.setDisable(true);
        end();
    }
    private void stopLastPomodoro() {
        todoItems.peek().stop();
    }

    private void startOnePomodoro() {
        todoItems.add(new PomodoroItem(pomodoroListPane));
    }
    class PomodoroItem {
        private final Logger LOGGER = Logger.getLogger(PomodoroItem.class.getName());
        final ProgressIndicator pomodoroProgIndicator = new ProgressIndicator(0),
                breakProgIndicator = new ProgressIndicator(0);
        ScaleDoubleProperty pomodoroTimeProg, breakTimeProg;
        final int TimerInterval = 1000;
        Timer pomodoroTimer = new Timer();

        PomodoroItem(GridPane grid) {
            HBox hbox = new HBox(pomodoroProgIndicator, breakProgIndicator);
            hbox.setSpacing(5);
            hbox.setAlignment(Pos.CENTER);
            hbox.setPadding(new Insets(5,5,5,5));
            pomodoroProgIndicator.getStyleClass().add("pomodoro-indicator");
            pomodoroProgIndicator.setPrefSize(100, 100);
            breakProgIndicator.getStyleClass().add("break-indicator");
            breakProgIndicator.setPrefSize(80, 100);
            breakProgIndicator.setPadding(new Insets(20, 0, 0, 0));
            final Label subPomodoro = new Label(String.format("[%2d]%s: ", todoNum+1, pomodoroName));
            subPomodoro.setFont(new Font(18));
            subPomodoro.setTextAlignment(TextAlignment.RIGHT);
            grid.add(subPomodoro, 0, todoNum);
            grid.add(hbox, 1, todoNum);
            ++todoNum;
            hbox.setSpacing(20);
            pomodoroTimeProg = new ScaleDoubleProperty(PomodoroTimeLength);
            breakTimeProg = new ScaleDoubleProperty(PomodoroBreakLength);
            pomodoroProgIndicator.progressProperty().bind(pomodoroTimeProg);
            breakProgIndicator.progressProperty().bind(breakTimeProg);

            pomodoroTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    LOGGER.log(Level.INFO, "pomodoro = " + pomodoroTimeProg.doubleValue() + ", break = " + breakTimeProg);
                    double pomVal =  pomodoroTimeProg.getRealValue();
                    if (pomVal + TimerInterval >= PomodoroTimeLength) {
                        pomodoroProgIndicator.progressProperty().unbind();
                        pomodoroProgIndicator.setProgress(1);
                        double breakVal = breakTimeProg.getRealValue();
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
            }, TimerInterval, TimerInterval);
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
