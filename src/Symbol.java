/**
 * Representa un identificador en código Ada, como variable, función, procedimiento o tipo.
 * Contiene nombre, categoría y tipo para el análisis semántico.
 */
public class Symbol {
    /** Nombre del identificador (ej., variable, función, procedimiento) */
    String name;
    /** Categoría del símbolo (ej., "variable", "procedure", "function", "type") */
    String category;
    /** Tipo de dato o tipo de retorno (ej., "Integer", "Boolean" o nombre de tipo) */
    String type;

    /**
     * Construye un Symbol con nombre, categoría y tipo.
     * @param name Nombre del identificador
     * @param category Categoría del símbolo
     * @param type Tipo de dato o retorno
     */
    public Symbol(String name, String category, String type) {
        this.name = name;
        this.category = category;
        this.type = type;
    }

    /**
     * Representación en cadena para depuración.
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
