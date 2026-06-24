package com.movelt.service;

import com.movelt.model.*;
import com.movelt.repository.CalificacionRepository;
import com.movelt.repository.PedidoRepository;
import org.springframework.stereotype.Service;

@Service
public class CalificacionService {

    private final CalificacionRepository repo;
    private final PedidoRepository pedidoRepo;

    public CalificacionService(CalificacionRepository repo, PedidoRepository pedidoRepo) {
        this.repo = repo;
        this.pedidoRepo = pedidoRepo;
    }

    public void calificar(Long pedidoId, Long clienteId, int valor) {
        if (valor < 1 || valor > 5) throw new RuntimeException("Calificación debe ser entre 1 y 5");
        if (repo.existsByPedidoIdAndClienteUsuarioId(pedidoId, clienteId))
            throw new RuntimeException("Ya calificaste este pedido");

        Pedido pedido = pedidoRepo.findById(pedidoId).orElseThrow();
        if (pedido.getRepartidorAsignado() == null)
            throw new RuntimeException("No hay repartidor asignado");

        Calificacion c = Calificacion.builder()
            .pedido(pedido)
            .repartidor(pedido.getRepartidorAsignado())
            .clienteUsuario(pedido.getCliente())
            .calificacion(valor)
            .build();
        repo.save(c);

        pedido.setCalificacion(java.math.BigDecimal.valueOf(valor));
        pedidoRepo.save(pedido);
    }

    public Double promedioRepartidor(Long repartidorId) {
        Double avg = repo.promedioByRepartidorId(repartidorId);
        return avg != null ? avg : 0.0;
    }

    public Long totalCalificaciones(Long repartidorId) {
        return repo.countByRepartidorId(repartidorId);
    }
}
