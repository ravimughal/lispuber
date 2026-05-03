package pt.ul.fc.ptir.webtaxi.exception;

public class DataInicioDepoisDoFimException extends RuntimeException {
    public DataInicioDepoisDoFimException() {
        super("Data de início não pode ser depois da data de fim.");
    }

    public DataInicioDepoisDoFimException(String message) {
        super("Data de início não pode ser depois da data de fim: " + message);
    }
}
