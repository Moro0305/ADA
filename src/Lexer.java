/**
 * Abstract base class for lexical analyzers (lexers).
 * Provides basic input handling and character consumption logic.
 * Subclasses must implement nextToken() to return the next token from input.
 */
public abstract class Lexer {
    /** End-of-file character constant */
    public static final char EOF = (char) -1;
    /** Input string to tokenize */
    protected String input;
    /** Current position in the input string */
    protected int p = 0;
    /** Current character being analyzed */
    protected char c;

    /**
     * Constructs a Lexer with the given input string.
     * Initializes the current character and position.
     * @param input Source code to tokenize
     */
    public Lexer(String input) {
        this.input = input;
        c = input.charAt(p);
    }

    /**
     * Consumes the current character and advances the position.
     */
    public void consume() {
        p++;
        if (p >= input.length()) {
            c = EOF;
        } else {
            c = input.charAt(p);
        }
    }

    /**
     * Returns the next token from the input.
     * Must be implemented by subclasses.
     * @return The next Token object
     */
    public abstract Token nextToken();
}