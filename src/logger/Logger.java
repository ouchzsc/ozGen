package logger;

public class Logger {
    private boolean enable;

    public Logger(String name, boolean enable) {
        this.enable = enable;
    }


    public void info(Object obj) {
        if (enable)
            System.out.println(obj);
    }

    public void error(Object obj) {
        System.out.println(obj);
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            System.out.println(ste);
        }
    }
}
