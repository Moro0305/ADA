/**
 * Represents an identifier in Ada code, such as a variable, function, procedure, or type.
 * Stores the name, category, and type information for semantic analysis.
 */
public class Symbol {
    /** Identifier name (e.g., variable, function, procedure) */
    String name;
    /** Category of the symbol (e.g., "variable", "procedure", "function", "type") */
    String category;
    /** Data type or return type (e.g., "Integer", "Boolean", or custom type name) */
    String type;

    /**
     * Constructs a Symbol with the given name, category, and type.
     * @param name Identifier name
     * @param category Symbol category
     * @param type Data type or return type
     */
    public Symbol(String name, String category, String type) {
        this.name = name;
        this.category = category;
        this.type = type;
    }

    /**
     * Returns a string representation of the symbol for debugging.
     */
    @Override
    public String toString() {
        return "Symbol{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
