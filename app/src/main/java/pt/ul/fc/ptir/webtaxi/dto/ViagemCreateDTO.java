package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.embeddable.*;

public record ViagemCreateDTO(Long clienteId, Long turnoId, Long pedidoId,
                              Duracao duracao, Double kilometros, Integer numPassageiro) {}
