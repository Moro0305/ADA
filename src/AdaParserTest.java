/**
 * Clase de prueba para AdaParser.
 * Demuestra el análisis léxico y sintáctico de código Ada.
 * Muestra los tokens generados y valida la sintaxis con AdaParser.
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class AdaParserTest {
    /**
     * Método principal para ejecutar la prueba de AdaParser.
     * Carga código Ada desde un archivo o usa un ejemplo incorporado, lo tokeniza y lo parsea.
     * Imprime los tokens y reporta errores de sintaxis si se encuentran.
     * @param args Argumentos de línea de comandos (opcional: ruta al archivo Ada)
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
                System.err.println("Error al leer el archivo: " + e.getMessage());
                System.err.println("Se usará el código predefinido.");
                // If file reading fails, use the hardcoded Ada code
                codigoAda = getHardcodedAdaCode();
            }
        } else {
            // If no arguments, try to load TestComplete.ada by default
            String defaultFile = "src/TestComplete.ada";
            try {
                codigoAda = new String(Files.readAllBytes(Paths.get(defaultFile)));
                System.out.println("Archivo '" + defaultFile + "' cargado exitosamente.");
            } catch (IOException e) {
                System.out.println("No se encontró '" + defaultFile + "'. Se usará el código predefinido.");
                codigoAda = getHardcodedAdaCode();
            }
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
     * Devuelve un programa Ada embebido usado para pruebas.
     * @return Código Ada de ejemplo como cadena
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