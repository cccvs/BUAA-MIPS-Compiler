package ir;

import ir.frame.FuncFrame;

import java.util.HashMap;

public class MidCode {
    private static HashMap<String, FuncFrame> funcTab;
    private static HashMap<String, String> globalStr;
    private static HashMap<String, Integer> globalAddr;
    private static FuncFrame mainFunc = null;

    // generator
    private static int strCnt = 0;
    private static int IdCnt = 0;

    public MidCode() {
        funcTab = new HashMap<>();
        globalStr = new HashMap<>();
        globalAddr = new HashMap<>();
    }

    // tag id generator
    public static int genId() {
        return IdCnt++;
    }

    // basic
    public static void putFunc(FuncFrame funcFrame) {
        funcTab.put(funcFrame.getIdent(), funcFrame);
    }

    public static void putMainFunc(FuncFrame func) {
        mainFunc = func;
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

    public static FuncFrame getMainFunc() {
        return mainFunc;
    }

    public static String getString(String label) {
        return globalStr.getOrDefault(label, null);
    }

    public int getAddr(String symbolName) {
        return globalAddr.getOrDefault(symbolName, null);
    }
}
