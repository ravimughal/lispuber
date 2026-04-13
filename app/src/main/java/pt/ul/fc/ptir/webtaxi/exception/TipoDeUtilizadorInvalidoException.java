package pt.ul.fc.ptir.webtaxi.exception;

public class TipoDeUtilizadorInvalidoException extends RuntimeException {

    public TipoDeUtilizadorInvalidoException() {
        super("Erro: Tipo de utilizador desconhecido.");
    }

    public TipoDeUtilizadorInvalidoException(String message) {
        super(message);
    }
}
