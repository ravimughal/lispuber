package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.Embeddable;

import java.time.Duration;
import java.time.LocalDateTime;

@Embeddable
public class Duracao {

    private LocalDateTime inicio;
    private LocalDateTime fim;

    public Duracao() {}

    public Duracao(LocalDateTime inicio, LocalDateTime fim) throws Exception {
        if (inicio.isAfter(fim)) throw new Exception(); // TODO: mudar para uma exceção personalizada

        this.inicio = inicio;
        this.fim = fim;
    }

    public long getTotalEmMinutos() {
        return Duration.between(inicio, fim).toMinutes();
    }

    public long getTotalEmHoras() {
        return Duration.between(inicio, fim).toHours();
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }
}
