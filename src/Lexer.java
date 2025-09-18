public abstract class Lexer {
    public static final char EOF = (char) -1; // Representa el final del archivo

    String input;
    int p = 0;
    char c;

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