package pt.ul.fc.ptir.webtaxi.dto;

public record AvaliacaoMediaDTO(
    Long motoristaId,
    String nomeMotorista,
    Double mediaClassificacao,
    Long totalAvaliacoes
) {}