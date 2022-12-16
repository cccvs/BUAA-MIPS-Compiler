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
        lexer.outputTokens(new PrintStream(LEXER), OUTPUT_LEXER);
        // parse part
        Parser parser = new Parser(lexer);
        CompUnitNode compUnit = parser.parseCompUnit();
        parser.outputSyntax(new PrintStream(SYNTAX), OUTPUT_SYNTAX);
        // ir/error part
        IrConverter irConverter = new IrConverter(compUnit);
        ErrorTable.outputError(new PrintStream(ERROR), OUTPUT_ERROR);
        ErrorTable.throwError();    // terminate if error
        // middle code
        MidCode midCode = irConverter.getMidCode();
        // --- optimize
        Optimizer optimizer  = new Optimizer(midCode);
        optimizer.run();
        optimizer.outputRegInfo(new PrintStream("interval_info.txt"));
        // ---
        // mid code out
        midCode.outputMidCode(new PrintStream(MID_CODE), OUTPUT_MID_CODE);
        // mips part
        MipsTranslator mipsTranslator = new MipsTranslator(midCode);
        mipsTranslator.outputMips(new PrintStream(MIPS), OUTPUT_MIPS);
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
