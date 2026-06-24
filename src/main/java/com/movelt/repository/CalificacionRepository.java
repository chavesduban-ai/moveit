package com.movelt.repository;

import com.movelt.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    boolean existsByPedidoIdAndClienteUsuarioId(Long pedidoId, Long clienteId);

    @Query("SELECT AVG(c.calificacion) FROM Calificacion c WHERE c.repartidor.id = :repId")
    Double promedioByRepartidorId(@Param("repId") Long repartidorId);

    @Query("SELECT COUNT(c) FROM Calificacion c WHERE c.repartidor.id = :repId")
    Long countByRepartidorId(@Param("repId") Long repartidorId);
}
