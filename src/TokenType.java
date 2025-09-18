public enum TokenType {
    EOF,
    IDENTIFIER,
    INTEGER_LITERAL,
    REAL_LITERAL,
    STRING_LITERAL,

    // Palabras reservadas de Ada
    ABORT_KW, ABS_KW, ABSTRACT_KW, ACCEPT_KW, ACCESS_KW, ALIASED_KW, ALL_KW, AND_KW, ARRAY_KW, AT_KW,
    BEGIN_KW, BODY_KW, CASE_KW, CONSTANT_KW, DECLARE_KW, DELAY_KW, DELTA_KW, DIGITS_KW, DO_KW, ELSE_KW,
    ELSIF_KW, END_KW, ENTRY_KW, EXCEPTION_KW, EXIT_KW, FOR_KW, FUNCTION_KW, GENERIC_KW, GOTO_KW, IF_KW,
    IN_KW, INTERFACE_KW, IS_KW, LIMITED_KW, LOOP_KW, MOD_KW, NEW_KW, NOT_KW, NULL_KW, OF_KW, OR_KW,
    OTHERS_KW, OUT_KW, PACKAGE_KW, PRAGMA_KW, PRIVATE_KW, PROCEDURE_KW, PROTECTED_KW, RAISE_KW,
    RANGE_KW, RECORD_KW, REM_KW, RENAMES_KW, REQUEUE_KW, RETURN_KW, REVERSE_KW, SELECT_KW,
    SEPARATE_KW, SOME_KW, SUBTYPE_KW, TAGGED_KW, TASK_KW, TERMINATE_KW, THEN_KW, TYPE_KW, UNTIL_KW,
    USE_KW, WHEN_KW, WHILE_KW, WITH_KW, XOR_KW,

    // Operadores y Delimitadores
    ASSIGNMENT_OP,      // :=
    DIFFERENCE_OP,      // /=
    EQUALITY_OP,        // =
    LESS_EQUAL_OP,      // <=
    GREATER_EQUAL_OP,   // >=
    RANGE_OP,           // ..
    PLUS_OP,            // +
    MINUS_OP,           // -
    MULTIPLY_OP,        // *
    DIVIDE_OP,          // /
    ATTRIBUTE_OP,       // '
    CONCATENATION_OP,   // &

    DOUBLE_ARROW,       // =>
    PAREN_LEFT,         // (
    PAREN_RIGHT,        // )
    COLON,              // :
    SEMICOLON,          // ;
    COMMA,              // ,
    DOT,                // .
    LESS_THAN,          // <
    GREATER_THAN,       // >

    // Otros
    COMMENT,
    WHITESPACE,
    ERROR
}