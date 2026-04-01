package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;

import pt.ul.fc.ptir.webtaxi.entity.enums.NivelConforto;

@Entity
@Table(name = "conforto")
public class Conforto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double preco;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_conforto", nullable = false, unique = true)
    private NivelConforto nivelConforto;

    public Conforto(Double preco, NivelConforto nivelConforto) {
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
    
    public NivelConforto getNivelConforto() {
        return nivelConforto;
    }

    public void setNivelConforto(NivelConforto nivelConforto) {
        this.nivelConforto = nivelConforto;
    }
}
