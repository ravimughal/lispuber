package pt.ul.fc.ptir.webtaxi.repository;

import pt.ul.fc.ptir.webtaxi.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    Optional<Pagamento> findByViagemId(Long viagemId);
    Optional<Pagamento> findByStripePaymentId(String stripePaymentId);
}