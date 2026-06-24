package com.movelt.repository;

import com.movelt.model.Pedido;
import com.movelt.model.enums.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteIdOrderByIdDesc(Long clienteId);
    Pedido findFirstByClienteIdOrderByIdDesc(Long clienteId);
    List<Pedido> findByClienteIdAndEstadoOrderByIdDesc(Long clienteId, EstadoPedido estado);

    List<Pedido> findByEstadoOrderByFechaAsc(EstadoPedido estado);
    List<Pedido> findByRepartidorAsignadoIdAndEstadoInOrderByFechaAsc(Long repartidorId, List<EstadoPedido> estados);
    List<Pedido> findByRepartidorAsignadoIdAndEstadoOrderByFechaDesc(Long repartidorId, EstadoPedido estado);

    @Query("SELECT p FROM Pedido p WHERE p.repartidorAsignado.id = :repId AND p.estado = 'entregado' AND p.fecha >= :desde")
    List<Pedido> findEntregadosByRepartidorDesde(@Param("repId") Long repId, @Param("desde") LocalDateTime desde);

    long countByEstadoNot(EstadoPedido estado);

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.repartidorAsignado WHERE p.cliente.id = :clienteId AND p.estado = 'entregado' ORDER BY p.id DESC")
    List<Pedido> findEntregadosByClienteWithRepartidor(@Param("clienteId") Long clienteId);

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.repartidorAsignado LEFT JOIN FETCH p.cliente ORDER BY p.fecha DESC")
    List<Pedido> findAllWithRelations();
}
