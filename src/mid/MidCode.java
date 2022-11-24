package mid;

import mid.frame.FuncFrame;
import mid.operand.Symbol;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MidCode {
    private final Map<String, String> globalStr;
    private final Map<String, Symbol> globalSym;
    private final Map<String, FuncFrame> funcTab;
    private FuncFrame mainFunc = null;

    // generator
    private static int strCnt = 0;
    private static int IdCnt = 0;
    private int globalStackSize = 0;

    public MidCode() {
        funcTab = new LinkedHashMap<>();
        globalStr = new LinkedHashMap<>();
        globalSym = new LinkedHashMap<>();
    }

    // tag id generator
    public static int genId() {
        return IdCnt++;
    }

    // basic
    public int addStackSize(int size) {
        globalStackSize += size;
        return globalStackSize;
    }

    public void putFunc(FuncFrame funcFrame) {
        funcTab.put(funcFrame.getIdent(), funcFrame);
    }

    public void setMainFunc(FuncFrame func) {
        mainFunc = func;
    }

    public FuncFrame getFunc(String funcName) {
        return funcTab.getOrDefault(funcName, null);
    }

    public FuncFrame getMainFunc() {
        return mainFunc;
    }

    public Iterator<FuncFrame> funcIter() {
        return funcTab.values().iterator();
    }

    public void putGlobalSym(Symbol symbol) {
        globalSym.put(symbol.getIdent(), symbol);
    }

    public Iterator<Symbol> symIter() {
        return globalSym.values().iterator();
    }

    public String genStrLabel(String str) {
        String strLabel = "str" + strCnt;
        globalStr.put(strLabel, str);
        ++strCnt;
        return strLabel;
    }

    public String getStr(String label) {
        return globalStr.get(label);
    }

    public Iterator<String> strLabelIter() {
        return globalStr.keySet().iterator();
    }

    // output
    public void outputMidCode(PrintStream ps) {
        // Global value
        ps.println("# Global Value:");
        for (Symbol symbol : globalSym.values()) {
            ps.println("[0x" + Integer.toHexString(symbol.getOffset()) + "]: " + symbol.getIdent());
        }
        ps.println();
        // Global Str
        ps.println("# Global String:");
        for (String label : globalStr.keySet()) {
            ps.println(label + ": \"" + globalStr.get(label) + "\"");
        }
        ps.println();
        // Functions
        for (FuncFrame func : funcTab.values()) {
            ps.println(func.toString());
        }
        ps.println(mainFunc.toString());
    }
}
