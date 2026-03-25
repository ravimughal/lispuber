package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;

public record FinalizarViagemDTO (Duracao duracao,
                                  Double kmPercorridos,
                                  Double preco) {
}
