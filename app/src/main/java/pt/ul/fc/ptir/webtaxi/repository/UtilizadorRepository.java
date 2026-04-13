package pt.ul.fc.ptir.webtaxi.repository;

import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.ptir.webtaxi.entity.*;

@Repository
public interface UtilizadorRepository extends JpaRepository<Utilizador,Long>{

    Optional<Utilizador> findByEmail(String email);
}
