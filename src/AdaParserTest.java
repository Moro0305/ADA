import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class AdaParserTest {
    public static void main(String[] args) {
        String codigoAda;

        // Verificar si se proporcionó una ruta de archivo en los argumentos de la línea de comandos
        if (args.length > 0) {
            String filePath = args[0];
            try {
                // Intentar leer el contenido del archivo en una cadena
                codigoAda = new String(Files.readAllBytes(Paths.get(filePath)));
                System.out.println("Archivo '" + filePath + "' cargado exitosamente.");
            } catch (IOException e) {
                System.err.println("Error al leer el archivo. Se usará el código predefinido.");
                // Si la lectura del archivo falla, usar el código predefinido
                codigoAda = getHardcodedAdaCode();
            }
        } else {
            // Si no se proporcionan argumentos, usar el código predefinido
            System.out.println("No se especificó un archivo. Se usará el código predefinido.");
            codigoAda = getHardcodedAdaCode();
        }

        // Fase de Análisis Léxico (Lexer)
        AdaLexer lexer = new AdaLexer(codigoAda);
        ArrayList<Token> tokens = new ArrayList<>();
        Token t = lexer.nextToken();
        while (t.type != TokenType.EOF) {
            tokens.add(t);
            t = lexer.nextToken();
        }
        tokens.add(t); // Agregar el token EOF final

        System.out.println("Tokens generados por el Lexer: ");
        for (Token token : tokens) {
            System.out.println(token);
        }

        System.out.println("\n---");

        // Fase de Análisis Sintáctico (Parser)
        AdaParser parser = new AdaParser(tokens);
        try {
            parser.analizar();
        } catch (SyntaxException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String getHardcodedAdaCode() {
        return
                "with Ada.Text_IO;\n" +
                        "with Ada.Integer_Text_IO;\n" +
                        "\n" +
                        "procedure Ejemplo_Operacion is\n" +
                        "   -- Declaración de variables\n" +
                        "   Numero_Uno   : Integer := 10;\n" +
                        "   Numero_Dos   : Integer := 5;\n" +
                        "   Suma_Total   : Integer;\n" +
                        "\n" +
                        "begin\n" +
                        "   -- Operación de suma\n" +
                        "   Suma_Total := Numero_Uno + Numero_Dos;\n" +
                        "\n" +
                        "   -- Impresión del resultado\n" +
                        "   Ada.Text_IO.Put(\"La suma es: \");\n" +
                        "   Ada.Integer_Text_IO.Put(Suma_Total);\n" +
                        "   Ada.Text_IO.New_Line;\n" +
                        "\n" +
                        "end Ejemplo_Operacion;";
    }
}