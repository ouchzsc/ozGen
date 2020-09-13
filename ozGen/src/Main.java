import gen.*;
import logger.LoggerWrap;

public class Main {
    public static void main(String[] args) {
        LoggerWrap.instance.init();
        cfgMgr.instance.gen("data");
    }
}
