package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import pt.ul.fc.ptir.webtaxi.entity.enums.NivelConforto;

public record GetPrecoViagemDTO(
    Double latOrigem,
    Double lonOrigem,
    Double latDestino,
    Double lonDestino,
    NivelConforto conforto,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
    LocalDateTime data
) {} 
