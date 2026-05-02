package pt.ul.fc.ptir.webtaxi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ul.fc.ptir.webtaxi.entity.Turno;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    // Restrição 6 — turnos do mesmo motorista que se intersetam com o intervalo dado
    @Query("""
        SELECT t FROM Turno t
        WHERE t.motorista.id = :motoristaId
          AND t.duracao.inicio < :fim
          AND t.duracao.fim > :inicio
    """)
    List<Turno> findTurnosMotoristaNoIntervalo(
        @Param("motoristaId") Long motoristaId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    // Restrição 6 — turnos com o mesmo táxi que se intersetam
    @Query("""
        SELECT t FROM Turno t
        WHERE t.taxi.id = :taxiId
          AND t.duracao.inicio < :fim
          AND t.duracao.fim > :inicio
    """)
    List<Turno> findTurnosTaxiNoIntervalo(
        @Param("taxiId") Long taxiId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    // Restrição 11 — períodos de carregamento (reabastecimentos) do táxi elétrico que se intersetam com o início do turno
    @Query("""
        SELECT COUNT(r) > 0 FROM Reabastecimento r
        WHERE r.turno.taxi.id = :taxiId
          AND r.duracao.inicio <= :inicio
          AND r.duracao.fim >= :inicio
    """)
    boolean isTaxiACarregarEm(
        @Param("taxiId") Long taxiId,
        @Param("inicio") LocalDateTime inicio
    );

    // needed to check no viagem goes beyond the new fim
    @Query("""
        SELECT COUNT(v) > 0 FROM Viagem v
        WHERE v.turno.id = :turnoId
        AND v.duracao.fim > :novoFim
    """)
    boolean existeViagemAlemDeFim(@Param("turnoId") Long turnoId, @Param("novoFim") LocalDateTime novoFim);

    @Query("""
        SELECT COUNT(t) > 0 from Turno t
        WHERE t.taxi.id = :taxiId
    """)
    boolean existeTaxiEmTurnos(@Param("taxiId") Long taxiId);

    @Query("""
        SELECT COUNT(t) > 0 from Turno t
        WHERE t.motorista.id = :motoristaId
    """)
    boolean existeMotoristaEmTurnos(@Param("motoristaId") Long motoristaId);

}