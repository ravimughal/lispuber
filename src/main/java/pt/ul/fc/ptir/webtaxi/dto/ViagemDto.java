package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.Cliente;
import pt.ul.fc.ptir.webtaxi.entity.Fatura;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.*;

public record ViagemDto(Long id, Morada moradaEntrada, Morada moradaSaida, Duracao duracao, Double kmPercorridos,Integer numPassageiro, Double preco, Cliente cliente, Fatura fatura) {
}
