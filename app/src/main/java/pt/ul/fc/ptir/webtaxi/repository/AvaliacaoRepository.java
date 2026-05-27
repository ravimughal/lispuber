package pt.ul.fc.ptir.webtaxi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.ul.fc.ptir.webtaxi.dto.AvaliacaoMediaDTO;
import pt.ul.fc.ptir.webtaxi.entity.Avaliacao;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    boolean existsByViagemId(Long viagemId);

    Optional<Avaliacao> findByViagemId(Long viagemId);

    List<Avaliacao> findAllByMotoristaId(Long motoristaId);

    List<Avaliacao> findAllByClienteId(Long clienteId);

    @Query("""
        SELECT new pt.ul.fc.ptir.webtaxi.dto.AvaliacaoMediaDTO(
            a.motorista.id,
            a.motorista.nome,
            AVG(a.classificacao),
            COUNT(a)
        )
        FROM Avaliacao a
        WHERE a.motorista.id = :motoristaId
        GROUP BY a.motorista.id, a.motorista.nome
    """)
    Optional<AvaliacaoMediaDTO> findMediaByMotoristaId(@Param("motoristaId") Long motoristaId);
}