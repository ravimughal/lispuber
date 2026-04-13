package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDateTime;


public record ViagemCreateDTO(Long clienteId, Long turnoId, Long pedidoId,
                              LocalDateTime dataInicio, Double kilometros) {}
