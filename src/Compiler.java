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
        fileIO();
    }

    public static void fileIO() throws FileNotFoundException {
        // read part
        InputStream inputStream = new FileInputStream(INPUT_FILE);
        Scanner in = new Scanner(inputStream);
        String inputStr = readAll(in);
        // front.lexical part
        Lexer lexer = new Lexer(inputStr);
        lexer.lex();
        lexer.outputTokens(new PrintStream(LEXER));
        // parse part
        Parser parser = new Parser(lexer);
        CompUnitNode compUnit = parser.parseCompUnit();
        parser.outputSyntax(new PrintStream(SYNTAX));
        // intermediate part
        IrConverter irConverter = new IrConverter(compUnit);
        MidCode midCode = irConverter.getMidCode();
        midCode.outputMidCode(new PrintStream(MID_CODE));
        // mips part
        MipsTranslator mipsTranslator = new MipsTranslator(midCode);
        mipsTranslator.outputMips(new PrintStream(MIPS));
    }

    public static String readAll(Scanner in) {
        StringBuilder sb = new StringBuilder();
        // nextLine() doesn't include last '\n'
        while (in.hasNextLine()) {
            sb.append(in.nextLine());
            sb.append('\n');
        }
        return sb.toString();
    }
}
