package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Conforto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double price;

    private String confortLevel;

    private Conforto() {}

    public Conforto(Double price, String confortLevel) {
        this.price = price;
        this.confortLevel = confortLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getConfortLevel() {
        return confortLevel;
    }

    public void setConfortLevel(String confortLevel) {
        this.confortLevel = confortLevel;
    }


}
