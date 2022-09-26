import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Compiler {

    public static void main(String[] args) throws FileNotFoundException {
        fileIO("testfile.txt", "output.txt");
    }

    public static void fileIO(String inputStr, String outputStr) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(inputStr);
        Scanner in = new Scanner(inputStream);
        PrintStream out = new PrintStream(outputStr);
        Lexer lexer = new Lexer(in, out);
        lexer.lex();
    }
}
