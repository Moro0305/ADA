/**
 * AdaParser parses Ada source code tokens and builds the program structure.
 * Supports nested symbol tables for scope management and semantic checks.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AdaParser {
    /** List of tokens to parse */
    private ArrayList<Token> tokens;
    /** Current token index in the token list */
    private int tokenIndex = 0;
    /** Lookahead buffer for predictive parsing */
    private static final int LOOKAHEAD_K = 4;
    private final Token[] lookaheadBuffer = new Token[LOOKAHEAD_K];
    /** Symbol table for managing scopes and declarations */
    private SymbolTable symbolTable;

    /** Set of predefined identifiers to ignore during semantic checks */
    private static final Set<String> PREDEFINED_IDENTIFIERS = new HashSet<>(Arrays.asList(
            // Subprogramas y E/S estándar
            "Put_Line", "Get_Line", "New_Line", "Put", "Get",
            // Paquetes estándar
            "Ada", "Text_IO",
            // Tipos escalares estándar
            "Integer", "Float", "Boolean", "Character", "Natural", "Positive"
    ));

    /**
     * Constructs an AdaParser with the given tokens.
     * Initializes the lookahead buffer and symbol table.
     * @param tokens List of tokens to parse
     */
    public AdaParser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.symbolTable = new SymbolTable(); // Initialize symbol table
        for (int i = 0; i < LOOKAHEAD_K; i++) {
            if (i < tokens.size()) {
                lookaheadBuffer[i] = tokens.get(i);
            } else {
                lookaheadBuffer[i] = new Token(TokenType.EOF, "<EOF>", 0, 0);
            }
        }
    }

    /**
     * Returns the type of the k-th lookahead token.
     * @param k Lookahead position (1-based)
     * @return TokenType of the k-th lookahead token
     */
    public TokenType LA(int k) {
        if (k > LOOKAHEAD_K || tokenIndex + k - 1 >= tokens.size()) {
            return TokenType.EOF;
        }
        return lookaheadBuffer[k - 1].type;
    }

    /**
     * Returns the k-th lookahead token.
     * @param k Lookahead position (1-based)
     * @return Token object at the k-th lookahead position
     */
    public Token LT(int k) {
        if (k > LOOKAHEAD_K || tokenIndex + k - 1 >= tokens.size()) {
            return new Token(TokenType.EOF, "<EOF>", 0, 0);
        }
        return lookaheadBuffer[k - 1];
    }

    /**
     * Consumes the current token and advances the lookahead buffer.
     */
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

    /**
     * Entry point for parsing. Throws SyntaxException if syntax is invalid.
     */
    public void analizar() throws SyntaxException {
        programa();
        if (LA(1) != TokenType.EOF) {
            throw new SyntaxException("Se esperaba el final del archivo, pero se encontró un token inesperado: '" + LT(1).text + "' en la línea " + LT(1).line + ", columna " + LT(1).column);
        }
        System.out.println("La sintaxis del programa es correcta. ✅");
    }

    /**
     * Matches the expected token type, throws SyntaxException if not matched.
     * @param expectedType Expected TokenType to match
     * @throws SyntaxException if the next token does not match expectedType
     */
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
        while (LA(1) != TokenType.EOF) {
            // Procesa cláusulas de contexto al principio de cada unidad de compilación
            while (LA(1) == TokenType.WITH_KW || LA(1) == TokenType.USE_KW) {
                if (LA(1) == TokenType.WITH_KW) {
                    withClause();
                } else {
                    useClause();
                }
            }

            // Determina qué tipo de unidad de compilación es la siguiente
            if (LA(1) == TokenType.PROCEDURE_KW) {
                declaracionProcedimiento();
            } else if (LA(1) == TokenType.FUNCTION_KW) {
                declaracionFuncion();
            } else if (LA(1) == TokenType.PACKAGE_KW) {
                if (LA(2) == TokenType.BODY_KW) {
                    declaracionCuerpoPaquete();
                } else {
                    declaracionPaquete();
                }
            } else if (LA(1) == TokenType.GENERIC_KW) {
                declaracionGenerica();
            } else if (LA(1) == TokenType.TASK_KW) {
                if (LA(2) == TokenType.BODY_KW) {
                    declaracionCuerpoTask();
                } else {
                    declaracionTask();
                }
            } else if (LA(1) == TokenType.INTERFACE_KW) {
                declaracionInterface();
            } else {
                // Si no se encuentra una unidad de compilación conocida, se lanza un error
                throw new SyntaxException("Se esperaba una unidad de compilación, pero se encontró '" + LT(1).text + "' en la línea " + LT(1).line + ", columna " + LT(1).column);
            }
        }
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
        while (true) {
            // Lógica corregida para diferenciar entre 'renames' y 'declaracion de variable'
            if (LA(1) == TokenType.IDENTIFIER && LA(2) == TokenType.COLON && LA(3) == TokenType.IDENTIFIER && LA(4) == TokenType.RENAMES_KW) {
                declaracionRenames();
            } else if (LA(1) == TokenType.IDENTIFIER && LA(2) == TokenType.COLON) {
                declaracionVariable();
            } else if (LA(1) == TokenType.TYPE_KW || LA(1) == TokenType.ABSTRACT_KW || LA(1) == TokenType.TAGGED_KW || LA(1) == TokenType.LIMITED_KW || LA(1) == TokenType.PRIVATE_KW) {
                declaracionTipo();
            } else if (LA(1) == TokenType.PACKAGE_KW) {
                // Maneja la especificación y el cuerpo del paquete
                if (LA(2) == TokenType.BODY_KW) {
                    declaracionCuerpoPaquete();
                } else {
                    declaracionPaquete();
                }
            } else if (LA(1) == TokenType.FUNCTION_KW) {
                declaracionFuncion();
            } else if (LA(1) == TokenType.BODY_KW) {
                // Ya tienes esta lógica, pero la refactorizamos para mayor claridad
                declaracionBody();
            } else if (LA(1) == TokenType.PROTECTED_KW) {
                declaracionProtegida();
            } else if (LA(1) == TokenType.RECORD_KW) {
                declaracionRecord();
            } else if (LA(1) == TokenType.SUBTYPE_KW) {
                declaracionSubtype();
            } else if (LA(1) == TokenType.GENERIC_KW) {
                declaracionGenerica();
            } else if (LA(1) == TokenType.INTERFACE_KW) {
                declaracionInterface();
            } else if (LA(1) == TokenType.ENTRY_KW || LA(1) == TokenType.ACCEPT_KW) {
                declaracionEntrada();
            } else if (LA(1) == TokenType.TASK_KW) {
                // Check for a task body first
                if (LA(2) == TokenType.BODY_KW) {
                    declaracionCuerpoTask();
                } else {
                    declaracionTask();
                }
            }else {
                break;
            }
        }
    }

    private void declaracionCuerpoPaquete() throws SyntaxException {
        match(TokenType.PACKAGE_KW);
        match(TokenType.BODY_KW);
        expresionPuntual();
        match(TokenType.IS_KW);

        // El cuerpo del paquete puede tener declaraciones de subprogramas directamente.
        // La sección de declaraciones y el bloque BEGIN son opcionales.

        // Procesa las declaraciones de subprogramas y otras declaraciones
        while (LA(1) == TokenType.PROCEDURE_KW || LA(1) == TokenType.FUNCTION_KW || LA(1) == TokenType.PRAGMA_KW || LA(1) == TokenType.TYPE_KW) {
            if (LA(1) == TokenType.PROCEDURE_KW) {
                declaracionProcedimiento();
            } else if (LA(1) == TokenType.FUNCTION_KW) {
                declaracionFuncion();
            } else if (LA(1) == TokenType.PRAGMA_KW) {
                directivaPragma();
            } else if (LA(1) == TokenType.TYPE_KW) {
                declaracionTipo();
            }
            // Agrega más tipos de declaraciones si es necesario
        }

        // Procesa el bloque de inicialización del paquete si existe
        if (LA(1) == TokenType.BEGIN_KW) {
            match(TokenType.BEGIN_KW);
            enunciados();
        }

        match(TokenType.END_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.SEMICOLON);
    }

    private void declaracionCuerpoTask() throws SyntaxException {
        match(TokenType.TASK_KW);
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

    private void enunciados() throws SyntaxException {
        while (LA(1) != TokenType.END_KW && LA(1) != TokenType.EOF && LA(1) != TokenType.ELSE_KW && LA(1) != TokenType.ELSIF_KW) {
            enunciado();
        }
    }

    private void enunciado() throws SyntaxException {
        switch (LA(1)) {
            case IDENTIFIER:
                expresionOAsignacion();
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
            case DELAY_KW:
                enunciadoDelay();
                break;
            case ABORT_KW:
                sentenciaAbort();
                break;
            case REQUEUE_KW:
                sentenciaRequeue();
                break;
            case TERMINATE_KW:
                sentenciaTerminate();
                break;
            case PRAGMA_KW:
                directivaPragma();
                break;
            default:
                throw new SyntaxException("Se esperaba un enunciado, pero se encontró '" + LT(1).text + "' en la línea " + LT(1).line + ", columna " + LT(1).column);
        }
    }

    private void enunciadoDelay() throws SyntaxException {
        match(TokenType.DELAY_KW);
        expresion();
        match(TokenType.SEMICOLON);
    }

    private void expresionOAsignacion() throws SyntaxException {
        // Manejar primero la asignación
        if (LA(1) == TokenType.IDENTIFIER) {
            if (LA(2) == TokenType.ASSIGNMENT_OP) {
                Token varToken = LT(1);
                // Verificar que la variable esté declarada antes de usarla
                symbolTable.verifySymbolDeclared(varToken.text, varToken.line, varToken.column);
                match(TokenType.IDENTIFIER);
                match(TokenType.ASSIGNMENT_OP);
                expresion();
                match(TokenType.SEMICOLON);
                return;
            }
        }

        // Si no es una asignación, puede ser una llamada a procedimiento
        if (LA(1) == TokenType.IDENTIFIER) {
            // Lookahead para manejar notación de punto (ej. Ada.Text_IO)
            if (LA(2) == TokenType.DOT) {
                // No verificar aquí, probablemente es un paquete o módulo
                expresionPuntual();
            } else {
                Token idToken = LT(1);
                // Verificar que el identificador esté declarado (puede ser procedimiento o función)
                if (!PREDEFINED_IDENTIFIERS.contains(idToken.text)) {
                    symbolTable.verifySymbolDeclared(idToken.text, idToken.line, idToken.column);
                }
                match(TokenType.IDENTIFIER);
            }

            if (LA(1) == TokenType.PAREN_LEFT) {
                match(TokenType.PAREN_LEFT);
                listaExpresiones();
                match(TokenType.PAREN_RIGHT);
            }
        } else {
            // En caso de que no sea ninguna de las anteriores, es un error de sintaxis.
            throw new SyntaxException("Se esperaba un identificador para una asignación o llamada a procedimiento en la línea " + LT(1).line + ", columna " + LT(1).column);
        }
        match(TokenType.SEMICOLON);
    }

    private void declaracionVariable() throws SyntaxException {
        Token varName = LT(1);
        match(TokenType.IDENTIFIER);
        match(TokenType.COLON);

        if (LA(1) == TokenType.CONSTANT_KW) {
            match(TokenType.CONSTANT_KW);
        }

        Token typeName = LT(1);
        match(TokenType.IDENTIFIER);
        // Register variable in symbol table
        symbolTable.addSymbol(new Symbol(varName.text, "variable", typeName.text));

        if (LA(1) == TokenType.AT_KW) {
            match(TokenType.AT_KW);
            expresion();
        }

        if (LA(1) == TokenType.ASSIGNMENT_OP) {
            match(TokenType.ASSIGNMENT_OP);
            // The key change: The initializer can be a list of expressions in parentheses.
            if (LA(1) == TokenType.PAREN_LEFT) {
                match(TokenType.PAREN_LEFT);
                listaExpresiones(); // Handles the comma-separated list
                match(TokenType.PAREN_RIGHT);
            } else {
                // Or it can be a single expression
                expresion();
            }
        }
        match(TokenType.SEMICOLON);
    }

    private void listaExpresiones() throws SyntaxException {
        // Handle empty list case
        if (LA(1) == TokenType.PAREN_RIGHT) {
            return;
        }

        // At least one expression is expected
        expresion();
        while (LA(1) == TokenType.COMMA) {
            match(TokenType.COMMA);
            expresion();
        }
    }

    private void declaracionGenerica() throws SyntaxException {
        match(TokenType.GENERIC_KW);
        // A generic can have many parameters
        while (LA(1) == TokenType.IDENTIFIER || LA(1) == TokenType.WITH_KW) {
            // Simplified logic for processing generic parameters
            match(TokenType.IDENTIFIER);
            if (LA(1) == TokenType.IS_KW) {
                match(TokenType.IS_KW);
                if (LA(1) == TokenType.NEW_KW) {
                    match(TokenType.NEW_KW);
                }
            }
            if (LA(1) == TokenType.SEMICOLON) {
                match(TokenType.SEMICOLON);
            }
        }
        // The generic body can be a package or a subprogram
        if (LA(1) == TokenType.PACKAGE_KW) {
            declaracionPaquete();
        } else if (LA(1) == TokenType.FUNCTION_KW || LA(1) == TokenType.PROCEDURE_KW) {
            // Logic for subprogram
            match(LA(1)); // consume FUNCTION_KW or PROCEDURE_KW
            match(TokenType.IDENTIFIER);
            match(TokenType.IS_KW);
            declaraciones();
            match(TokenType.BEGIN_KW);
            enunciados();
            match(TokenType.END_KW);
            match(TokenType.IDENTIFIER);
            match(TokenType.SEMICOLON);
        }
    }

    private void declaracionInterface() throws SyntaxException {
        match(TokenType.INTERFACE_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.IS_KW);

        while (LA(1) == TokenType.ENTRY_KW || LA(1) == TokenType.PROCEDURE_KW || LA(1) == TokenType.FUNCTION_KW) {
            if (LA(1) == TokenType.ENTRY_KW) {
                declaracionEntrada();
            } else if (LA(1) == TokenType.PROCEDURE_KW) {
                match(TokenType.PROCEDURE_KW);
                match(TokenType.IDENTIFIER);
                match(TokenType.SEMICOLON);
            } else if (LA(1) == TokenType.FUNCTION_KW) {
                declaracionFuncion();
            }
        }

        match(TokenType.END_KW);
        // Este es el cambio: Espera 'INTERFACE_KW', no 'IDENTIFIER'
        match(TokenType.INTERFACE_KW);
        match(TokenType.SEMICOLON);
    }

    private void declaracionEntrada() throws SyntaxException {
        if (LA(1) == TokenType.ENTRY_KW) {
            match(TokenType.ENTRY_KW);
            match(TokenType.IDENTIFIER);
            match(TokenType.SEMICOLON);
        } else if (LA(1) == TokenType.ACCEPT_KW) {
            match(TokenType.ACCEPT_KW);
            match(TokenType.IDENTIFIER);
            // Optionally has parameters
            if (LA(1) == TokenType.PAREN_LEFT) {
                match(TokenType.PAREN_LEFT);
                listaParametros();
                match(TokenType.PAREN_RIGHT);
            }
            if (LA(1) == TokenType.DO_KW) {
                match(TokenType.DO_KW);
                enunciados();
                match(TokenType.END_KW);
                match(TokenType.IDENTIFIER);
            }
            match(TokenType.SEMICOLON);
        }
    }

    private void declaracionRenames() throws SyntaxException {
        match(TokenType.IDENTIFIER);
        match(TokenType.COLON);
        match(TokenType.IDENTIFIER);
        match(TokenType.RENAMES_KW);
        expresionPuntual();
        match(TokenType.SEMICOLON);
    }

    private void listaParametros() throws SyntaxException {
        // Si la lista de parámetros no está vacía
        if (LA(1) != TokenType.PAREN_RIGHT) {
            declaracionParametro();
            while (LA(1) == TokenType.SEMICOLON) {
                match(TokenType.SEMICOLON);
                declaracionParametro();
            }
        }
    }

    private void declaracionParametro() throws SyntaxException {
        // Declara una lista de identificadores (H en el ejemplo)
        ArrayList<Token> paramNames = new ArrayList<>();
        Token paramName = LT(1);
        paramNames.add(paramName);
        match(TokenType.IDENTIFIER);
        while (LA(1) == TokenType.COMMA) {
            match(TokenType.COMMA);
            paramName = LT(1);
            paramNames.add(paramName);
            match(TokenType.IDENTIFIER);
        }

        // El separador es un ':'
        match(TokenType.COLON);

        // Opcionalmente, un modo de paso
        if (LA(1) == TokenType.IN_KW) {
            match(TokenType.IN_KW);
        } else if (LA(1) == TokenType.OUT_KW) {
            match(TokenType.OUT_KW);
        } else if (LA(1) == TokenType.IN_KW && LA(2) == TokenType.OUT_KW) {
            match(TokenType.IN_KW);
            match(TokenType.OUT_KW);
        }

        // El tipo del parámetro - capturar antes de procesar
        Token paramType = LT(1);
        String typeString = paramType.text;

        // Si es una expresión puntual, construir el nombre completo
        match(TokenType.IDENTIFIER);
        while (LA(1) == TokenType.DOT) {
            match(TokenType.DOT);
            Token nextPart = LT(1);
            typeString += "." + nextPart.text;
            match(TokenType.IDENTIFIER);
        }

        // Registrar todos los parámetros en la tabla de símbolos
        for (Token param : paramNames) {
            symbolTable.addSymbol(new Symbol(param.text, "parameter", typeString));
        }

        // Opcionalmente, una inicialización
        if (LA(1) == TokenType.ASSIGNMENT_OP) {
            match(TokenType.ASSIGNMENT_OP);
            expresion();
        }
    }

    // Métodos de Expresión con Jerarquía
    private void expresion() throws SyntaxException {
        if (LA(1) == TokenType.SOME_KW) {
            cuantificadorExpresion();
        } else {
            // La expresión puede empezar con un término
            termino();

            // Se procesan los operadores relacionales
            if (LA(1) == TokenType.EQUALITY_OP || LA(1) == TokenType.DIFFERENCE_OP ||
                    LA(1) == TokenType.LESS_THAN || LA(1) == TokenType.LESS_EQUAL_OP ||
                    LA(1) == TokenType.GREATER_THAN || LA(1) == TokenType.GREATER_EQUAL_OP) {

                consume(); // Consume el operador relacional
                termino(); // Procesa el operando derecho
            }

            // Luego se procesan los operadores lógicos
            while (LA(1) == TokenType.AND_KW || LA(1) == TokenType.OR_KW || LA(1) == TokenType.XOR_KW) {
                consume();
                termino();
            }
        }
    }

    private void termino() throws SyntaxException {
        factor();
        while (LA(1) == TokenType.PLUS_OP || LA(1) == TokenType.MINUS_OP || LA(1) == TokenType.CONCATENATION_OP || LA(1) == TokenType.MOD_KW || LA(1) == TokenType.REM_KW) {
            consume();
            factor();
        }
    }

    private void factor() throws SyntaxException {
        if (LA(1) == TokenType.ABS_KW || LA(1) == TokenType.NOT_KW) {
            consume();
            factor();
        } else if (LA(1) == TokenType.MINUS_OP) {
            consume();
            factor();
        } else {
            TokenType currentType = LA(1);
            if (currentType == TokenType.INTEGER_LITERAL ||
                    currentType == TokenType.REAL_LITERAL ||
                    currentType == TokenType.STRING_LITERAL) {
                literal();
            } else if (currentType == TokenType.IDENTIFIER || currentType == TokenType.NEW_KW) {
                primario();
            } else if (currentType == TokenType.PAREN_LEFT) {
                match(TokenType.PAREN_LEFT);
                expresion();
                match(TokenType.PAREN_RIGHT);
            }else {
                throw new SyntaxException("Se esperaba un literal, identificador, 'new', 'abs', 'not' o 'resta unaria' o expresión entre paréntesis en la línea " + LT(1).line + ", columna " + LT(1).column);
            }
        }
    }

    private void cuantificadorExpresion() throws SyntaxException {
        match(TokenType.SOME_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.OF_KW);
        expresionPuntual();
        if (LA(1) == TokenType.ATTRIBUTE_OP) {
            match(TokenType.ATTRIBUTE_OP); // ATTRIBUTE_OP already encodes attribute name like 'Range
            // removed extra IDENTIFIER expectation
        }
        match(TokenType.DOUBLE_ARROW);
        expresion();
    }

    private void subexpresion() throws SyntaxException {
        match(TokenType.IDENTIFIER);
        if (LA(1) == TokenType.DOT) {
            match(TokenType.DOT);
            match(TokenType.IDENTIFIER);
        }
        if (LA(1) == TokenType.PAREN_LEFT) {
            match(TokenType.PAREN_LEFT);
            listaExpresiones();
            match(TokenType.PAREN_RIGHT);
        }
    }

    private void primario() throws SyntaxException {
        if (LA(1) == TokenType.NEW_KW) {
            match(TokenType.NEW_KW);
            match(TokenType.IDENTIFIER);
            if (LA(1) == TokenType.ATTRIBUTE_OP) {
                match(TokenType.ATTRIBUTE_OP); // token text contains attribute name
                if (LA(1) == TokenType.PAREN_LEFT) {
                    match(TokenType.PAREN_LEFT);
                    if (LA(1) != TokenType.PAREN_RIGHT) {
                        listaExpresiones();
                    }
                    match(TokenType.PAREN_RIGHT);
                }
            }
        } else if (LA(1) == TokenType.ABS_KW) {
            match(TokenType.ABS_KW);
            factor();
        } else {
            Token idToken = LT(1);
            // Verificar que el identificador esté declarado antes de usarlo,
            // salvo que sea uno de los predefinidos de Ada (tipos/paquetes estándar).
            if (!PREDEFINED_IDENTIFIERS.contains(idToken.text)) {
                symbolTable.verifySymbolDeclared(idToken.text, idToken.line, idToken.column);
            }
            match(TokenType.IDENTIFIER);
            while (true) {
                if (LA(1) == TokenType.DOT) {
                    match(TokenType.DOT);
                    match(TokenType.IDENTIFIER);
                } else if (LA(1) == TokenType.ATTRIBUTE_OP) {
                    match(TokenType.ATTRIBUTE_OP); // attribute token (e.g. 'Image)
                    if (LA(1) == TokenType.PAREN_LEFT) { // possible attribute argument list (e.g. Integer'Image(Result))
                        match(TokenType.PAREN_LEFT);
                        if (LA(1) != TokenType.PAREN_RIGHT) {
                            listaExpresiones();
                        }
                        match(TokenType.PAREN_RIGHT);
                    }
                } else if (LA(1) == TokenType.PAREN_LEFT) {
                    match(TokenType.PAREN_LEFT);
                    if (LA(1) != TokenType.PAREN_RIGHT) {
                        listaExpresiones();
                    }
                    match(TokenType.PAREN_RIGHT);
                } else {
                    break;
                }
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
                currentType == TokenType.STRING_LITERAL ||
                currentType == TokenType.BASE_LITERAL) {
            match(currentType);
        } else {
            throw new SyntaxException("Se esperaba un literal en la línea " + LT(1).line + ", columna " + LT(1).column);
        }
    }

    private boolean esLiteral() {
        TokenType currentType = LA(1);
        return currentType == TokenType.INTEGER_LITERAL ||
                currentType == TokenType.REAL_LITERAL ||
                currentType == TokenType.STRING_LITERAL ||
                currentType == TokenType.BASE_LITERAL;
    }

    private void sentenciaAbort() throws SyntaxException {
        match(TokenType.ABORT_KW);
        // An abort can have a list of tasks
        match(TokenType.IDENTIFIER);
        while (LA(1) == TokenType.COMMA) {
            match(TokenType.COMMA);
            match(TokenType.IDENTIFIER);
        }
        match(TokenType.SEMICOLON);
    }

    private void sentenciaRequeue() throws SyntaxException {
        match(TokenType.REQUEUE_KW);
        // Call an entry
        match(TokenType.IDENTIFIER);
        match(TokenType.DOT);
        match(TokenType.IDENTIFIER);
        if (LA(1) == TokenType.PAREN_LEFT) {
            match(TokenType.PAREN_LEFT);
            listaExpresiones();
            match(TokenType.PAREN_RIGHT);
        }
        // Optionally with 'with abort'
        if (LA(1) == TokenType.WITH_KW) {
            match(TokenType.WITH_KW);
            match(TokenType.ABORT_KW);
        }
        match(TokenType.SEMICOLON);
    }

    private void sentenciaTerminate() throws SyntaxException {
        match(TokenType.TERMINATE_KW);
        match(TokenType.SEMICOLON);
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

        // Aquí, la clave es analizar la expresión del rango.
        // La expresión del rango puede ser un atributo o un rango numérico.
        expresion();

        // Este 'if' es crucial para manejar el caso de un rango como '1..10'
        // La llamada anterior a 'expresion()' manejó 'Numeros'Range' por completo.
        if (LA(1) == TokenType.RANGE_OP) {
            match(TokenType.RANGE_OP);
            expresion();
        }

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
        symbolTable.enterScope(); // Nuevo scope para el bloque DECLARE
        declaraciones();
        match(TokenType.BEGIN_KW);
        enunciados();
        match(TokenType.END_KW);
        match(TokenType.SEMICOLON);
        symbolTable.exitScope(); // Salir del scope del bloque DECLARE
    }

    private void sentenciaSelect() throws SyntaxException {
        match(TokenType.SELECT_KW);
        do {
            sentenciaSelectAlternative();
            if (LA(1) == TokenType.OR_KW) {
                match(TokenType.OR_KW);
            } else {
                break;
            }
        } while (true);

        match(TokenType.DO_KW);
        enunciados();
        match(TokenType.END_KW);
        match(TokenType.SELECT_KW);
        match(TokenType.SEMICOLON);
    }

    private void sentenciaSelectAlternative() throws SyntaxException {
        if (LA(1) == TokenType.DELAY_KW) {
            enunciadoDelay();
        } else if (LA(1) == TokenType.IDENTIFIER) {
            match(TokenType.IDENTIFIER);
            match(TokenType.DOT);
            match(TokenType.IDENTIFIER);
            match(TokenType.SEMICOLON);
        } else {
            throw new SyntaxException("Se esperaba una alternativa de selección (delay, entrada) en la línea " + LT(1).line + ", columna " + LT(1).column);
        }
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

        // Maneja la variante 'is' o 'is new'
        if (LA(1) == TokenType.IS_KW) {
            match(TokenType.IS_KW);
        } else if (LA(1) == TokenType.IS_KW && LA(2) == TokenType.NEW_KW) {
            match(TokenType.IS_KW);
            match(TokenType.NEW_KW);
            expresionPuntual();
            match(TokenType.PAREN_LEFT);
            // Lógica para manejar los parámetros de instanciación genérica
            listaExpresiones();
            match(TokenType.PAREN_RIGHT);
        } else {
            throw new SyntaxException("Se esperaba 'is' o 'is new' en la línea " + LT(1).line + ", columna " + LT(1).column);
        }

        // Permite declaraciones de subprogramas dentro del paquete
        while (LA(1) == TokenType.PROCEDURE_KW || LA(1) == TokenType.FUNCTION_KW) {
            if (LA(1) == TokenType.PROCEDURE_KW) {
                declaracionProcedimiento();
            } else {
                declaracionFuncion();
            }
        }

        // Ahora, el parser buscará el 'END_KW' después de procesar todas las declaraciones
        match(TokenType.END_KW);
        match(TokenType.IDENTIFIER);
        match(TokenType.SEMICOLON);
    }

    private void declaracionProcedimiento() throws SyntaxException {
        match(TokenType.PROCEDURE_KW);
        Token procName = LT(1);
        match(TokenType.IDENTIFIER);
        // Register procedure in symbol table BEFORE entering the new scope
        symbolTable.addSymbol(new Symbol(procName.text, "procedure", null));
        symbolTable.enterScope(); // New scope for procedure
        if (LA(1) == TokenType.PAREN_LEFT) {
            match(TokenType.PAREN_LEFT);
            listaParametros();
            match(TokenType.PAREN_RIGHT);
        }
        if (LA(1) == TokenType.IS_KW) {
            match(TokenType.IS_KW);
            declaraciones();
            match(TokenType.BEGIN_KW);
            enunciados();
            match(TokenType.END_KW);
            if (LA(1) == TokenType.IDENTIFIER) {
                match(TokenType.IDENTIFIER);
            }
        }
        match(TokenType.SEMICOLON);
        symbolTable.exitScope(); // Exit procedure
    }

    private void declaracionFuncion() throws SyntaxException {
        // 1) FUNCTION <id>
        match(TokenType.FUNCTION_KW);
        Token funcName = LT(1);
        match(TokenType.IDENTIFIER);

        // 2) Cabecera: parámetros opcionales + RETURN tipo
        // Guardamos la posición actual para poder reprocesar parámetros al entrar al nuevo scope
        int headerStartIndex = tokenIndex;

        // --- parse parámetros si existen (solo para avanzar el parser sobre la cabecera) ---
        if (LA(1) == TokenType.PAREN_LEFT) {
            match(TokenType.PAREN_LEFT);
            listaParametros();
            match(TokenType.PAREN_RIGHT);
        }

        // --- parse RETURN tipo de retorno ---
        match(TokenType.RETURN_KW);
        Token returnTypeTok = LT(1);
        match(TokenType.IDENTIFIER); // asumimos tipo simple

        // 3) Registrar la función en el scope ACTUAL (el del procedimiento que la contiene)
        symbolTable.addSymbol(new Symbol(funcName.text, "function", returnTypeTok.text));

        // 4) Ahora crear un nuevo scope para el cuerpo de la función
        symbolTable.enterScope();

        // 5) Volver a procesar parámetros dentro del nuevo scope
        //    Volvemos el índice a donde empezaba la parte de parámetros/RETURN
        tokenIndex = headerStartIndex;
        // Reconstruimos el buffer de lookahead acorde al nuevo índice
        for (int i = 0; i < LOOKAHEAD_K; i++) {
            if (tokenIndex + i < tokens.size()) {
                lookaheadBuffer[i] = tokens.get(tokenIndex + i);
            } else {
                lookaheadBuffer[i] = new Token(TokenType.EOF, "<EOF>", 0, 0);
            }
        }

        // Re-parse parámetros ahora sí añadiendo parámetros al nuevo scope a través de listaParametros()
        if (LA(1) == TokenType.PAREN_LEFT) {
            match(TokenType.PAREN_LEFT);
            listaParametros();
            match(TokenType.PAREN_RIGHT);
        }

        // Re-parse RETURN tipo (solo avanza el parser; no es necesario volver a registrar nada)
        match(TokenType.RETURN_KW);
        match(TokenType.IDENTIFIER);

        // 6) Cuerpo de la función: IS declaraciones? BEGIN enunciados END id?;
        if (LA(1) == TokenType.IS_KW) {
            match(TokenType.IS_KW);
            declaraciones();
            match(TokenType.BEGIN_KW);
            enunciados();
            match(TokenType.END_KW);
            if (LA(1) == TokenType.IDENTIFIER) {
                match(TokenType.IDENTIFIER); // nombre opcional tras END
            }
        }
        match(TokenType.SEMICOLON);
        symbolTable.exitScope(); // salir del scope de la función
    }

    private void declaracionBody() throws SyntaxException {
        // Placeholder for body declaration parsing
        throw new SyntaxException("Parsing for body declarations is not yet implemented.");
    }

    private void declaracionProtegida() throws SyntaxException {
        // Placeholder for protected declaration parsing
        throw new SyntaxException("Parsing for protected declarations is not yet implemented.");
    }

    private void declaracionRecord() throws SyntaxException {
        // Placeholder for record declaration parsing
        throw new SyntaxException("Parsing for record declarations is not yet implemented.");
    }

    private void declaracionTask() throws SyntaxException {
        // Placeholder for task declaration parsing
        throw new SyntaxException("Parsing for task declarations is not yet implemented.");
    }

    private void declaracionSubtype() throws SyntaxException {
        // Placeholder for subtype declaration parsing
        throw new SyntaxException("Parsing for subtype declarations is not yet implemented.");
    }

    private void declaracionTipo() throws SyntaxException {
        // Placeholder for type declaration parsing
        throw new SyntaxException("Parsing for type declarations is not yet implemented.");
    }

    private void directivaPragma() throws SyntaxException {
        // Placeholder for pragma directive parsing
        throw new SyntaxException("Parsing for pragma directives is not yet implemented.");
    }

    private void condicion() throws SyntaxException {
        // Placeholder for condition parsing
        expresion();
    }
}
