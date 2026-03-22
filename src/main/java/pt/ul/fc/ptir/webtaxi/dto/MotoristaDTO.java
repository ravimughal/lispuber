package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDate;

import pt.ul.fc.ptir.webtaxi.entity.embeddable.Morada;
import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;

public record MotoristaDTO(Integer nif, String email, String senha, String nome, Genero genero,LocalDate dataNascimento,
    Morada morada,String numCartaConducao,LocalDate validadeCarta, String rua, String numero, String complemento, String codigoPostal) {
}
