package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;

public record ReabastecimentoResponseDTO (Long id, Long turnoId, Double quantidade, Double kilometros,
                                          Double eurosPagos, Duracao duracao) {}