package com.movelt.service;

import com.movelt.model.Pedido;
import com.movelt.model.Usuario;
import com.movelt.model.enums.EstadoPedido;
import com.movelt.model.enums.Servicio;
import com.movelt.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository repo;
    private final ComisionService comisionService;

    public PedidoService(PedidoRepository repo, ComisionService comisionService) {
        this.repo = repo;
        this.comisionService = comisionService;
    }

    public Pedido crear(Pedido pedido) {
        pedido.setEstado(EstadoPedido.pendiente);
        pedido.setFecha(LocalDateTime.now());
        comisionService.calcularGanancias(pedido);
        return repo.save(pedido);
    }

    public Pedido guardar(Pedido pedido) {
        if (pedido.getGananciaPlataforma() == null
                || pedido.getGananciaPlataforma().signum() == 0) {
            comisionService.calcularGanancias(pedido);
        }
        return repo.save(pedido);
    }

    public Pedido buscarPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    public Pedido ultimoPedidoCliente(Long clienteId) {
        return repo.findFirstByClienteIdOrderByIdDesc(clienteId);
    }

    public List<Pedido> pedidosCliente(Long clienteId) {
        return repo.findByClienteIdOrderByIdDesc(clienteId);
    }

    public List<Pedido> pedidosEntregadosCliente(Long clienteId) {
        return repo.findEntregadosByClienteWithRepartidor(clienteId);
    }

    public List<Pedido> pedidosPendientes() {
        return repo.findByEstadoOrderByFechaAsc(EstadoPedido.pendiente);
    }

    public List<Pedido> pedidosAsignados(Long repartidorId) {
        return repo.findByRepartidorAsignadoIdAndEstadoInOrderByFechaAsc(
            repartidorId, List.of(EstadoPedido.aceptado, EstadoPedido.en_curso));
    }

    public List<Pedido> historialRepartidor(Long repartidorId) {
        return repo.findByRepartidorAsignadoIdAndEstadoOrderByFechaDesc(repartidorId, EstadoPedido.entregado);
    }

    public void aceptarPedido(Long pedidoId, Usuario repartidor) {
        Pedido p = buscarPorId(pedidoId);
        if (p.getEstado() != EstadoPedido.pendiente)
            throw new RuntimeException("El pedido ya no está pendiente");
        p.setEstado(EstadoPedido.aceptado);
        p.setRepartidorAsignado(repartidor);
        repo.save(p);
    }

    public void entregarPedido(Long pedidoId, Long repartidorId) {
        Pedido p = buscarPorId(pedidoId);
        if (p.getRepartidorAsignado() == null || !p.getRepartidorAsignado().getId().equals(repartidorId))
            throw new RuntimeException("No eres el repartidor asignado");
        p.setEstado(EstadoPedido.entregado);
        repo.save(p);
    }

    public List<Pedido> listarTodos() { return repo.findAllWithRelations(); }

    public BigDecimal totalGananciaPlataforma() {
        return repo.findAll().stream()
            .filter(p -> p.getEstado() == EstadoPedido.entregado)
            .map(Pedido::getGananciaPlataforma)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalGananciaRepartidores() {
        return repo.findAll().stream()
            .filter(p -> p.getEstado() == EstadoPedido.entregado)
            .map(Pedido::getGananciaRepartidor)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public long contarActivos() { return repo.countByEstadoNot(EstadoPedido.entregado); }
    public long contarTotal() { return repo.count(); }

    public Map<String, Object> estadisticasRepartidor(Long repartidorId) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();

        List<Pedido> entregados = repo.findEntregadosByRepartidorDesde(repartidorId, inicioMes);

        int entregasHoy = 0, totalMes = entregados.size();
        int gananciasHoy = 0, gananciasSemana = 0, gananciasMes = 0;

        LocalDate inicioSemana = hoy.with(java.time.DayOfWeek.MONDAY);

        for (Pedido p : entregados) {
            int precio = p.getServicio().getPrecio();
            LocalDate fechaPedido = p.getFecha().toLocalDate();
            gananciasMes += precio;

            if (!fechaPedido.isBefore(inicioSemana)) gananciasSemana += precio;
            if (fechaPedido.equals(hoy)) {
                entregasHoy++;
                gananciasHoy += precio;
            }
        }

        return Map.of(
            "entregasHoy", entregasHoy, "totalMes", totalMes,
            "gananciasHoy", gananciasHoy, "gananciasSemana", gananciasSemana,
            "gananciasMes", gananciasMes
        );
    }

    public Map<String, Object> estadisticasCliente(Long clienteId) {
        List<Pedido> pedidos = pedidosCliente(clienteId);
        int total = pedidos.size();
        int entregados = (int) pedidos.stream().filter(p -> p.getEstado() == EstadoPedido.entregado).count();
        int gasto = pedidos.stream().mapToInt(p -> p.getServicio().getPrecio()).sum();
        return Map.of("total", total, "entregados", entregados, "gasto", gasto);
    }
}
