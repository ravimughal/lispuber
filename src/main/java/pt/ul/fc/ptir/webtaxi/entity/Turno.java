package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private Duracao duracao;

    @Column(nullable = false)
    private Motorista motorista;

    @Column(nullable = false)
    private Taxi taxi;

    private List<Reabastecimento> reabastecimentos;

    private List<Viagem> viagens;

}
