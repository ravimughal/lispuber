package pt.ul.fc.ptir.webtaxi.exception;

public class DataInicioDepoisDoFimException extends RuntimeException {
    public DataInicioDepoisDoFimException() {
        super("Erro: Data de início não pode ser depois da data de fim.");
    }

    public DataInicioDepoisDoFimException(String mensagem) {
        super(mensagem);
    }
}
