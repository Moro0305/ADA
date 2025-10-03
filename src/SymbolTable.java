import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    private final Stack<Map<String, Symbol>> scopeStack;

    public SymbolTable() {
        scopeStack = new Stack<>();
        enterScope(); // Global scope
    }

    public void enterScope() {
        scopeStack.push(new HashMap<>());
        System.out.println("Entering new scope. Current depth: " + scopeStack.size());
    }

    public void exitScope() {
        if (scopeStack.size() > 1) {
            scopeStack.pop();
            System.out.println("Exiting scope. Current depth: " + scopeStack.size());
        } else {
            System.err.println("Cannot exit global scope.");
        }
    }

    public void addSymbol(Symbol symbol) {
        if (scopeStack.isEmpty()) {
            enterScope();
        }
        // Check if symbol already exists in the current scope
        if (scopeStack.peek().containsKey(symbol.name)) {
            System.err.println("Semantic Error: Symbol '" + symbol.name + "' already declared in this scope.");
        } else {
            scopeStack.peek().put(symbol.name, symbol);
            System.out.println("Added symbol: " + symbol);
        }
    }

    public Symbol findSymbol(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).containsKey(name)) {
                return scopeStack.get(i).get(name);
            }
        }
        return null; // Not found
    }
}

