package gen;

import logger.LoggerWrap;

import java.util.HashMap;
import java.util.function.Consumer;

public class utils {

    public static String replaceName2indexStr(Config config, String txt) {
        StringBuilder name2Index = new StringBuilder();
        name2Index.append("\n");
        for (int i = 0; i < config.varList.size(); i++) {
            CfgVar beanVar = config.varList.get(i);
            name2Index.append(String.format("    %s = %s,\n", beanVar.name, i + 1));
        }
        txt = txt.replaceFirst("<name2index>", name2Index.toString());
        return txt;
    }

    public static String replaceDataListStr(Config config, String txt) {
        var var_id = config.getVar(config.indexName);

        StringBuilder dataTxt = new StringBuilder();
        for (int i = 0; i < config.dataList.size(); i++) {
            var dataStrArray = config.dataList.get(i);
            String idFmt = utils.getValueInCodeFmt(var_id.typeName, dataStrArray[var_id.columnIndex]);
            dataTxt.append(String.format("    [%s] = { %s},\n", idFmt, replaceDataStr(config, dataStrArray)));
        }
        txt = txt.replaceFirst("<data>", dataTxt.toString());
        return txt;
    }

    public static String replaceDataStr(Config config, String[] dataStrArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dataStrArray.length; i++) {
            CfgVar beanVar = config.getVar(i);
            sb.append(getValueInCodeFmt(beanVar.typeName, dataStrArray[i]));
            sb.append(", ");
        }
        return sb.toString();
    }

    public static String getValueInCodeFmt(String type, String content) {
        switch (type) {
            case "string":
                return String.format("\"%s\"", content.replaceAll("\"", "\\\\\\\\\\\\\""));
            case "bool":
                return content.equals("0") ? "false" : "true";
            case "int":
                return content;
            case "list,int":
                return String.format("{%s}",content) ;
            case "list,string":
                var refIds = content.split(",");
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append("{");
                for (int i = 0; i < refIds.length; i++) {
                    var refId = refIds[i];
                    strBuilder.append("\"").append(refId).append("\"").append(",");
                }
                strBuilder.append("}");
                return strBuilder.toString();
        }
        return null;
    }

    public static String replaceEnums(Config config, String txt) {
        if (config.enumName == null)
            return txt.replaceFirst("<enums>", "");
        StringBuilder enumTxtStringBuilder = new StringBuilder();
        enumTxtStringBuilder.append("\n");
        var enumVar = config.getVar(config.enumName);
        var idVar = config.getVar(config.indexName);
        for (int i = 0; i < config.dataList.size(); i++) {
            var strings = config.dataList.get(i);
            var enumValue = strings[enumVar.columnIndex];
            if (enumValue.equals(""))
                continue;
            if (enumValue.contains(" ")) {
                LoggerWrap.instance.xml.error(String.format("%s.csv:%s, space in enum", config.name, enumValue));
                System.exit(-1);
            }
            enumTxtStringBuilder.append(String.format("%s.%s = %s.get(%s)",
                    config.name,
                    strings[enumVar.columnIndex],
                    config.name,
                    getValueInCodeFmt(idVar.typeName, strings[idVar.columnIndex])));
            enumTxtStringBuilder.append("\n");
        }
        return txt.replaceFirst("<enums>", enumTxtStringBuilder.toString());
    }

    public static String replaceRefGet(String template, Config config, String txt) {
        HashMap<String, Config> refName2Config = config.getRefName2Config();

        if (refName2Config.size() == 0)
            return txt.replaceFirst("<refGet>", "");

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("\n");
        refName2Config.forEach((refName, cfg) -> {
            strBuilder.append(genAssetGetTxt(template, refName, cfg));
        });
        strBuilder.append("\n");
        return txt.replaceFirst("<refGet>", strBuilder.toString());
    }

    public static String genAssetGetTxt(String template, String refName, Config config) {
        return template.replaceAll("<module>", config.moduleName).replaceAll("<config>", config.name);
    }

    public static String replaceName2Ref(Config config, String txt) {
        var varList = config.getRefVarList();

        if (varList.size() == 0)
            return txt.replaceFirst("<name2ref>", "");

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("\n");
        varList.forEach(cfgVar -> strBuilder.append(
                String.format("    %s_ref = { %s, %s_get },\n", cfgVar.name, cfgVar.columnIndex, cfgVar.refName.replace('.','_'))));
        return txt.replaceFirst("<name2ref>", strBuilder.toString());
    }
}
