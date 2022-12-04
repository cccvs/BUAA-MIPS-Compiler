package mid.frame;

import mid.code.BasicIns;
import mid.code.Branch;
import mid.code.Jump;
import mid.code.Return;
import mid.operand.Symbol;
import front.TkType;

import java.util.*;

public class FuncFrame {
    /*
     *  Originate from FuncDefNode.
     *  Can't extend BasicBlock. Otherwise, it's hard to distinguish real params.
     */
    public enum RetType {
        INT, VOID
    }

    // basic information
    private final String ident;
    private final RetType retType;
    private final List<Symbol> params;
    private final List<BasicBlock> bodyBlocks;
    private int stackSize = 0;

    // optimize
    private final Set<BasicBlock> endBlocks = new HashSet<>();

    public FuncFrame(String ident, TkType tkType) {
        this.ident = ident;
        this.retType = tkType.equals(TkType.VOIDTK) ? RetType.VOID : RetType.INT;
        this.params = new ArrayList<>();
        this.bodyBlocks = new ArrayList<>();
    }

    public int addStackSize(int size) {
        stackSize += size;
        return stackSize;
    }

    // basic function
    public String getIdent() {
        return ident;
    }

    public Iterator<BasicBlock> iterBody() {
        return bodyBlocks.iterator();
    }

    public RetType getRetType() {
        return retType;
    }

    public Iterator<Symbol> iterFormatParam() {
        return params.iterator();
    }

    public int formatParamNum() {
        return params.size();
    }

    public void addParam(Symbol param) {
        params.add(param);
    }

    public boolean hasParamName(String ident) {
        for (Symbol param : params) {
            if (Objects.equals(param.getIdent(), ident)) {
                return true;
            }
        }
        return false;
    }

    public void appendBlock(BasicBlock block) {
        this.bodyBlocks.add(block);
    }

    // TODO[13]: abandoned in the future
    public String getLabel() {
        return "f_" + ident;
    }

    // optimize
    public void buildFlowGraph() {
        for (int i = 0; i < bodyBlocks.size(); i++) {
            BasicBlock block = bodyBlocks.get(i);
            block.clearReturnFollows();
            BasicIns lastIns = block.getLastIns();
            if (lastIns instanceof Jump) {
                block.linkNext(((Jump) lastIns).getTargetBlock());
            } else if (lastIns instanceof Branch) {
                block.linkNext(((Branch) lastIns).getBlockTrue());
                block.linkNext(((Branch) lastIns).getBlockFalse());
            } else if (lastIns instanceof Return) {
                endBlocks.add(block);
            } else {
                if (i + 1 < bodyBlocks.size()) {
                    block.linkNext(bodyBlocks.get(i + 1));
                } else {
                    endBlocks.add(block);
                }
            }
        }
    }

    public void livenessAnalysis() {

    }

    @Override
    public String toString() {
        // System.out.println(getLabel());
        return "# Function " + ident + "[stack size: 0x" +
                Integer.toHexString(stackSize) + "]\n" +
                getLabel() + ":\n" +
                bodyBlocks.stream().map(BasicBlock::toString).reduce((x, y) -> x + y).orElse("");
    }
}
