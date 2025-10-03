/**
 * Represents a lexical token in Ada source code.
 * Stores the token type, text, and its position (line and column).
 */
public class Token {
    /** Type of the token (keyword, identifier, literal, operator, etc.) */
    public final TokenType type;
    /** Text content of the token */
    public final String text;
    /** Line number where the token appears */
    public final int line;
    /** Column number where the token appears */
    public final int column;

    /**
     * Constructs a Token with the given type, text, line, and column.
     * @param type TokenType of the token
     * @param text Text content of the token
     * @param line Line number
     * @param column Column number
     */
    public Token(TokenType type, String text, int line, int column) {
        this.type = type;
        this.text = text;
        this.line = line;
        this.column = column;
    }

    /**
     * Returns a string representation of the token for debugging.
     */
    @Override
    public String toString() {
        return "<'" + text + "', " + type.name() + ">";
    }
}