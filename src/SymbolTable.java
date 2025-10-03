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
     * Adds a symbol to the current scope. Warns if already declared in this scope.
     * @param symbol Symbol to add
     */
    public void addSymbol(Symbol symbol) {
        if (scopeStack.isEmpty()) {
            enterScope();
        }
        Map<String, Symbol> currentScope = scopeStack.peek();
        if (currentScope.containsKey(symbol.name)) {
            System.err.println("Semantic Error: Symbol '" + symbol.name + "' already declared in this scope.");
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
}
