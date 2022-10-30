package ir;

import ast.CompUnitNode;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class IrRunner {
    private static List<String> globalSym = new ArrayList<>();

    public IrRunner(CompUnitNode compUnitNode) {
        MidCode midCode = new MidCode();
        compUnitNode.fillGlobalTab();
        outputAll(System.out);
    }

    public static void addOutput(String str) {
        globalSym.add(str);
    }

    public void outputAll(PrintStream ps) {
        globalSym.forEach(ps::println);
    }
}
