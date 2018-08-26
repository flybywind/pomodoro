package app.flybywind.pomodoro.util;

import com.google.common.base.Joiner;

import java.util.logging.Logger;

public class Util {
    static Joiner jOnLine = Joiner.on("    \n");
    public static String getFullStackTrace(Exception e) {
        return jOnLine.join(e.getStackTrace());
    }
    public static <T> Logger getLogger(Class<T> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}
