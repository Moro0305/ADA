import java.util.ArrayList;

public class AdaParser {

    private ArrayList<Token> tokens;
    private int tokenIndex = 0;

    private static final int LOOKAHEAD_K = 2;
    private final Token[] lookaheadBuffer = new Token[LOOKAHEAD_K];

    public AdaParser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        for (int i = 0; i < LOOKAHEAD_K; i++) {
            if (i < tokens.size()) {
                lookaheadBuffer[i] = tokens.get(i);
            } else {
                lookaheadBuffer[i] = new Token(TokenType.EOF, "<EOF>", 0, 0);
            }
        }
    }

    public TokenType LA(int k) {
        if (k > LOOKAHEAD_K || tokenIndex + k - 1 >= tokens.size()) {
            return TokenType.EOF;
        }
        return lookaheadBuffer[k - 1].type;
    }

    public Token LT(int k) {
        if (k > LOOKAHEAD_K || tokenIndex + k - 1 >= tokens.size()) {
            return new Token(TokenType.EOF, "<EOF>", 0, 0);
        }
        return lookaheadBuffer[k - 1];
    }

    private void consume() {
        tokenIndex++;
        for (int i = 0; i < LOOKAHEAD_K - 1; i++) {
            lookaheadBuffer[i] = lookaheadBuffer[i + 1];
        }
        if (tokenIndex + LOOKAHEAD_K - 1 < tokens.size()) {
            lookaheadBuffer[LOOKAHEAD_K - 1] = tokens.get(tokenIndex + LOOKAHEAD_K - 1);
        } else {
            lookaheadBuffer[LOOKAHEAD_K - 1] = new Token(TokenType.EOF, "<EOF>", 0, 0);
        }
    }

    public void analizar() throws SyntaxException {
        programa();
        if (LA(1) != TokenType.EOF) {
            throw new SyntaxException("Se esperaba el final del archivo, pero se encontró un token inesperado: '" + LT(1).text + "' en la línea " + LT(1).line + ", columna " + LT(1).column);
        }
        System.out.println("La sintaxis del programa es correcta. ✅");
    }

    private void match(TokenType expectedType) throws SyntaxException {
        if (LA(1) != expectedType) {

            StringBuilder contexto = new StringBuilder();
            int startContext = Math.max(0, tokenIndex - 2);
            for (int i = startContext; i < tokenIndex; i++) {
                contexto.append(tokens.get(i).text).append(" ");
            }
            contexto.append(">>").append(LT(1).text).append("<<");
            int endContext = Math.min(tokens.size(), tokenIndex + 3);
            for (int i = tokenIndex + 1; i < endContext; i++) {
                contexto.append(" ").append(tokens.get(i).text);
            }

            throw new SyntaxException(
                    "Se esperaba el token '" + expectedType.name() +
                            "' pero se encontró '" + LT(1).text +
                            "' en la línea " + LT(1).line + ", columna " + LT(1).column +
                            "\nContexto del error: " + contexto.toString()
            );
        }
        consume();
    }

    private void programa() throws SyntaxException {
        while (LA(1) == TokenType.WITH_KW) {
            withClause();
        }
        while (LA(1) == TokenType.USE_KW) {
            useClause();
        }
        match(TokenType.PROCEDURE_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.IS_KW);
        declaraciones();
        match(TokenType.BEGIN_KW);
        enunciados();
        match(TokenType.END_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.SEMICOLON);
    }

    private void withClause() throws SyntaxException {
        match(TokenType.WITH_KW);
        expresionPuntual();
        match(TokenType.SEMICOLON);
    }

    private void useClause() throws SyntaxException {
        match(TokenType.USE_KW);
        expresionPuntual();
        match(TokenType.SEMICOLON);
    }

    private void declaraciones() throws SyntaxException {
        while (LA(1) == TokenType.IDENTIFIER || LA(1) == TokenType.PACKAGE_KW || LA(1) == TokenType.FUNCTION_KW || LA(1) == TokenType.BODY_KW || LA(1) == TokenType.PROTECTED_KW || LA(1) == TokenType.RECORD_KW || LA(1) == TokenType.TASK_KW || LA(1) == TokenType.SUBTYPE_KW) {
            if (LA(1) == TokenType.PACKAGE_KW) {
                declaracionPaquete();
            } else if (LA(1) == TokenType.FUNCTION_KW) {
                declaracionFuncion();
            } else if (LA(1) == TokenType.BODY_KW) {
                declaracionBody();
            } else if (LA(1) == TokenType.PROTECTED_KW) {
                declaracionProtegida();
            } else if (LA(1) == TokenType.RECORD_KW) {
                declaracionRecord();
            } else if (LA(1) == TokenType.TASK_KW) {
                declaracionTask();
            } else if (LA(1) == TokenType.SUBTYPE_KW) {
                declaracionSubtype();
            } else {
                declaracionVariable();
            }
        }
    }

    private void enunciados() throws SyntaxException {
        while (LA(1) != TokenType.END_KW && LA(1) != TokenType.EOF && LA(1) != TokenType.ELSE_KW && LA(1) != TokenType.ELSIF_KW) {
            enunciado();
        }
    }

    private void enunciado() throws SyntaxException {
        switch (LA(1)) {
            case IDENTIFIER:
                // Usamos Lookahead para decidir si es asignación o llamada
                if (LA(2) == TokenType.ASSIGNMENT_OP) {
                    asignacion();
                } else {
                    llamadaProcedimiento();
                }
                break;
            case IF_KW:
                sentenciaSi();
                break;
            case FOR_KW:
                sentenciaFor();
                break;
            case WHILE_KW:
                sentenciaLoop();
                break;
            case LOOP_KW:
                sentenciaLoop();
                break;
            case NULL_KW:
                match(TokenType.NULL_KW);
                match(TokenType.SEMICOLON);
                break;
            case CASE_KW:
                sentenciaCase();
                break;
            case DECLARE_KW:
                sentenciaDeclare();
                break;
            case SELECT_KW:
                sentenciaSelect();
                break;
            case GOTO_KW:
                sentenciaGoto();
                break;
            case RETURN_KW:
                sentenciaReturn();
                break;
            case RAISE_KW:
                sentenciaRaise();
                break;
            case EXIT_KW:
                sentenciaExit();
                break;
            case SEPARATE_KW:
                sentenciaSeparate();
                break;
            default:
                throw new SyntaxException("Se esperaba un enunciado, pero se encontró '" + LT(1).text + "' en la línea " + LT(1).line + ", columna " + LT(1).column);
        }
    }

    private void asignacion() throws SyntaxException {
        // La asignación puede tener una expresion compleja a la izquierda
        expresion();
        match(TokenType.ASSIGNMENT_OP);
        expresion();
        match(TokenType.SEMICOLON);
    }

    private void llamadaProcedimiento() throws SyntaxException {
        expresion();
        match(TokenType.SEMICOLON);
    }

    private void declaracionVariable() throws SyntaxException {
        match(TokenType.IDENTIFIER);
        match(TokenType.COLON);

        if (LA(1) == TokenType.CONSTANT_KW) {
            match(TokenType.CONSTANT_KW);
        }

        match(TokenType.IDENTIFIER);

        if (LA(1) == TokenType.ASSIGNMENT_OP) {
            match(TokenType.ASSIGNMENT_OP);
            expresion();
        }
        match(TokenType.SEMICOLON);
    }

    private void listaParametros() throws SyntaxException {
        if (LA(1) == TokenType.PAREN_RIGHT) {
            return;
        }
        expresion();
        while (LA(1) == TokenType.COMMA) {
            match(TokenType.COMMA);
            expresion();
        }
    }

    // <Expresion> -> <Termino> ( (AND_KW | OR_KW | XOR_KW) <Termino> )*
    private void expresion() throws SyntaxException {
        termino();
        while (LA(1) == TokenType.AND_KW || LA(1) == TokenType.OR_KW || LA(1) == TokenType.XOR_KW) {
            consume();
            termino();
        }
    }

    // <Termino> -> <Factor> ( (PLUS_OP | MINUS_OP | CONCATENATION_OP) <Factor> )*
    private void termino() throws SyntaxException {
        factor();
        while (LA(1) == TokenType.PLUS_OP || LA(1) == TokenType.MINUS_OP || LA(1) == TokenType.CONCATENATION_OP) {
            consume();
            factor();
        }
    }

    // <Factor> -> <Literal> | <Primario> | ( <Expresion> )
    private void factor() throws SyntaxException {
        TokenType currentType = LA(1);
        if (currentType == TokenType.INTEGER_LITERAL ||
                currentType == TokenType.REAL_LITERAL ||
                currentType == TokenType.STRING_LITERAL) {
            literal();
        } else if (currentType == TokenType.IDENTIFIER) {
            primario();
        } else if (currentType == TokenType.PAREN_LEFT) {
            match(TokenType.PAREN_LEFT);
            expresion();
            match(TokenType.PAREN_RIGHT);
        } else {
            throw new SyntaxException("Se esperaba un literal, identificador o expresión entre paréntesis en la línea " + LT(1).line + ", columna " + LT(1).column);
        }
    }

    private void primario() throws SyntaxException {
        match(TokenType.IDENTIFIER);
        while (true) {
            if (LA(1) == TokenType.DOT) {
                match(TokenType.DOT);
                match(TokenType.IDENTIFIER);
            } else if (LA(1) == TokenType.ATTRIBUTE_OP) {
                match(TokenType.ATTRIBUTE_OP);
                match(TokenType.IDENTIFIER);
                if (LA(1) == TokenType.PAREN_LEFT) {
                    match(TokenType.PAREN_LEFT);
                    listaParametros();
                    match(TokenType.PAREN_RIGHT);
                }
            } else if (LA(1) == TokenType.PAREN_LEFT) {
                match(TokenType.PAREN_LEFT);
                if (LA(2) == TokenType.PAREN_RIGHT) { // Maneja el caso de ()
                    listaParametros();
                } else {
                    expresion();
                }
                match(TokenType.PAREN_RIGHT);
            } else {
                break;
            }
        }
    }

    private void expresionPuntual() throws SyntaxException {
        match(TokenType.IDENTIFIER);
        while (LA(1) == TokenType.DOT) {
            match(TokenType.DOT);
            match(TokenType.IDENTIFIER);
        }
    }

    private void literal() throws SyntaxException {
        TokenType currentType = LA(1);
        if (currentType == TokenType.INTEGER_LITERAL ||
                currentType == TokenType.REAL_LITERAL ||
                currentType == TokenType.STRING_LITERAL) {
            consume();
        } else {
            throw new SyntaxException("Se esperaba un literal en la línea " + LT(1).line + ", columna " + LT(1).column);
        }
    }

    private void sentenciaSi() throws SyntaxException {
        match(TokenType.IF_KW);
        condicion();
        match(TokenType.THEN_KW);
        enunciados();

        while (LA(1) == TokenType.ELSIF_KW) {
            match(TokenType.ELSIF_KW);
            condicion();
            match(TokenType.THEN_KW);
            enunciados();
        }

        if (LA(1) == TokenType.ELSE_KW) {
            match(TokenType.ELSE_KW);
            enunciados();
        }

        match(TokenType.END_KW);
        match(TokenType.IF_KW);
        match(TokenType.SEMICOLON);
    }

    private void sentenciaFor() throws SyntaxException {
        match(TokenType.FOR_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.IN_KW);
        if (LA(1) == TokenType.REVERSE_KW) {
            match(TokenType.REVERSE_KW);
        }
        expresion();
        match(TokenType.RANGE_OP);
        expresion();
        match(TokenType.LOOP_KW);
        enunciados();
        match(TokenType.END_KW);
        match(TokenType.LOOP_KW);
        match(TokenType.SEMICOLON);
    }

    private void sentenciaLoop() throws SyntaxException {
        if (LA(1) == TokenType.WHILE_KW) {
            match(TokenType.WHILE_KW);
            condicion();
        }
        match(TokenType.LOOP_KW);
        enunciados();

        if (LA(1) == TokenType.UNTIL_KW) {
            match(TokenType.UNTIL_KW);
            condicion();
        }

        match(TokenType.END_KW);
        match(TokenType.LOOP_KW);
        match(TokenType.SEMICOLON);
    }

    private void sentenciaCase() throws SyntaxException {
        match(TokenType.CASE_KW);
        expresion();
        match(TokenType.IS_KW);
        while (LA(1) == TokenType.WHEN_KW) {
            match(TokenType.WHEN_KW);
            expresion();
            match(TokenType.DOUBLE_ARROW);
            enunciados();
        }
        if (LA(1) == TokenType.OTHERS_KW) {
            match(TokenType.OTHERS_KW);
            match(TokenType.DOUBLE_ARROW);
            enunciados();
        }
        match(TokenType.END_KW);
        match(TokenType.CASE_KW);
        match(TokenType.SEMICOLON);
    }

    private void sentenciaDeclare() throws SyntaxException {
        match(TokenType.DECLARE_KW);
        declaraciones();
        match(TokenType.BEGIN_KW);
        enunciados();
        match(TokenType.END_KW);
        match(TokenType.SEMICOLON);
    }

    private void sentenciaSelect() throws SyntaxException {
        match(TokenType.SELECT_KW);
        enunciados();
        match(TokenType.END_KW);
        match(TokenType.SELECT_KW);
        match(TokenType.SEMICOLON);
    }

    private void sentenciaGoto() throws SyntaxException {
        match(TokenType.GOTO_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.SEMICOLON);
    }

    private void sentenciaReturn() throws SyntaxException {
        match(TokenType.RETURN_KW);
        if (LA(1) != TokenType.SEMICOLON) {
            expresion();
        }
        match(TokenType.SEMICOLON);
    }

    private void sentenciaRaise() throws SyntaxException {
        match(TokenType.RAISE_KW);
        if (LA(1) == TokenType.IDENTIFIER) {
            match(TokenType.IDENTIFIER);
        }
        match(TokenType.SEMICOLON);
    }

    private void sentenciaExit() throws SyntaxException {
        match(TokenType.EXIT_KW);
        if (LA(1) == TokenType.WHEN_KW) {
            match(TokenType.WHEN_KW);
            condicion();
        }
        match(TokenType.SEMICOLON);
    }

    private void sentenciaSeparate() throws SyntaxException {
        match(TokenType.SEPARATE_KW);
        match(TokenType.PAREN_LEFT);
        match(TokenType.IDENTIFIER);
        match(TokenType.PAREN_RIGHT);
        match(TokenType.SEMICOLON);
    }

    private void declaracionPaquete() throws SyntaxException {
        match(TokenType.PACKAGE_KW);
        expresionPuntual();
        match(TokenType.IS_KW);
        declaraciones();
        match(TokenType.END_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.SEMICOLON);
    }

    private void declaracionFuncion() throws SyntaxException {
        match(TokenType.FUNCTION_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.RETURN_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.IS_KW);
        declaraciones();
        match(TokenType.BEGIN_KW);
        enunciados();
        match(TokenType.END_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.SEMICOLON);
    }

    private void declaracionBody() throws SyntaxException {
        match(TokenType.BODY_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.IS_KW);
        declaraciones();
        match(TokenType.BEGIN_KW);
        enunciados();
        match(TokenType.END_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.SEMICOLON);
    }

    private void declaracionProtegida() throws SyntaxException {
        match(TokenType.PROTECTED_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.IS_KW);
        declaraciones();
        match(TokenType.END_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.SEMICOLON);
    }

    private void declaracionRecord() throws SyntaxException {
        match(TokenType.RECORD_KW);
        declaraciones();
        match(TokenType.END_KW);
        match(TokenType.RECORD_KW);
        match(TokenType.SEMICOLON);
    }

    private void declaracionTask() throws SyntaxException {
        match(TokenType.TASK_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.IS_KW);
        declaraciones();
        match(TokenType.END_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.SEMICOLON);
    }

    private void declaracionSubtype() throws SyntaxException {
        match(TokenType.SUBTYPE_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.IS_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.SEMICOLON);
    }

    private void condicion() throws SyntaxException {
        if (LA(1) == TokenType.PAREN_LEFT) {
            match(TokenType.PAREN_LEFT);
            expresion();
            operadorRelacional();
            expresion();
            match(TokenType.PAREN_RIGHT);
        } else {
            expresion();
            operadorRelacional();
            expresion();
        }
    }

    private void operadorRelacional() throws SyntaxException {
        TokenType currentType = LA(1);
        if (currentType == TokenType.LESS_THAN ||
                currentType == TokenType.GREATER_THAN ||
                currentType == TokenType.LESS_EQUAL_OP ||
                currentType == TokenType.GREATER_EQUAL_OP ||
                currentType == TokenType.EQUALITY_OP ||
                currentType == TokenType.DIFFERENCE_OP) {
            consume();
        } else {
            throw new SyntaxException("Se esperaba un operador relacional en la línea " + LT(1).line + ", columna " + LT(1).column);
        }
    }
}