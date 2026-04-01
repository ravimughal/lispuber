package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.embeddable.*;

public record ViagemResponseDTO(Long id, ClienteResponseDTO cliente, TurnoResponseDTO turno, PedidoResponseDTO pedido,
                                Duracao duracao, Double kilometros, Integer numPassageiro) {}
