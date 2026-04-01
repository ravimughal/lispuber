package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;

public record TurnoCreateDTO (Duracao duracao, Long motoristaId, Long taxiId) {}