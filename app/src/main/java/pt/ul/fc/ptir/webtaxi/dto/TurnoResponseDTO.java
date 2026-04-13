package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDateTime;

public record TurnoResponseDTO(Long id, Long motoristaId, Long taxiId, LocalDateTime inicio, LocalDateTime fim) {}