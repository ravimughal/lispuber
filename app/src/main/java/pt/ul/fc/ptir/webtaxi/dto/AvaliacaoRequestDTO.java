package pt.ul.fc.ptir.webtaxi.dto;

public record AvaliacaoRequestDTO(
    Long viagemId,
    Integer classificacao,
    String comentario
) {}