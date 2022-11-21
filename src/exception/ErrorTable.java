package exception;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ErrorTable {
    private static final List<SysYError> errorList = new ArrayList<>();

    public static void outputError(PrintStream ps) {
        errorList.sort(Comparator.naturalOrder());
        for (SysYError error : errorList) {
            ps.println(error);
        }
    }

    public static void appendError(SysYError error) {
        errorList.add(error);
    }
}
