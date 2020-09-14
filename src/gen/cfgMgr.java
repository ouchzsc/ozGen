package gen;

import logger.LoggerWrap;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.BiConsumer;

public enum cfgMgr {
    instance;

    private HashMap<String, Module> moduleMap = new HashMap<>();

    private Module addModule(String moduleName) {
        var module = new Module(moduleName);
        moduleMap.put(moduleName, module);
        return module;
    }

    public Module getModule(String moduleName) {
        return moduleMap.get(moduleName);
    }

    private void parseXML(String fileName) {
        File inputXml = new File(fileName);
        var moduleName = inputXml.getName().split("\\.")[0];
        LoggerWrap.instance.xml.info("module:" + moduleName);
        var module = addModule(moduleName);
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(inputXml);
            Element moduleElem = document.getRootElement();
            for (Iterator i = moduleElem.elementIterator(); i.hasNext(); ) {
                Element beanElem = (Element) i.next();
                var attrName = beanElem.attribute("name");
                var attrIndex = beanElem.attribute("index");
                var attrEnum = beanElem.attribute("enum");

                Config config = new Config(attrName.getValue(), moduleName);
                if (attrIndex != null)
                    config.indexName = attrIndex.getValue();
                if (attrEnum != null)
                    config.enumName = attrEnum.getValue();
                if (!module.addConfig(config.name, config)) {
                    LoggerWrap.instance.xml.error(String.format("%s.xml �����ظ���bean:%s", moduleName, config.name));
                    System.exit(-1);
                }

                int coloumIndex = 0;
                for (Iterator j = beanElem.elementIterator(); j.hasNext(); ) {
                    Element varElem = (Element) j.next();
                    var varName = varElem.attribute("name");
                    var attrType = varElem.attribute("type");
                    var attrRef = varElem.attribute("ref");

                    var cfgVar = new CfgVar(varName.getValue(), attrType.getValue(), coloumIndex++);
                    if (attrRef != null) {
                        cfgVar.refName = attrRef.getValue();
                    }
                    if (!config.addVar(varName.getValue(), cfgVar)) {
                        LoggerWrap.instance.xml.error(String.format("%s.xml bean:%s �����ظ���var:%s", moduleName, config.name, varName.getValue()));
                        System.exit(-1);
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void gen(String dataRoot, String genRoot) {

        //load xml
        File file = new File(dataRoot);
        File[] fs = file.listFiles();
        for (File f : fs) {
            if (f.isDirectory()) {
                parseXML(dataRoot + File.separator + f.getName() + File.separator + f.getName() + ".xml");
            }
        }

        moduleMap.forEach((s, module) ->
                module.configMap.forEach((s12, config) ->
                        config.loadCsv(dataRoot, module)));

        moduleMap.forEach((s, module) ->
                module.configMap.forEach((s1, config) -> {
                    String templateTxt = "local util = require(\"cfgGen.util\")\n" +
                            "local <config> = {}\n" +
                            "\n" +
                            "---@class <module>.<config>\n" +
                            "local name2index = {<name2index>}\n" +
                            "<refGet>\n" +
                            "local name2ref = {<name2ref>}\n" +
                            "\n" +
                            "local data = {\n" +
                            "<data>}\n" +
                            "\n" +
                            "---@return asset.asset\n" +
                            "function <config>.get(id)\n" +
                            "    return data[id]\n" +
                            "end\n" +
                            "\n" +
                            "util.setMetaGet(data, name2index, name2ref)\n" +
                            "<enums>\n" +
                            "return <config>";
                    var txt = templateTxt.replaceAll("<config>", config.name);
                    txt = utils.replaceName2indexStr(config, txt);
                    txt = utils.replaceDataListStr(config, txt);
                    txt = utils.replaceEnums(config, txt);

                    String templateTxt_refGet = "local function <module>_<config>_get(id)\n" +
                            "    local <config> = require(\"cfgGen.<module>.<config>\")\n" +
                            "    return <config>.get(id)\n" +
                            "end\n";

                    txt = utils.replaceRefGet(templateTxt_refGet, config, txt);
                    txt = utils.replaceName2Ref(config, txt);
                    txt = txt.replaceAll("<module>", module.name);
                    config.content = txt;
                }));

        fileUtils.deleteDir(new File(genRoot));

        moduleMap.forEach((s, module) ->
                module.configMap.forEach((s1, config) -> {
                    fileUtils.writeFile(config.content, Path.of(genRoot, module.name, config.name) + ".lua");
                }));

        fileUtils.writeFile("local util = {}\n" +
                "\n" +
                "function util.setMetaGet(data, name2index, name2ref)\n" +
                "    local theMetaTable = {\n" +
                "        __index = function(oneLineData, name)\n" +
                "            local index = name2index[name]\n" +
                "            if index then\n" +
                "                return rawget(oneLineData, index)\n" +
                "            end\n" +
                "            if not name2ref then\n" +
                "                return\n" +
                "            end\n" +
                "            local index_tb = name2ref[name]\n" +
                "            if index_tb then\n" +
                "                return index_tb[2](rawget(oneLineData, index_tb[1]))\n" +
                "            end\n" +
                "        end\n" +
                "    }\n" +
                "    for _, v in pairs(data) do\n" +
                "        setmetatable(v, theMetaTable)\n" +
                "    end\n" +
                "end\n" +
                "\n" +
                "return util", Path.of(genRoot, "util") + ".lua");
    }
}