package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;

import pt.ul.fc.ptir.webtaxi.entity.enums.TipoMotor;

@Entity
@Table(name = "taxi")
public class Taxi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 31)
    private String matricula;

    @Column(nullable = false, length = 63)
    private String modelo;

    @Column(nullable = false, length = 63)
    private String marca;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_motor", nullable = false, length = 10)
    private TipoMotor tipoMotor;

    @Column(name = "ano_compra", nullable = false)
    private Integer anoCompra;

    @ManyToOne
    @JoinColumn(name = "conforto_id")
    private Conforto conforto;

    @Column(nullable = false)
    private Double kilometros;


    public Taxi() {}

    public Taxi(String matricula, String modelo, String marca, TipoMotor tipoMotor, Integer anoCompra, Conforto conforto, Double kilometros) {
        this.matricula = matricula;
        this.modelo = modelo;
        this.marca = marca;
        this.tipoMotor = tipoMotor;
        this.anoCompra = anoCompra;
        this.conforto = conforto;
        this.kilometros = kilometros;
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


    public TipoMotor getTipoMotor() {
        return tipoMotor;
    }

    public void setTipoMotor(TipoMotor tipoMotor) {
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

    public Double getKilometros() {
        return kilometros;
    }

    public void setKilometros(Double kilometros) {
        this.kilometros = kilometros;
    }
}
