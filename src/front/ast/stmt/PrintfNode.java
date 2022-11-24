package front.ast.stmt;

import exception.ErrorTable;
import exception.SysYError;
import front.ast.exp.ExpNode;
import front.lexical.Token;

import java.util.ArrayList;
import java.util.Iterator;

public class PrintfNode implements StmtNode {
    private final String formatStr;
    private final int printfLine;
    private final ArrayList<ExpNode> params;

    public PrintfNode(String formatStr, int printfLine) {
        this.formatStr = formatStr;
        this.printfLine = printfLine;
        this.params = new ArrayList<>();
    }

    public void checkParamCount() throws SysYError {
        int cnt = 0;
        int pos = 0;
        while (formatStr.indexOf("%d", pos) != -1) {
            pos = formatStr.indexOf("%d", pos);
            pos += 2;
            ++cnt;
        }
        if (cnt != params.size()) {
            throw new SysYError(SysYError.MISMATCHED_PRINTF, printfLine);
        }
    }

    // basic method
    public void addParam(ExpNode param) {
        params.add(param);
    }

    public String getFormatStr() {
        return formatStr.substring(1, formatStr.length() - 1);
    }

    public Iterator<ExpNode> iterParam() {
        return params.iterator();
    }
}
