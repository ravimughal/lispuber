package pt.ul.fc.ptir.webtaxi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pt.ul.fc.ptir.webtaxi.entity.Fatura;

import java.util.List;

@Repository
public interface FaturaRepository extends JpaRepository<Fatura, Long> {

    boolean existsByViagemId(Long viagemId);

    @Query("SELECT MAX(f.numSequencial() FROM Fatura f")
    Long findMaxNumSequencial();

    List<Fatura> findByViagemTurnoMotoristaIdOrderByDataDesc(Long motoristaId);
}
