package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;

@Entity
public class Conforto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private Double preco;

    @Column(nullable = false)
    private String nivelConforto;

    private Conforto() {}

    public Conforto(Double preco, String nivelConforto) {
        this.preco = preco;
        this.nivelConforto = nivelConforto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public String getNivelConforto() {
        return nivelConforto;
    }

    public void setNivelConforto(String nivelConforto) {
        this.nivelConforto = nivelConforto;
    }
}
