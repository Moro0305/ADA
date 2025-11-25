/**
 * Excepción lanzada cuando se detecta un error de sintaxis durante el análisis.
 * Contiene un mensaje descriptivo del error para reporte.
 */
public class SyntaxException extends Exception {
    /**
     * Construye la excepción con un mensaje detallado del error.
     * @param message Descripción del error de sintaxis
     */
    public SyntaxException(String message) {
        super(message);
    }
}