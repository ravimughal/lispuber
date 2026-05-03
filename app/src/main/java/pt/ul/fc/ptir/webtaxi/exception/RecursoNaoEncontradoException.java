package pt.ul.fc.ptir.webtaxi.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException() {
        super("Recurso não encontrado");
    }

    public RecursoNaoEncontradoException(String message) {
        super("Recurso não encontrado: " + message);
    }
}
