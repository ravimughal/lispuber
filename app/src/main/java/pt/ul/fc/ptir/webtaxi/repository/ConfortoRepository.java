package pt.ul.fc.ptir.webtaxi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.ul.fc.ptir.webtaxi.entity.Conforto;
import pt.ul.fc.ptir.webtaxi.entity.enums.NivelConforto;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfortoRepository extends JpaRepository<Conforto, Long> {
    Optional<Conforto> findByNivelConforto(NivelConforto nivelConforto);
}

