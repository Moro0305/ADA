/**
 * Analizador léxico (lexer) para código Ada.
 * Tokeniza la entrada y ofrece utilidades específicas para el análisis sintáctico.
 */
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class AdaLexer extends Lexer {
    /** Cadena de entrada a tokenizar */
    protected String input;
    /** Posición actual en la entrada */
    protected int position = 0;
    /** Carácter actual en análisis */
    protected char c;
    /** Constante que representa fin de archivo */
    protected final char EOF = (char)-1;
    /** Número de línea actual para reportes */
    protected int line = 1;
    /** Número de columna actual para reportes */
    protected int column = 1;

    /**
     * Construye un AdaLexer con la entrada dada.
     * @param input Código Ada a tokenizar
     */
    public AdaLexer(String input) {
        super(input);
        this.input = input;
        if (input.length() > 0) {
            this.c = input.charAt(0);
        } else {
            this.c = EOF;
        }
    }

    /** Devuelve true si el carácter actual es una letra. */
    private boolean isLetter() {
        return Character.isLetter(c);
    }

    /** Devuelve true si el carácter actual es un dígito. */
    private boolean isDigit() {
        return Character.isDigit(c);
    }

    /** Devuelve true si el carácter actual es un espacio en blanco. */
    private boolean isWhitespace() {
        return Character.isWhitespace(c);
    }

    /** Devuelve el siguiente carácter sin consumirlo. */
    private char peek() {
        if (position + 1 >= input.length()) {
            return EOF;
        }
        return input.charAt(position + 1);
    }

    /** Devuelve true si se alcanzó el final de la entrada. */
    private boolean isEOF() {
        return position >= input.length();
    }

    /** Consume caracteres de espacio en blanco. */
    private void ws() {
        while (isWhitespace()) {
            consume();
        }
    }

    /** Consume Ada-style comments (starting with --). */
    private void comment() {
        if (c == '-') {
            consume();
            if (c == '-') {
                while (c != '\n' && c != '\r' && c != EOF) {
                    consume();
                }
            }
        }
    }

    /**
     * Identifies reserved words or identifiers in the Ada source code.
     * Consumes characters until a delimiter is found.
     * Checks the consumed characters against known Ada reserved words.
     * @return Token representing the reserved word or identifier
     */
    private Token identifierOrReservedWord() {
        int startLine = line;
        int startColumn = column;
        StringBuilder buf = new StringBuilder();
        do {
            buf.append(c);
            consume();
        } while (isLetter() || isDigit() || c == '_');

        String text = buf.toString();

        switch (text.toLowerCase()) {
            case "abort": return new Token(TokenType.ABORT_KW, text, startLine, startColumn);
            case "abs": return new Token(TokenType.ABS_KW, text, startLine, startColumn);
            case "abstract": return new Token(TokenType.ABSTRACT_KW, text, startLine, startColumn);
            case "accept": return new Token(TokenType.ACCEPT_KW, text, startLine, startColumn);
            case "access": return new Token(TokenType.ACCESS_KW, text, startLine, startColumn);
            case "aliased": return new Token(TokenType.ALIASED_KW, text, startLine, startColumn);
            case "all": return new Token(TokenType.ALL_KW, text, startLine, startColumn);
            case "and": return new Token(TokenType.AND_KW, text, startLine, startColumn);
            case "array": return new Token(TokenType.ARRAY_KW, text, startLine, startColumn);
            case "at": return new Token(TokenType.AT_KW, text, startLine, startColumn);
            case "begin": return new Token(TokenType.BEGIN_KW, text, startLine, startColumn);
            case "body": return new Token(TokenType.BODY_KW, text, startLine, startColumn);
            case "case": return new Token(TokenType.CASE_KW, text, startLine, startColumn);
            case "constant": return new Token(TokenType.CONSTANT_KW, text, startLine, startColumn);
            case "declare": return new Token(TokenType.DECLARE_KW, text, startLine, startColumn);
            case "delay": return new Token(TokenType.DELAY_KW, text, startLine, startColumn);
            case "delta": return new Token(TokenType.DELTA_KW, text, startLine, startColumn);
            case "digits": return new Token(TokenType.DIGITS_KW, text, startLine, startColumn);
            case "do": return new Token(TokenType.DO_KW, text, startLine, startColumn);
            case "else": return new Token(TokenType.ELSE_KW, text, startLine, startColumn);
            case "elsif": return new Token(TokenType.ELSIF_KW, text, startLine, startColumn);
            case "end": return new Token(TokenType.END_KW, text, startLine, startColumn);
            case "entry": return new Token(TokenType.ENTRY_KW, text, startLine, startColumn);
            case "exception": return new Token(TokenType.EXCEPTION_KW, text, startLine, startColumn);
            case "exit": return new Token(TokenType.EXIT_KW, text, startLine, startColumn);
            case "for": return new Token(TokenType.FOR_KW, text, startLine, startColumn);
            case "function": return new Token(TokenType.FUNCTION_KW, text, startLine, startColumn);
            case "generic": return new Token(TokenType.GENERIC_KW, text, startLine, startColumn);
            case "goto": return new Token(TokenType.GOTO_KW, text, startLine, startColumn);
            case "if": return new Token(TokenType.IF_KW, text, startLine, startColumn);
            case "in": return new Token(TokenType.IN_KW, text, startLine, startColumn);
            case "interface": return new Token(TokenType.INTERFACE_KW, text, startLine, startColumn);
            case "is": return new Token(TokenType.IS_KW, text, startLine, startColumn);
            case "limited": return new Token(TokenType.LIMITED_KW, text, startLine, startColumn);
            case "loop": return new Token(TokenType.LOOP_KW, text, startLine, startColumn);
            case "mod": return new Token(TokenType.MOD_KW, text, startLine, startColumn);
            case "new": return new Token(TokenType.NEW_KW, text, startLine, startColumn);
            case "not": return new Token(TokenType.NOT_KW, text, startLine, startColumn);
            case "null": return new Token(TokenType.NULL_KW, text, startLine, startColumn);
            case "of": return new Token(TokenType.OF_KW, text, startLine, startColumn);
            case "or": return new Token(TokenType.OR_KW, text, startLine, startColumn);
            case "others": return new Token(TokenType.OTHERS_KW, text, startLine, startColumn);
            case "out": return new Token(TokenType.OUT_KW, text, startLine, startColumn);
            case "package": return new Token(TokenType.PACKAGE_KW, text, startLine, startColumn);
            case "pragma": return new Token(TokenType.PRAGMA_KW, text, startLine, startColumn);
            case "private": return new Token(TokenType.PRIVATE_KW, text, startLine, startColumn);
            case "procedure": return new Token(TokenType.PROCEDURE_KW, text, startLine, startColumn);
            case "protected": return new Token(TokenType.PROTECTED_KW, text, startLine, startColumn);
            case "raise": return new Token(TokenType.RAISE_KW, text, startLine, startColumn);
            case "range": return new Token(TokenType.RANGE_KW, text, startLine, startColumn);
            case "record": return new Token(TokenType.RECORD_KW, text, startLine, startColumn);
            case "rem": return new Token(TokenType.REM_KW, text, startLine, startColumn);
            case "renames": return new Token(TokenType.RENAMES_KW, text, startLine, startColumn);
            case "requeue": return new Token(TokenType.REQUEUE_KW, text, startLine, startColumn);
            case "return": return new Token(TokenType.RETURN_KW, text, startLine, startColumn);
            case "reverse": return new Token(TokenType.REVERSE_KW, text, startLine, startColumn);
            case "select": return new Token(TokenType.SELECT_KW, text, startLine, startColumn);
            case "separate": return new Token(TokenType.SEPARATE_KW, text, startLine, startColumn);
            case "some": return new Token(TokenType.SOME_KW, text, startLine, startColumn);
            case "subtype": return new Token(TokenType.SUBTYPE_KW, text, startLine, startColumn);
            case "tagged": return new Token(TokenType.TAGGED_KW, text, startLine, startColumn);
            case "task": return new Token(TokenType.TASK_KW, text, startLine, startColumn);
            case "terminate": return new Token(TokenType.TERMINATE_KW, text, startLine, startColumn);
            case "then": return new Token(TokenType.THEN_KW, text, startLine, startColumn);
            case "type": return new Token(TokenType.TYPE_KW, text, startLine, startColumn);
            case "until": return new Token(TokenType.UNTIL_KW, text, startLine, startColumn);
            case "use": return new Token(TokenType.USE_KW, text, startLine, startColumn);
            case "when": return new Token(TokenType.WHEN_KW, text, startLine, startColumn);
            case "while": return new Token(TokenType.WHILE_KW, text, startLine, startColumn);
            case "with": return new Token(TokenType.WITH_KW, text, startLine, startColumn);
            case "xor": return new Token(TokenType.XOR_KW, text, startLine, startColumn);
            default:
                return new Token(TokenType.IDENTIFIER, text, startLine, startColumn);
        }
    }

    /**
     * Parses number literals (integers and reals) in the Ada source code.
     * Consumes digits, optional decimal points, and exponent parts.
     * @return Token representing the number literal
     */
    private Token numberLiteral() {
        int startLine = line;
        int startColumn = column;
        StringBuilder buf = new StringBuilder();
        boolean hasDecimal = false;

        while (isDigit() || c == '_') {
            if (c == '_') {
                consume();
                continue;
            }
            buf.append(c);
            consume();
        }

        if (c == '.' && peek() != '.') {
            hasDecimal = true;
            buf.append(c);
            consume();

            while (isDigit() || c == '_') {
                if (c == '_') {
                    consume();
                    continue;
                }
                buf.append(c);
                consume();
            }
        }

        if (c == 'e' || c == 'E') {
            buf.append(c);
            consume();
            if (c == '+' || c == '-') {
                buf.append(c);
                consume();
            }
            while (isDigit() || c == '_') {
                if (c == '_') {
                    consume();
                    continue;
                }
                buf.append(c);
                consume();
            }
        }

        String text = buf.toString().replace("_", "");

        if (hasDecimal || text.contains("e") || text.contains("E")) {
            return new Token(TokenType.REAL_LITERAL, text, startLine, startColumn);
        } else {
            return new Token(TokenType.INTEGER_LITERAL, text, startLine, startColumn);
        }
    }

    /**
     * Parses string literals in the Ada source code.
     * Consumes characters until a closing quote is found.
     * @return Token representing the string literal
     */
    private Token stringLiteral() {
        int startLine = line;
        int startColumn = column;
        StringBuilder buf = new StringBuilder();
        consume();

        while (c != '"' && c != EOF) {
            buf.append(c);
            consume();
        }

        if (c == EOF) {
            throw new RuntimeException("Cadena no cerrada al final del archivo");
        }

        consume();
        return new Token(TokenType.STRING_LITERAL, buf.toString(), startLine, startColumn);
    }

    /**
     * Consume el carácter actual y actualiza línea y columna.
     * Mantiene la lógica específica de AdaLexer para seguimiento de posición.
     */
    public void consume() {
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        position++;
        if (position >= input.length()) {
            c = EOF;
        } else {
            c = input.charAt(position);
        }
    }

    /**
     * Retrieves the next token from the input.
     * Skips whitespace and comments, and identifies tokens for keywords,
     * identifiers, literals, and operators.
     * @return The next token in the input
     */
    @Override
    public Token nextToken() {
        while (c != EOF) {
            if (isWhitespace()) {
                ws();
                continue;
            }
            if (c == '-' && peek() == '-') {
                comment();
                continue;
            }

            if (isLetter()) {
                return identifierOrReservedWord();
            }

            if (isDigit()) {
                return numberLiteral();
            }

            if (c == '"') {
                return stringLiteral();
            }

            if (c == '\'') {
                return attribute();
            }

            int tokenLine = line;
            int tokenColumn = column;

            switch (c) {
                case ':':
                    consume();
                    if (c == '=') {
                        consume();
                        return new Token(TokenType.ASSIGNMENT_OP, ":=", tokenLine, tokenColumn);
                    }
                    return new Token(TokenType.COLON, ":", tokenLine, tokenColumn);
                case '/':
                    consume();
                    if (c == '=') {
                        consume();
                        return new Token(TokenType.DIFFERENCE_OP, "/=", tokenLine, tokenColumn);
                    }
                    return new Token(TokenType.DIVIDE_OP, "/", tokenLine, tokenColumn);
                case '=': consume(); return new Token(TokenType.EQUALITY_OP, "=", tokenLine, tokenColumn);
                case '<':
                    consume();
                    if (c == '=') {
                        consume();
                        return new Token(TokenType.LESS_EQUAL_OP, "<=", tokenLine, tokenColumn);
                    }
                    return new Token(TokenType.LESS_THAN, "<", tokenLine, tokenColumn);
                case '>':
                    consume();
                    if (c == '=') {
                        consume();
                        return new Token(TokenType.GREATER_EQUAL_OP, ">=", tokenLine, tokenColumn);
                    }
                    return new Token(TokenType.GREATER_THAN, ">", tokenLine, tokenColumn);
                case '.':
                    consume();
                    if (c == '.') {
                        consume();
                        return new Token(TokenType.RANGE_OP, "..", tokenLine, tokenColumn);
                    }
                    return new Token(TokenType.DOT, ".", tokenLine, tokenColumn);
                case '+': consume(); return new Token(TokenType.PLUS_OP, "+", tokenLine, tokenColumn);
                case '-': consume(); return new Token(TokenType.MINUS_OP, "-", tokenLine, tokenColumn);
                case '*': consume(); return new Token(TokenType.MULTIPLY_OP, "*", tokenLine, tokenColumn);
                case '(': consume(); return new Token(TokenType.PAREN_LEFT, "(", tokenLine, tokenColumn);
                case ')': consume(); return new Token(TokenType.PAREN_RIGHT, ")", tokenLine, tokenColumn);
                case ';': consume(); return new Token(TokenType.SEMICOLON, ";", tokenLine, tokenColumn);
                case ',': consume(); return new Token(TokenType.COMMA, ",", tokenLine, tokenColumn);
                case '&': consume(); return new Token(TokenType.CONCATENATION_OP, "&", tokenLine, tokenColumn);
            }

            throw new RuntimeException("Carácter no válido: '" + c + "'");
        }

        return new Token(TokenType.EOF, "<EOF>", line, column);
    }

    /**
     * Parses attribute references in the Ada source code.
     * Consumes characters following an apostrophe until a delimiter is found.
     * @return Token representing the attribute reference
     */
    private Token attribute() {
        int startLine = line;
        int startColumn = column;
        consume(); // Consume la comilla simple
        StringBuilder buf = new StringBuilder();

        while (isLetter() || isDigit() || c == '_') {
            buf.append(c);
            consume();
        }

        return new Token(TokenType.ATTRIBUTE_OP, "'" + buf.toString(), startLine, startColumn);
    }
}