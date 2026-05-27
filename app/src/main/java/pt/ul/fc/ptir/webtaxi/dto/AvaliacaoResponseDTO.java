package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDateTime;

public record AvaliacaoResponseDTO(
    Long id,
    Long viagemId,
    Long clienteId,
    String nomeCliente,
    Long motoristaId,
    String nomeMotorista,
    Integer classificacao,
    String comentario,
    LocalDateTime criadoEm
) {}