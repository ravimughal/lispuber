package pt.ul.fc.ptir.webtaxi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.ul.fc.ptir.webtaxi.entity.Motorista;

@Repository
public interface MotoristaRepository extends JpaRepository<Motorista,Long> {
    
    Optional<Motorista> findByEmail(String email);
    boolean existsByNumCartaConducao(String numcarta);
}
