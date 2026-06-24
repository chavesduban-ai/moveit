package com.movelt.model;

import com.movelt.model.enums.EstadoPago;
import com.movelt.model.enums.EstadoPedido;
import com.movelt.model.enums.MetodoPago;
import com.movelt.model.enums.Servicio;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario cliente;

    private String recogida;
    private String entrega;
    private String destinatario;
    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(precision = 10, scale = 2)
    private BigDecimal peso = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private Servicio servicio = Servicio.standard;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado = EstadoPedido.pendiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor")
    private Usuario repartidorAsignado;

    @Column(precision = 2, scale = 1)
    private BigDecimal calificacion = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago = MetodoPago.efectivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago")
    private EstadoPago estadoPago = EstadoPago.pendiente;

    @Column(name = "distancia_km", precision = 8, scale = 3)
    private BigDecimal distanciaKm;

    @Column(name = "eta_minutos")
    private Integer etaMinutos;

    @Column(name = "zona_recogida")
    private String zonaRecogida;

    @Column(name = "zona_entrega")
    private String zonaEntrega;

    @Column(name = "ganancia_plataforma", precision = 10, scale = 2)
    private BigDecimal gananciaPlataforma = BigDecimal.ZERO;

    @Column(name = "ganancia_repartidor", precision = 10, scale = 2)
    private BigDecimal gananciaRepartidor = BigDecimal.ZERO;

    public Pedido() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }
    public String getRecogida() { return recogida; }
    public void setRecogida(String recogida) { this.recogida = recogida; }
    public String getEntrega() { return entrega; }
    public void setEntrega(String entrega) { this.entrega = entrega; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }
    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public Usuario getRepartidorAsignado() { return repartidorAsignado; }
    public void setRepartidorAsignado(Usuario r) { this.repartidorAsignado = r; }
    public BigDecimal getCalificacion() { return calificacion; }
    public void setCalificacion(BigDecimal calificacion) { this.calificacion = calificacion; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }
    public BigDecimal getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(BigDecimal distanciaKm) { this.distanciaKm = distanciaKm; }
    public Integer getEtaMinutos() { return etaMinutos; }
    public void setEtaMinutos(Integer etaMinutos) { this.etaMinutos = etaMinutos; }
    public String getZonaRecogida() { return zonaRecogida; }
    public void setZonaRecogida(String zonaRecogida) { this.zonaRecogida = zonaRecogida; }
    public String getZonaEntrega() { return zonaEntrega; }
    public void setZonaEntrega(String zonaEntrega) { this.zonaEntrega = zonaEntrega; }
    public BigDecimal getGananciaPlataforma() { return gananciaPlataforma; }
    public void setGananciaPlataforma(BigDecimal gananciaPlataforma) { this.gananciaPlataforma = gananciaPlataforma; }
    public BigDecimal getGananciaRepartidor() { return gananciaRepartidor; }
    public void setGananciaRepartidor(BigDecimal gananciaRepartidor) { this.gananciaRepartidor = gananciaRepartidor; }

    public static PedidoBuilder builder() { return new PedidoBuilder(); }
    public static class PedidoBuilder {
        private final Pedido p = new Pedido();
        public PedidoBuilder cliente(Usuario v) { p.cliente = v; return this; }
        public PedidoBuilder recogida(String v) { p.recogida = v; return this; }
        public PedidoBuilder entrega(String v) { p.entrega = v; return this; }
        public PedidoBuilder destinatario(String v) { p.destinatario = v; return this; }
        public PedidoBuilder telefono(String v) { p.telefono = v; return this; }
        public PedidoBuilder descripcion(String v) { p.descripcion = v; return this; }
        public PedidoBuilder peso(BigDecimal v) { p.peso = v; return this; }
        public PedidoBuilder servicio(Servicio v) { p.servicio = v; return this; }
        public PedidoBuilder observaciones(String v) { p.observaciones = v; return this; }
        public PedidoBuilder metodoPago(MetodoPago v) { p.metodoPago = v; return this; }
        public PedidoBuilder distanciaKm(BigDecimal v) { p.distanciaKm = v; return this; }
        public PedidoBuilder etaMinutos(Integer v) { p.etaMinutos = v; return this; }
        public PedidoBuilder zonaRecogida(String v) { p.zonaRecogida = v; return this; }
        public PedidoBuilder zonaEntrega(String v) { p.zonaEntrega = v; return this; }
        public Pedido build() { return p; }
    }
}
