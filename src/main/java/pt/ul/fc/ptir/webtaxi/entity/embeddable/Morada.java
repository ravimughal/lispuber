package pt.ul.fc.ptir.webtaxi.entity.embeddable;

import jakarta.persistence.Embeddable;

@Embeddable
public class Morada {

    private String rua;

    private String numero;

    private String complemento;

    private String codigoPostal;

    public Morada() {} // Preciso para hibernate conseguir instanciar

    public Morada(String rua, String numero, String codigoPostal) {
        this.rua = rua;
        this.numero = numero;
        this.codigoPostal = codigoPostal;
    }

    public Morada(String rua, String numero, String complemento, String codigoPostal) {
        this.rua = rua;
        this.numero = numero;
        this.complemento = complemento;
        this.codigoPostal = codigoPostal;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String toString() {
        return rua + ", " + numero + (complemento == null ? "" : ", " + complemento) + ", " + codigoPostal;
    }

}
