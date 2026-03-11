package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Taxi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String matricula;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String tipoMotor;

    @Column(nullable = false)
    private Integer anoCompra;

    @ManyToOne
    @JoinColumn(name = "conforto_id")
    private Conforto conforto;

    @OneToMany(mappedBy = "taxi")
    private Set<Turno> turnos;

    public Taxi() {}

    public Taxi(String matricula, String modelo, String marca, String tipoMotor, Integer anoCompra, Conforto conforto) {
        this.matricula = matricula;
        this.modelo = modelo;
        this.marca = marca;
        this.tipoMotor = tipoMotor;
        this.anoCompra = anoCompra;
        this.conforto = conforto;
        this.turnos = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getTipoMotor() {
        return tipoMotor;
    }

    public void setTipoMotor(String tipoMotor) {
        this.tipoMotor = tipoMotor;
    }

    public Integer getAnoCompra() {
        return anoCompra;
    }

    public void setAnoCompra(Integer anoCompra) {
        this.anoCompra = anoCompra;
    }

    public Conforto getConforto() {
        return conforto;
    }

    public void setConforto(Conforto conforto) {
        this.conforto = conforto;
    }

    public Set<Turno> getTurnos() {
        return turnos;
    }

    public void setTurnos(Set<Turno> turnos) {
        this.turnos = turnos;
    }
}
