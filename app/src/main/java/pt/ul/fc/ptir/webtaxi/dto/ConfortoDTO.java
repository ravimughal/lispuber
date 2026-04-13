package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.enums.NivelConforto;

public record ConfortoDTO (Double preco, NivelConforto nivelConforto) {}