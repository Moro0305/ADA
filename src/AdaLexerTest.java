/**
 * Test class for AdaLexer.
 * Demonstrates tokenization of Ada source code and prints each token.
 */
public class AdaLexerTest {
    /**
     * Main method to run the AdaLexer test.
     * Tokenizes a sample Ada program and prints all tokens to the console.
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        String codigoAda =
                "with Ada.Text_IO;\n" +
                        "procedure Hello_World is\n" +
                        "   Message : constant String := \"Hello, World!\";\n" +
                        "   Numero : Integer := 10_000;\n" +
                        "   Contador : Integer := 3;\n" +
                        "begin\n" +
                        "   Ada.Text_IO.Put_Line (Message);\n" +
                        "   -- Ejemplo de bucle For\n" +
                        "   Ada.Text_IO.Put_Line (\"--- Bucle For ---\");\n" +
                        "   for I in 1..5 loop\n" +
                        "      Ada.Text_IO.Put (\"Iteracion: \" );\n" +
                        "      Ada.Text_IO.Put_Line (Integer'Image(I));\n" +
                        "   end loop;\n" +
                        "   -- Ejemplo de bucle While\n" +
                        "   Ada.Text_IO.Put_Line (\"--- Bucle While ---\");\n" +
                        "   while Contador > 0 loop\n" +
                        "      Ada.Text_IO.Put_Line (\"Cuenta regresiva: \" & Integer'Image(Contador));\n" +
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

        AdaLexer lexer = new AdaLexer(codigoAda);
        Token t = lexer.nextToken();
        while (t.type != TokenType.EOF) {
            System.out.println(t); // Imprime cada token
            t = lexer.nextToken();
        }
        System.out.println(t); // Imprime el token EOF
    }
}