package pt.ul.fc.ptir.webtaxi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.ptir.webtaxi.entity.Taxi;

public interface TaxiRepository extends JpaRepository<Taxi, Long> {

    boolean existsByMatricula(String matricula);
}
