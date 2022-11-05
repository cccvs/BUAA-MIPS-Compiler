package ir.operand;

public interface Operand {
    public enum RefType {
        VALUE,      // 全局变量, 局部变量
        POINTER,    // 函数数组形参
        ARRAY       // 全局数组, 局部数组
    }
    public RefType getRefType();
    public Integer getId();
    public Integer getOffset();
}
