package gen;

import com.csvreader.CsvReader;
import logger.LoggerWrap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Config {
    public String name;
    public String moduleName;
    public String indexName;
    public String enumName;
    public String content;

    public HashMap<String, CfgVar> name2Var = new HashMap<>();
    public List<CfgVar> varList = new LinkedList<>();
    public LinkedList<String[]> dataList = new LinkedList<>();
    HashMap<String, Config> refName2Config;
    List<CfgVar> refVarList;

    public Config(String name, String moduleName) {
        this.name = name;
        this.moduleName = moduleName;
    }

    public boolean addVar(String varName, CfgVar beanVar) {
        if (name2Var.containsKey(varName)) {
            return false;
        }
        name2Var.put(varName, beanVar);
        varList.add(beanVar);
        return true;
    }

    public void loadCsv(String root, Module module) {
        var csvPath = Path.of(root, module.name, name) + ".csv";
        try {
            CsvReader csvReader = new CsvReader(csvPath);
            while (csvReader.readRecord()) {
                var values = csvReader.getValues();
                if (values.length <= 0)
                    continue;
                if (values[0].equals(""))
                    continue;
                if (values[0].startsWith("##"))
                    continue;
                dataList.add(values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CfgVar getVar(String varName) {
        return name2Var.get(varName);
    }

    public CfgVar getVar(int index) {
        return varList.get(index);
    }

    public HashMap<String, Config> getRefName2Config() {
        if (refName2Config == null) {
            refName2Config = new HashMap<>();
            for (int i = 0; i < varList.size(); i++) {
                var cfgVar = varList.get(i);
                if (cfgVar.refName == null)
                    continue;
                String refName[] = cfgVar.refName.split("\\.");
                var moduleName = refName[0];
                var configName = refName[1];
                var refModule = cfgMgr.instance.getModule(moduleName);
                var refConfig = refModule.configMap.get(configName);
                refName2Config.put(cfgVar.refName, refConfig);
            }
        }
        return refName2Config;
    }

    public List<CfgVar> getRefVarList() {
        if (refVarList == null) {
            refVarList = new LinkedList<>();
            for (int i = 0; i < varList.size(); i++) {
                var cfgVar = varList.get(i);
                if (cfgVar.refName == null)
                    continue;
                refVarList.add(cfgVar);
            }
        }
        return refVarList;
    }
}
