package gen;

public class CfgVar {
    public String name;
    public String typeName;
    public String refName;
    public int columnIndex;

    public CfgVar(String name, String typeName, int columnIndex) {
        this.name = name;
        this.typeName = typeName;
        this.columnIndex = columnIndex;
    }
}
