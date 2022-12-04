import back.MipsTranslator;
import exception.ErrorTable;
import exception.LexerError;
import exception.ParserError;
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
import java.util.Scanner;

public class SysYRunner {
    // io file
    public static final String INPUT_FILE = "testfile.txt";
    public static final String LEXER = "tokens.txt";
    public static final String SYNTAX = "output.txt";
    public static final String MID_CODE = "midcode.txt";
    public static final String ERROR = "error.txt";
    public static final String MIPS = "mips.txt";
    // boolean
    public static final boolean OUTPUT_LEXER = false;
    public static final boolean OUTPUT_SYNTAX = false;
    public static final boolean OUTPUT_MID_CODE = true;
    public static final boolean OUTPUT_ERROR = true;
    public static final boolean OUTPUT_MIPS = true;
    public static final boolean OPTIMIZE = true;

    public SysYRunner() throws FileNotFoundException {
        try {
            run();
        } catch (LexerError | ParserError error) {
            error.printStackTrace();
        } catch (SysYError ignored) {
            // do nothing, just terminate
        }
    }

    private void run() throws FileNotFoundException, LexerError, ParserError, SysYError {
        // read part
        InputStream inputStream = new FileInputStream(INPUT_FILE);
        Scanner in = new Scanner(inputStream);
        String inputStr = readAll(in);
        // front.lexical part
        Lexer lexer = new Lexer(inputStr);
        lexer.lex();
        if (OUTPUT_LEXER) {
            lexer.outputTokens(new PrintStream(LEXER));
        }
        // parse part
        Parser parser = new Parser(lexer);
        CompUnitNode compUnit = parser.parseCompUnit();
        if (OUTPUT_SYNTAX) {
            parser.outputSyntax(new PrintStream(SYNTAX));
        }
        // ir/error part
        IrConverter irConverter = new IrConverter(compUnit);
        if (OUTPUT_ERROR) {
            ErrorTable.outputError(new PrintStream(ERROR));
        }
        ErrorTable.throwError();    // terminate if error
        // middle code output
        MidCode midCode = irConverter.getMidCode();
        if (OUTPUT_MID_CODE) {
            midCode.outputMidCode(new PrintStream(MID_CODE));
        }
        // mips part
        MipsTranslator mipsTranslator = new MipsTranslator(midCode);
        if (OUTPUT_MIPS) {
            mipsTranslator.outputMips(new PrintStream(MIPS));
        }
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
