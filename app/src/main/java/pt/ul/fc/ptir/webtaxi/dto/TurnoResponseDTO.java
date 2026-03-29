package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;



public record TurnoResponseDTO (Long id, Duracao duracao, MotoristaResponseDTO motorista, TaxiResponseDTO taxi) {}