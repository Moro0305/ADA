/**
 * Representa un token léxico en código Ada.
 * Contiene el tipo, texto y la posición (línea y columna).
 */
public class Token {
    /** Tipo del token (palabra reservada, identificador, literal, operador, etc.) */
    public final TokenType type;
    /** Texto del token */
    public final String text;
    /** Línea donde aparece el token */
    public final int line;
    /** Columna donde aparece el token */
    public final int column;

    /**
     * Construye un Token con tipo, texto, línea y columna.
     * @param type Tipo del token
     * @param text Texto del token
     * @param line Línea donde aparece
     * @param column Columna donde aparece
     */
    public Token(TokenType type, String text, int line, int column) {
        this.type = type;
        this.text = text;
        this.line = line;
        this.column = column;
    }

    /**
     * Representación en cadena para depuración.
     */
    @Override
    public String toString() {
        return "<'" + text + "', " + type.name() + ">";
    }
}