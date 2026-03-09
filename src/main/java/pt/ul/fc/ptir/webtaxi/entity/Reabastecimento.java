package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class Reabastecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double qtdCombust;

    private Double kmTaxi;

    private Double eurosPagos;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    // Prob isto
    // @ManyToOne
    // @JoinColumn(name = "turno_id")
    // private Turno turno;

    public Reabastecimento() {
    }

    public Reabastecimento(Double qtdCombust, Double kmTaxi, Double eurosPagos, LocalDateTime dataInicio, LocalDateTime dataFim) { // , Turno turno
        this.qtdCombust = qtdCombust;
        this.kmTaxi = kmTaxi;
        this.eurosPagos = eurosPagos;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        // this.turno = turno;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getQtdCombust() {
        return qtdCombust;
    }

    public void setQtdCombust(Double qtdCombust) {
        this.qtdCombust = qtdCombust;
    }

    public Double getKmTaxi() {
        return kmTaxi;
    }

    public void setKmTaxi(Double kmTaxi) {
        this.kmTaxi = kmTaxi;
    }

    public Double getEurosPagos() {
        return eurosPagos;
    }

    public void setEurosPagos(Double eurosPagos) {
        this.eurosPagos = eurosPagos;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    // public Turno getTurno() {
    //     return turno;
    // }

    // public void setTurno(Turno turno) {
    //     this.turno = turno;
    // }
}
