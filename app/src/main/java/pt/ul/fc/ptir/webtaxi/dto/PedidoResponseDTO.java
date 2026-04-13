package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDateTime;

import pt.ul.fc.ptir.webtaxi.entity.enums.EstadoPedido;

public record PedidoResponseDTO (Long id, ClienteResponseDTO cliente, ConfortoDTO conforto, Integer numPassageiros, 
                                 Double preco, String ruaOrigem, Double latitudeOrigem, Double longitudeOrigem,
                                 String ruaDestino, Double latitudeDestino, Double longitudeDestino,EstadoPedido estadoPedido,Long motoristaId,LocalDateTime dataSolicitacao) {}