package app.flybywind.pomodoro;

import javafx.beans.property.SimpleDoubleProperty;

public class ScaleDoubleProperty extends SimpleDoubleProperty {
    double scale;
    ScaleDoubleProperty() {
        this.scale = 1.0;
    }
    ScaleDoubleProperty(double scale) {
        this.scale = scale;
    }

    public double getRealValue() {
        return super.get();
    }
    @Override
    public double get() {
        return super.get()/scale;
    }
}
