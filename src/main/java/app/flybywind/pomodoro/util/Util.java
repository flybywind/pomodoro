package app.flybywind.pomodoro.util;

import com.google.common.base.Joiner;

public class Util {
    static Joiner jOnLine = Joiner.on("    \n");
    public static String getFullStackTrace(Exception e) {
        return jOnLine.join(e.getStackTrace());
    }
}
