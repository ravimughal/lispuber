package pt.ul.fc.ptir.webtaxi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.ul.fc.ptir.webtaxi.entity.Conforto;

@Repository
public interface ConfortoRepository extends JpaRepository<Conforto, Long> {}
