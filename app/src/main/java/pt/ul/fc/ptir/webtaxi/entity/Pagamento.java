package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "viagem_id", nullable = false, unique = true)
    private Viagem viagem;

    @Column(name = "stripe_payment_id", nullable = false, unique = true)
    private String stripePaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPagamento estado = EstadoPagamento.PENDENTE;

    @Column(name = "valor", nullable = false)
    private Double valor;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public enum EstadoPagamento {
        PENDENTE, PAGO, FALHOU
    }

    // Getters and setters
    public Long getId() { return id; }
    public Viagem getViagem() { return viagem; }
    public void setViagem(Viagem viagem) { this.viagem = viagem; }
    public String getStripePaymentId() { return stripePaymentId; }
    public void setStripePaymentId(String stripePaymentId) { this.stripePaymentId = stripePaymentId; }
    public EstadoPagamento getEstado() { return estado; }
    public void setEstado(EstadoPagamento estado) { this.estado = estado; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}