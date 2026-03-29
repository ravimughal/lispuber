package pt.ul.fc.ptir.webtaxi.dto;



public record PedidoResponseDTO (Long id, ClienteResponseDTO cliente, ConfortoDTO conforto, Integer numPassageiros, 
                                 Double preco, String ruaOrigem, Double latitudeOrigem, Double longitudeOrigem,
                                 String ruaDestino, Double latitudeDestino, Double longitudeDestino) {}