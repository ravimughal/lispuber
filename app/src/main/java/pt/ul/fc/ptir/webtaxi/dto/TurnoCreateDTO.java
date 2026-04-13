package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDateTime;

public record TurnoCreateDTO(Long motoristaId, LocalDateTime inicio, LocalDateTime fim) {}