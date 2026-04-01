package pt.ul.fc.ptir.webtaxi.dto;

public record PedidoCreateDTO (Long clienteId, Long confortoId, Integer numPassageiros,
                               String ruaOrigem, Double latitudeOrigem, Double longitudeOrigem, 
                               String ruaDestino, Double latitudeDestino, Double longitudeDestino) {}