package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fatura")
public class Fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "num_sequencial", nullable = false, unique = true)
    private Long numSequencial;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    @OneToOne
    @JoinColumn(name = "viagem_id", nullable = false, unique = true)
    private Viagem viagem;



    public Fatura(){}

    public Fatura(Long numSequencial, LocalDateTime data, Viagem viagem) {
        this.numSequencial = numSequencial;
        this.data = data;
        this.viagem = viagem;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getNumSequencial() {
        return numSequencial;
    }

    public void setNumSequencial(Long numSequencial) {
        this.numSequencial = numSequencial;
    }


    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }


    public Viagem getViagem() {
        return viagem;
    }

    public void setViagem(Viagem viagem) {
        this.viagem = viagem;
    }
}
