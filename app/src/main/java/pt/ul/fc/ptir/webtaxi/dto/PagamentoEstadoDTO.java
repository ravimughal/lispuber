package pt.ul.fc.ptir.webtaxi.dto;

public record PagamentoEstadoDTO(
    Long pagamentoId,
    Long viagemId,
    String estado
) {}