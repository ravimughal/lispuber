package pt.ul.fc.ptir.webtaxi.exception;

public class TipoDeUtilizadorInvalidoException extends RuntimeException {

    public TipoDeUtilizadorInvalidoException() {
        super("Tipo de utilizador desconhecido.");
    }

    public TipoDeUtilizadorInvalidoException(String mensagem) {
        super("Tipo de utilizador desconhecido: " + mensagem);
    }
}
