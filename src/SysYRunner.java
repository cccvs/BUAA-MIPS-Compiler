import back.MipsTranslator;
import exception.ErrorTable;
import exception.SysYError;
import front.Parser;
import front.ast.CompUnitNode;
import front.lexical.Lexer;
import mid.IrConverter;
import mid.MidCode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import static util.Constant.*;

public class SysYRunner {

    public SysYRunner() throws FileNotFoundException {
        run();
    }

    private void run() throws FileNotFoundException{
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
        // error part
        ErrorTable.outputError(new PrintStream(ERROR));
        // mips part
        MipsTranslator mipsTranslator = new MipsTranslator(midCode);
        mipsTranslator.outputMips(new PrintStream(MIPS));
    }

    private String readAll(Scanner in) {
        StringBuilder sb = new StringBuilder();
        // nextLine() doesn't include last '\n'
        while (in.hasNextLine()) {
            sb.append(in.nextLine());
            sb.append('\n');
        }
        return sb.toString();
    }
}
