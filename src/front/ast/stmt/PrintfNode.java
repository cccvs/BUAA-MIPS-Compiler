package front.ast.stmt;

import front.ast.exp.ExpNode;

import java.util.ArrayList;
import java.util.Iterator;

public class PrintfNode implements StmtNode {
    private String formatStr;
    private ArrayList<ExpNode> params;

    public PrintfNode(String formatStr) {
        this.formatStr = formatStr;
        this.params = new ArrayList<>();
    }

    public boolean checkParam() {
        int cnt = 0;
        int pos = 0;
        while (formatStr.indexOf("%d", pos) != -1) {
            pos = formatStr.indexOf("%d", pos);
            pos += 2;
            ++cnt;
        }
        return cnt == params.size();
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
