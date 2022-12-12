package exception;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class ErrorTable {
    private static final List<SysYError> errorList = new ArrayList<>();
    private static final Stack<Integer> posStack = new Stack<>();

    public static void outputError(PrintStream ps, boolean execute) {
        if (!execute) {
            return;
        }
        errorList.sort(Comparator.naturalOrder());
        for (SysYError error : errorList) {
            ps.println(error);
        }
    }

    public static void checkpoint() {
        posStack.push(errorList.size());
    }

    public static void retrieve() {
        int prePos = posStack.pop();
        if (errorList.size() > prePos) {
            errorList.subList(prePos, errorList.size()).clear();
        }
    }

    public static void append(SysYError error) {
        errorList.add(error);
    }

    public static void throwError() throws SysYError {
        if (!errorList.isEmpty()) {
            throw errorList.get(0);
        }
    }
}
