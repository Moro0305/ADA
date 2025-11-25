/**
 * Clase base abstracta para analizadores léxicos.
 * Proporciona manejo básico de entrada y consumo de caracteres.
 */
public abstract class Lexer {
    /** Carácter que representa fin de archivo */
    public static final char EOF = (char) -1;
    /** Cadena de entrada a tokenizar */
    protected String input;
    /** Posición actual en la cadena de entrada */
    protected int p = 0;
    /** Carácter actual en análisis */
    protected char c;

    /**
     * Construye un Lexer con la cadena de entrada dada.
     * @param input Código fuente a tokenizar
     */
    public Lexer(String input) {
        this.input = input;
        c = input.charAt(p);
    }

    /**
     * Consume el carácter actual y avanza la posición.
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
     * Retorna el siguiente token de la entrada.
     * Debe ser implementado por subclases.
     * @return Siguiente Token
     */
    public abstract Token nextToken();
}