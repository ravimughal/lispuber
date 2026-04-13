package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDateTime;

public record ViagemResponseDTO(Long id, ClienteResponseDTO cliente, TurnoResponseDTO turno, PedidoResponseDTO pedido,
                               LocalDateTime inicio,LocalDateTime fim, Double kilometros) {}
