package pt.ul.fc.ptir.webtaxi.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "configuracao_taxas")
public class ConfiguracaoTaxa {

    @Id
    private String chave;

    @Column(nullable = false)
    private Double valor;

    public ConfiguracaoTaxa() {}

    public ConfiguracaoTaxa(String chave, Double valor) {
        this.chave = chave;
        this.valor = valor;
    }
    
    public String getChave() { return chave; }
    public void setChave(String chave) { this.chave = chave; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
}