package pt.ul.fc.ptir.webtaxi.exception;

public class RecursoNaoEditavelException extends RuntimeException {

    public RecursoNaoEditavelException() {
        super("Recurso não editável.");
    }

    public RecursoNaoEditavelException(String mensagem) {
        super("Recurso não editável: " + mensagem);
    }
}
