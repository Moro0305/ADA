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
        return "with Ada.Text_IO;\n" +
                "procedure Hello_World is\n" +
                "   Message : constant String := \"Hello, World!\";\n" +
                "   Numero : Integer := 10_000;\n" +
                "   Contador : Integer := 3;\n" +
                "begin\n" +
                "   Ada.Text_IO.Put_Line (Message);\n" +
                "   -- Ejemplo de bucle For\n" +
                "   Ada.Text_IO.Put_Line (\"--- Bucle For ---\");\n" +
                "   for I in 1..5 loop\n" +
                "      Ada.Text_IO.Put (\"Iteracion: \");\n" +
                "      -- Se ha modificado esta linea para evitar el atributo 'Image\n" +
                "      Ada.Text_IO.Put_Line (\"\" ); -- Imprimimos una cadena vacia en su lugar\n" +
                "   end loop;\n" +
                "   -- Ejemplo de bucle While\n" +
                "   Ada.Text_IO.Put_Line (\"--- Bucle While ---\");\n" +
                "   while Contador > 0 loop\n" +
                "      -- Se ha modificado esta linea para evitar la concatenacion y el atributo 'Image\n" +
                "      Ada.Text_IO.Put_Line (\"Cuenta regresiva: \");\n" +
                "      Contador := Contador - 1;\n" +
                "   end loop;\n" +
                "   -- Ejemplo de bucle Loop\n" +
                "   Ada.Text_IO.Put_Line (\"--- Bucle Loop ---\");\n" +
                "   loop\n" +
                "      Ada.Text_IO.Put_Line (\"Dentro del bucle sin condicion\");\n" +
                "      exit when (25 > 10);\n" +
                "   end loop;\n" +
                "   -- Este es un comentario\n" +
                "   if (25 > 10) then\n" +
                "      null;\n" +
                "   end if;\n" +
                "end Hello_World;";
    }
}