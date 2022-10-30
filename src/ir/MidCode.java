package ir;

import ast.CompUnitNode;
import ast.decl.DefNode;
import ast.func.FuncDefNode;

import java.util.HashMap;

public class MidCode {
    private static HashMap<String, FuncDefNode> funcTab;
    private static HashMap<String, DefNode> globalSymbol;
    private static HashMap<String, String> globalStr;
    private static HashMap<String, Integer> globalAddr;

    // symbol table of main func
    private SymTab rootTab = null;
    private FuncDefNode mainFunc = null;

    // string id generator
    private static int strCnt = 0;

    public MidCode() {
        funcTab = new HashMap<>();
        globalSymbol = new HashMap<>();
        globalStr = new HashMap<>();
        globalAddr = new HashMap<>();
    }

    // basic
    public static void putFunc(FuncDefNode funcDefNode) {
        funcTab.put(funcDefNode.getIdent(), funcDefNode);
    }

    public static void putSym(DefNode defNode) {
        globalSymbol.put(defNode.getIdent(), defNode);
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

    public static FuncDefNode getFunc(String funcName) {
        return funcTab.getOrDefault(funcName, null);
    }

    public static DefNode getSymbol(String symbolName) {
        return globalSymbol.getOrDefault(symbolName, null);
    }

    public static String getString(String label) {
        return globalStr.getOrDefault(label, null);
    }

    public int getAddr(String symbolName) {
        return globalAddr.getOrDefault(symbolName, null);
    }
}
