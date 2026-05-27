package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacao")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", nullable = false, unique = true)
    private Viagem viagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id", nullable = false)
    private Motorista motorista;

    @Column(nullable = false)
    private Integer classificacao;

    @Column(length = 500)
    private String comentario;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Viagem getViagem() { return viagem; }
    public Cliente getCliente() { return cliente; }
    public Motorista getMotorista() { return motorista; }
    public Integer getClassificacao() { return classificacao; }
    public String getComentario() { return comentario; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setViagem(Viagem viagem) { this.viagem = viagem; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public void setMotorista(Motorista motorista) { this.motorista = motorista; }
    public void setClassificacao(Integer classificacao) { this.classificacao = classificacao; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}