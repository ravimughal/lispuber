package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne 
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne 
    @JoinColumn(name = "conforto_id", nullable = false)
    private Conforto conforto;

    @Column(name = "num_passageiros", nullable = false)
    private Integer numPassageiros;

    @Column(nullable = false)
    private Double preco;

    @Column(name = "rua_origem", nullable = false, length = 255)
    private String ruaOrigem;

    @Column(name = "latitude_origem", nullable = false)
    private Double latitudeOrigem;

    @Column(name = "longitude_origem", nullable = false)
    private Double longitudeOrigem; 

    @Column(name = "rua_destino", nullable = false, length = 255)
    private String ruaDestino;

    @Column(name = "latitude_destino", nullable = false)
    private Double latitudeDestino;

    @Column(name = "longitude_destino", nullable = false)
    private Double longitudeDestino;



    public Pedido() {}

    public Pedido(Cliente cliente, Conforto conforto, Integer numPassageiros, Double preco, String ruaOrigem, 
                  Double latitudeOrigem, Double longitudeOrigem, String ruaDestino, Double latitudeDestino, 
                  Double longitudeDestino) {
        this.conforto = conforto;
        this.cliente = cliente;
        this.preco = preco;
        this.numPassageiros = numPassageiros;
        this.ruaOrigem = ruaOrigem;
        this.latitudeOrigem = latitudeOrigem;
        this.longitudeOrigem = longitudeOrigem;
        this.ruaDestino = ruaDestino;
        this.latitudeDestino = latitudeDestino;
        this.longitudeDestino = longitudeDestino;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    
    public Conforto getConforto() {
        return conforto;
    }

    public void setConforto(Conforto conforto) {
        this.conforto = conforto;
    }


    public Integer getNumPassageiros() {
        return numPassageiros;
    }

    public void setNumPassageiros(Integer numPassageiros) {
        this.numPassageiros = numPassageiros;
    }

    
    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    
    public String getRuaOrigem() {
        return ruaOrigem;
    }

    public void setRuaOrigem(String ruaOrigem) {
        this.ruaOrigem = ruaOrigem;
    }


    public Double getLatitudeOrigem() {
        return latitudeOrigem;
    }

    public void setLatitudeOrigem(Double latitudeOrigem) {
        this.latitudeOrigem = latitudeOrigem;
    }


    public Double getLongitudeOrigem() {
        return longitudeOrigem;
    }

    public void setLongitudeOrigem(Double longitudeOrigem) {
        this.longitudeOrigem = longitudeOrigem;
    }


    public String getRuaDestino() {
        return ruaDestino;
    }

    public void setRuaDestino(String ruaDestino) {
        this.ruaDestino = ruaDestino;
    }


    public Double getLatitudeDestino() {
        return latitudeDestino;
    }

    public void setLatitudeDestino(Double latitudeDestino) {
        this.latitudeDestino = latitudeDestino;
    }


    public Double getLongitudeDestino() {
        return longitudeDestino;
    }

    public void setLongitudeDestino(Double longitudeDestino) {
        this.longitudeDestino = longitudeDestino;
    }
}
