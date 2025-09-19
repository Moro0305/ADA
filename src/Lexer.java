// Archivo: Lexer.java

public abstract class Lexer {
    public static final char EOF = (char) -1;

    protected String input; // Ahora es protected
    protected int p = 0;    // Ahora es protected
    protected char c;       // Ahora es protected

    public Lexer(String input) {
        this.input = input;
        c = input.charAt(p);
    }

    public void consume() {
        p++;
        if (p >= input.length()) {
            c = EOF;
        } else {
            c = input.charAt(p);
        }
    }

    public abstract Token nextToken();
}