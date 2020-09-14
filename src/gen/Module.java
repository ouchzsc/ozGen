package gen;

import java.util.HashMap;

public class Module {
    public String name;
    public HashMap<String, Config> configMap = new HashMap<>();

    public Module(String name) {
        this.name = name;
    }

    public boolean addConfig(String name, Config config) {
        if (configMap.containsKey(name)) {
            return false;
        }
        configMap.put(name, config);
        return true;
    }
}
