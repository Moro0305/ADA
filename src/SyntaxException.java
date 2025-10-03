/**
 * Exception thrown when a syntax error is detected during Ada parsing.
 * Used to signal and describe parsing errors with context information.
 */
public class SyntaxException extends Exception {
    /**
     * Constructs a SyntaxException with a detailed error message.
     * @param message Description of the syntax error
     */
    public SyntaxException(String message) {
        super(message);
    }
}