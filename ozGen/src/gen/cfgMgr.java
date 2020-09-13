package gen;

import logger.LoggerWrap;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.BiConsumer;

public enum cfgMgr {
    instance;

    private String dataRoot;

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
                    LoggerWrap.instance.xml.error(String.format("%s.xml 中有重复的bean:%s", moduleName, config.name));
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
                        LoggerWrap.instance.xml.error(String.format("%s.xml bean:%s 中有重复的var:%s", moduleName, config.name, varName.getValue()));
                        System.exit(-1);
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void gen(String dataRoot) {

        this.dataRoot = dataRoot;

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
                    try {
                        String templateTxt = Files.readString(Paths.get("src/gen/lua/luaConfig"));
                        var txt = templateTxt.replaceAll("<config>", config.name);
                        txt = utils.replaceName2indexStr(config, txt);
                        txt = utils.replaceDataListStr(config, txt);
                        txt = utils.replaceEnums(config, txt);
                        String templateTxt_refGet = Files.readString(Paths.get("src/gen/lua/refGet"));
                        txt = utils.replaceRefGet(templateTxt_refGet, config, txt);
                        txt = utils.replaceName2Ref(config, txt);
                        txt = txt.replaceAll("<module>", module.name);
                        config.content = txt;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

        fileUtils.deleteDir(new File("cfgGen"));

        moduleMap.forEach((s, module) ->
                module.configMap.forEach((s1, config) -> {
                    fileUtils.writeFile(config.content, Path.of("cfgGen", module.name, config.name) + ".lua");
                }));

        try {
            fileUtils.writeFile(Files.readString(Paths.get("src/gen/lua/util")), Path.of("cfgGen", "util") + ".lua");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}