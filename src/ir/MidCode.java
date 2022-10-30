package ir;

import ir.frame.FuncFrame;
import ir.operand.Symbol;

import java.util.HashMap;

public class MidCode {
    private static HashMap<String, FuncFrame> funcTab;
    private static HashMap<String, Symbol> globalSym;
    private static HashMap<String, String> globalStr;
    private static HashMap<String, Integer> globalAddr;
    private static FuncFrame mainFunc = null;

    // generator
    private static int strCnt = 0;
    private static int tagCnt = 0;
    // generate addr of .data
    public static final int DATA = 0;
    public static int dataSize = 0;

    public MidCode() {
        funcTab = new HashMap<>();
        globalSym = new HashMap<>();
        globalStr = new HashMap<>();
        globalAddr = new HashMap<>();
    }
    // ir part
    public static int getGlobalBias(int newSize) {
        int originBias = dataSize;
        dataSize += newSize;
        return originBias;
    }

    // tag id generator
    public static int genTagId() {
        return tagCnt++;
    }
    // basic
    public static void putFunc(FuncFrame funcFrame) {
        funcTab.put(funcFrame.getIdent(), funcFrame);
    }

    public static void putSym(Symbol symbol) {
        globalSym.put(symbol.getIdent(), symbol);
    }

    public static String putString(String str) {
        String strLabel = "str_" + strCnt;
        globalStr.put(strLabel, str);
        ++strCnt;
        return strLabel;
    }

    public static void putAddr(String ident, int addr) {
        globalAddr.put(ident, addr);
    }

    public static FuncFrame getFunc(String funcName) {
        return funcTab.getOrDefault(funcName, null);
    }

    public static Symbol getGlobalSym(String symbolName) {
        return globalSym.getOrDefault(symbolName, null);
    }

    public static String getString(String label) {
        return globalStr.getOrDefault(label, null);
    }

    public int getAddr(String symbolName) {
        return globalAddr.getOrDefault(symbolName, null);
    }
}
