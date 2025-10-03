public class Symbol {
    String name;
    String category; // e.g., "variable", "procedure", "function", "type"
    String type;     // e.g., "Integer", "Boolean", or the name of a custom type

    public Symbol(String name, String category, String type) {
        this.name = name;
        this.category = category;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
