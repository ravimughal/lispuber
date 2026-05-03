package pt.ul.fc.ptir.webtaxi.exception;

public class TipoDeUtilizadorInvalidoException extends RuntimeException {

    public TipoDeUtilizadorInvalidoException() {
        super("Tipo de utilizador desconhecido.");
    }

    public TipoDeUtilizadorInvalidoException(String message) {
        super("Tipo de utilizador desconhecido: " + message);
    }
}
