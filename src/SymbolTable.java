/**
 * SymbolTable manages nested scopes for Ada identifiers using a stack of maps.
 * Each scope is represented by a map from identifier names to Symbol objects.
 * Provides methods to enter/exit scopes, add symbols, and lookup symbols.
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    /** Stack of scopes, each scope is a map of identifier names to Symbol objects. */
    private final Stack<Map<String, Symbol>> scopeStack;

    /**
     * Constructs a SymbolTable and initializes the global scope.
     */
    public SymbolTable() {
        scopeStack = new Stack<>();
        enterScope(); // Global scope
    }

    /**
     * Enters a new scope (e.g., function, procedure, block).
     */
    public void enterScope() {
        scopeStack.push(new HashMap<>());
        System.out.println("Entering new scope. Current depth: " + scopeStack.size());
    }

    /**
     * Exits the current scope, unless it's the global scope.
     */
    public void exitScope() {
        if (scopeStack.size() > 1) {
            scopeStack.pop();
            System.out.println("Exiting scope. Current depth: " + scopeStack.size());
        } else {
            System.err.println("Cannot exit global scope.");
        }
    }

    /**
     * Adds a symbol to the current scope. Throws SyntaxException if already declared in this scope.
     * @param symbol Symbol to add
     * @throws SyntaxException if the symbol is already declared in the current scope
     */
    public void addSymbol(Symbol symbol) throws SyntaxException {
        if (scopeStack.isEmpty()) {
            enterScope();
        }
        Map<String, Symbol> currentScope = scopeStack.peek();
        if (currentScope.containsKey(symbol.name)) {
            throw new SyntaxException("Error Semántico: La variable '" + symbol.name +
                "' ya fue declarada en este alcance (scope).");
        } else {
            currentScope.put(symbol.name, symbol);
            System.out.println("Added symbol: " + symbol);
        }
    }

    /**
     * Finds a symbol by name, searching from innermost to outermost scope.
     * @param name Identifier name to search for
     * @return Symbol if found, null otherwise
     */
    public Symbol findSymbol(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, Symbol> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null; // Not found
    }

    /**
     * Verifies that a symbol has been declared in the current scope or any parent scope.
     * Throws SyntaxException if the symbol is not declared.
     * @param name Identifier name to verify
     * @param line Line number where the identifier is used (for error reporting)
     * @param column Column number where the identifier is used (for error reporting)
     * @throws SyntaxException if the symbol has not been declared
     */
    public void verifySymbolDeclared(String name, int line, int column) throws SyntaxException {
        Symbol symbol = findSymbol(name);
        if (symbol == null) {
            throw new SyntaxException("Error Semántico: La variable '" + name +
                "' se utiliza en la línea " + line + ", columna " + column +
                " pero no ha sido declarada en este alcance ni en ningún alcance superior.");
        }
        System.out.println("Symbol '" + name + "' verified at line " + line + ", column " + column);
    }
}
