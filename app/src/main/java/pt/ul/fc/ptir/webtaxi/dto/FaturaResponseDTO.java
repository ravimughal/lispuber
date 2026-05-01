package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDateTime;

public record FaturaResponseDTO (Long id, Long numSequencial, LocalDateTime data, Long viagemId, Double preco) {}