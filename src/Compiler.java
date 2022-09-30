import lexical.Lexer;
import syntax.Parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static util.Constant.INPUT_FILE;

public class Compiler {
    public static void main(String[] args) throws FileNotFoundException {
        fileIO();
    }

    public static void fileIO() throws FileNotFoundException {
        // read part
        InputStream inputStream = new FileInputStream(INPUT_FILE);
        Scanner in = new Scanner(inputStream);
        String inputStr = readAll(in);
        // lexical part
        Lexer lexer = new Lexer(inputStr);
        lexer.lex();
        // parse part
        Parser parser = new Parser(lexer);
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
