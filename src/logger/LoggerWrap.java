package logger;


public enum LoggerWrap {
    instance;

    public Logger xml;

    public void init() {
        xml = new Logger("xml", false);
    }
}

