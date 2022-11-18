import front.ast.CompUnitNode;
import back.MipsTranslator;
import mid.IrConverter;
import mid.MidCode;
import front.lexical.Lexer;
import front.Parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static util.Constant.*;

public class Compiler {
    public static void main(String[] args) throws FileNotFoundException {
        new SysYRunner();
    }
}
