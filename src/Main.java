import gen.*;
import logger.LoggerWrap;

public class Main {
    public static void main(String[] args) {
        LoggerWrap.instance.init();
        var dataRoot = args[0];
        var genRoot = args[1];
        cfgMgr.instance.gen(dataRoot, genRoot);
    }
}
