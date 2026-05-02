package pt.ul.fc.ptir.webtaxi.exception;

public class RecursoNaoRemovivelException extends RuntimeException {

    public RecursoNaoRemovivelException() {
        super("Recurso não pode ser removido.");
    }

    public RecursoNaoRemovivelException(String message) {
        super("Recurso não pode ser removido: " + message);
    }
}
