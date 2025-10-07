/**
 * Test class for AdaParser.
 * Demonstrates lexical and syntactic analysis of Ada source code.
 * Prints generated tokens and validates syntax using AdaParser.
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class AdaParserTest {
    /**
     * Main method to run the AdaParser test.
     * Loads Ada code from a file or uses a hardcoded example, tokenizes it, and parses it.
     * Prints all tokens and reports syntax errors if found.
     * @param args Command-line arguments (optional: path to Ada source file)
     */
    public static void main(String[] args) {
        String codigoAda;

        // Check if a file path was provided as a command-line argument
        if (args.length > 0) {
            String filePath = args[0];
            try {
                // Try to read the file contents into a string
                codigoAda = new String(Files.readAllBytes(Paths.get(filePath)));
                System.out.println("Archivo '" + filePath + "' cargado exitosamente.");
            } catch (IOException e) {
                System.err.println("Error al leer el archivo. Se usará el código predefinido.");
                // If file reading fails, use the hardcoded Ada code
                codigoAda = getHardcodedAdaCode();
            }
        } else {
            // If no arguments, use the hardcoded Ada code
            System.out.println("No se especificó un archivo. Se usará el código predefinido.");
            codigoAda = getHardcodedAdaCode();
        }

        // Lexical Analysis Phase (Lexer)
        AdaLexer lexer = new AdaLexer(codigoAda);
        ArrayList<Token> tokens = new ArrayList<>();
        Token t = lexer.nextToken();
        while (t.type != TokenType.EOF) {
            tokens.add(t);
            t = lexer.nextToken();
        }
        tokens.add(t); // Add the final EOF token

        System.out.println("Tokens generados por el Lexer: ");
        for (Token token : tokens) {
            System.out.println(token);
        }

        System.out.println("\n---");

        // Syntactic Analysis Phase (Parser)
        AdaParser parser = new AdaParser(tokens);
        try {
            parser.analizar();
        } catch (SyntaxException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Returns a hardcoded Ada program for testing purposes.
     * @return Example Ada source code as a string
     */
    private static String getHardcodedAdaCode() {
        return
                "with Ada.Text_IO; use Ada.Text_IO;\n" +
                "procedure Main is\n" +
                "   -- Nested symbol table example\n" +
                "   function Add_Numbers(A, B : Integer) return Integer is\n" +
                "   begin\n" +
                "      return A + B;\n" +
                "   end Add_Numbers;\n" +
                "\n" +
                "   Result : Integer;\n" +
                "begin\n" +
                "   Result := Add_Numbers(5, 10);\n" +
                "   Put_Line(\"The result is: \" & Integer'Image (Result));\n" +
                "end Main;\n";
    }
}