package pt.ul.fc.ptir.webtaxi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.ul.fc.ptir.webtaxi.entity.Pedido;
import pt.ul.fc.ptir.webtaxi.entity.enums.EstadoPedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstadoPedido(EstadoPedido estadoPedido);
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByMotoristaQueAceitou_Id(Long motoristaId);
}
