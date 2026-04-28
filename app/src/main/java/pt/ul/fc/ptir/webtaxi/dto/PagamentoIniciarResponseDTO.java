package pt.ul.fc.ptir.webtaxi.dto;

public record PagamentoIniciarResponseDTO(
    Long pagamentoId,
    String estado,
    String clientSecret,
    String publishableKey
) {}