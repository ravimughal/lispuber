package pt.ul.fc.ptir.webtaxi.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.ul.fc.ptir.webtaxi.entity.Viagem;

@Repository
public interface ViagemRepository extends JpaRepository<Viagem, Long> {

    boolean existsByPedidoId(Long pedidoId);
    List<Viagem> findByClienteId(Long clienteId);


    @Query("SELECT COUNT(v) > 0 FROM Viagem v " +
           "JOIN v.turno t " +
           "WHERE t.motorista.id = :motoristaId " +
           "AND (v.duracao.fim IS NULL OR v.duracao.fim > :inicioNova)")
    boolean isMotoristaOcupado(@Param("motoristaId") Long motoristaId, 
                               @Param("inicioNova") LocalDateTime inicioNova);

    // Verifica APENAS se o táxi está ocupado
    @Query("SELECT COUNT(v) > 0 FROM Viagem v " +
           "JOIN v.turno t " +
           "WHERE t.taxi.id = :taxiId " +
           "AND (v.duracao.fim IS NULL OR v.duracao.fim > :inicioNova)")
    boolean isTaxiOcupado(@Param("taxiId") Long taxiId, 
                          @Param("inicioNova") LocalDateTime inicioNova);

    @Query("""
        SELECT COUNT(t) > 0 from Viagem v
        WHERE t.taxi.id = :taxiId
    """)
    boolean existeTaxiEmViagens(@Param("taxiId") Long taxiId);

}
