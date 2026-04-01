package pt.ul.fc.ptir.webtaxi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.ul.fc.ptir.webtaxi.entity.Gestor;

@Repository
public interface GestorRepository extends JpaRepository<Gestor, Long> {
        Optional<Gestor> findByEmail(String email);
}
