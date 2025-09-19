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
                "with Ada.Text_IO;         use Ada.Text_IO;\n" +
                        "with Ada.Integer_Text_IO; use Ada.Integer_Text_IO;\n" +
                        "procedure Ejemplo_Bucles is\n" +
                        "   -- Declaración de variables\n" +
                        "   Contador     : Integer := 10;\n" +
                        "   Encontrado   : Boolean := False;\n" +
                        "   Numero_Buscado: Integer := 3;\n" +
                        "begin\n" +
                        "   -- Bucle FOR: Cuenta regresiva de 10 a 1\n" +
                        "   Put_Line(\"--- Bucle FOR: Cuenta regresiva ---\");\n" +
                        "   for I in reverse 1..10 loop\n" +
                        "      Put(I);\n" +
                        "      Put(\" \");\n" +
                        "   end loop;\n" +
                        "   New_Line;\n" +
                        "   ---\n" +
                        "   -- Bucle WHILE: Busca un número\n" +
                        "   Put_Line(\"--- Bucle WHILE: Buscando el numero 3 ---\");\n" +
                        "   while (Contador >= 1) and (not Encontrado) loop\n" +
                        "      Put(\"Buscando en: \");\n" +
                        "      Put(Contador);\n" +
                        "      New_Line;\n" +
                        "      if Contador = Numero_Buscado then\n" +
                        "         Encontrado := True;\n" +
                        "      end if;\n" +
                        "      Contador := Contador - 1;\n" +
                        "   end loop;\n" +
                        "   if Encontrado then\n" +
                        "      Put_Line(\"¡Número encontrado!\");\n" +
                        "   else\n" +
                        "      Put_Line(\"Número no encontrado.\");\n" +
                        "   end if;\n" +
                        "   ---\n" +
                        "   -- Bucle LOOP: Bucle simple que se ejecuta hasta que la condición se cumple\n" +
                        "   Put_Line(\"--- Bucle LOOP: Tarea simple ---\");\n" +
                        "   Contador := 1;\n" +
                        "   loop\n" +
                        "      Put(\"Iteracion: \");\n" +
                        "      Put(Contador);\n" +
                        "      New_Line;\n" +
                        "      exit when (Contador = 5) or (Encontrado = True);\n" +
                        "      Contador := Contador + 1;\n" +
                        "   end loop;\n" +
                        "   Put_Line(\"Fin del programa.\");\n" +
                        "end Ejemplo_Bucles;";
    }
}