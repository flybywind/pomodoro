package app.flybywind.pomodoro;

import app.flybywind.pomodoro.util.Util;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;


/**
 * 创建番茄tab
 * 公用一个番茄map，因为一次只能有一个番茄在计时
 */
public class PomodoroTab extends BorderPane {
    final private Logger LOGGER = Util.getLogger(PomodoroItem.class);
    private String pomodoroName;
    final private Label commandLabel;
    final static private int PomodoroTimeLength = 60000/3; // 25分钟
    final static private int PomodoroBreakLength = 60000/10; //  5分钟
    private Stack<PomodoroItem> todoItems = new Stack<>();
    private ComboBox dropDownCommand = new ComboBox();
    final private IntegerProperty isStoped = new SimpleIntegerProperty(0);
    private boolean isKilled = false;
    final private List<HBox> itemsHbox = new ArrayList<>();
    public PomodoroTab(String name) {
        pomodoroName = name;
        commandLabel = new Label("操作");
        commandLabel.setFont(new Font(20));
        dropDownCommand.getItems().addAll("新建", "暂停", "重启", "结束");
        dropDownCommand.setBorder(new Border(new BorderStroke(
                Color.GRAY, BorderStrokeStyle.SOLID,null,
                new BorderWidths(0, 0, 2, 0))));
        dropDownCommand.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.equals("暂停")) {
                end();
            } else if (newValue.equals("结束")) {
                kill();
            } else if (newValue.equals("重启")) {
                begin();
            } else if (newValue.equals("新建")) {
                end();
                begin();
            }
        }));
        isStoped.addListener(((observable, oldValue, newValue) -> {
            LOGGER.info(pomodoroName +": isStop = " + newValue  + " <-- " + oldValue);
            if (newValue.doubleValue() > 0) {
                stopLastPomodoro();
            } else {
                startOnePomodoro();
            }
        }));
        HBox head = new HBox(commandLabel, dropDownCommand);
        head.setSpacing(3);
        this.setTop(head);
        startOnePomodoro();

    }

    public void end() {
        LOGGER.log(Level.FINEST, "set pom to end");
        isStoped.setValue(1);
    }
    public void begin() {
        if (!isKilled) {
            LOGGER.log(Level.FINEST, "set pom to begin");
            this.requestFocus();
            isStoped.setValue(0);
        } else {
            new Alert(Alert.AlertType.INFORMATION, "Pomodoro has killed.").showAndWait();
//                    .ifPresent(resp -> {
//                        if (resp == ButtonType.OK) {
//                            formatSystem();
//                        }
//                    });
        }
    }
    public void kill() {
        dropDownCommand.setDisable(true);
        isKilled = true;
        end();
    }
    private void stopLastPomodoro() {
        todoItems.peek().stop();
    }

    private void startOnePomodoro() {
        GridPane pomodoroListPane = reInitGrid();
        todoItems.push(new PomodoroItem(pomodoroListPane));
    }
    private GridPane reInitGrid() {
        GridPane pomodoroListPane = new GridPane();
        pomodoroListPane.setVgap(5);
        pomodoroListPane.setHgap(2);
        pomodoroListPane.getStyleClass().add("pomodoro-list");
        this.setCenter(pomodoroListPane);
        return pomodoroListPane;
    }
    class PomodoroItem {
        private final Logger LOGGER = Util.getLogger(PomodoroItem.class);
        final ProgressIndicator pomodoroProgIndicator = new ProgressIndicator(0),
                breakProgIndicator = new ProgressIndicator(0);
        ScaleDoubleProperty pomodoroTimeProg, breakTimeProg;
        final int TimerInterval = 1000;
        Timer pomodoroTimer = new Timer();

        PomodoroItem(GridPane grid) {
            final int todoNum = itemsHbox.size() + 1;
            final Label subPomodoro = new Label(String.format("[%2d]%s: ", todoNum, pomodoroName));
            subPomodoro.setFont(new Font(18));
            subPomodoro.setTextAlignment(TextAlignment.RIGHT);
            final HBox hbox = new HBox(subPomodoro, pomodoroProgIndicator, breakProgIndicator);
            hbox.setAlignment(Pos.CENTER);
            hbox.setPadding(new Insets(5,5,5,5));
            hbox.setSpacing(20);
            pomodoroProgIndicator.getStyleClass().add("pomodoro-indicator");
            pomodoroProgIndicator.setPrefSize(100, 100);
            breakProgIndicator.getStyleClass().add("break-indicator");
            breakProgIndicator.setPrefSize(80, 80);
            breakProgIndicator.setPadding(new Insets(20, 0, 0, 0));
            itemsHbox.add(hbox);
            if (todoNum == 1) {
                grid.add(hbox, 0, 0);
            } else {
                hbox.setOpacity(0);
                hbox.setScaleX(0.1);
                hbox.setScaleY(0.1);
                grid.add(hbox, 0, 0);
                // define animation
                Duration newPomShowupDuration = Duration.seconds(0.5);
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().add(
                        new KeyFrame(newPomShowupDuration,
                                new KeyValue(hbox.opacityProperty(), 1),
                                new KeyValue(hbox.scaleXProperty(), 1),
                                new KeyValue(hbox.scaleYProperty(), 1)));
                GridPane subGrid = new GridPane();
                IntStream.range(1, todoNum).forEach(i -> {
                    HBox hbox_ = itemsHbox.get(todoNum-1-i);
                    hbox_.getStyleClass().add("sub-pom-stopped");
                    subGrid.add(hbox_,
                            0, i-1);
                });
                grid.add(subGrid, 0, 1);
                Duration subSlipDownDuration = Duration.seconds(0.5);
                TranslateTransition translate = new TranslateTransition(subSlipDownDuration, subGrid);
                translate.setFromY(-115);
                translate.setToY(0);
                timeline.play();
                translate.play();
            }

            pomodoroTimeProg = new ScaleDoubleProperty(PomodoroTimeLength);
            breakTimeProg = new ScaleDoubleProperty(PomodoroBreakLength);
            pomodoroProgIndicator.progressProperty().bind(pomodoroTimeProg);
            breakProgIndicator.progressProperty().bind(breakTimeProg);

            pomodoroTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    LOGGER.log(Level.FINEST, "pomodoro = " + pomodoroTimeProg.doubleValue() + ", break = " + breakTimeProg);
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
