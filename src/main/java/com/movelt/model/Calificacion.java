package com.movelt.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "calificaciones",
       uniqueConstraints = @UniqueConstraint(columnNames = {"pedido_id", "cliente_id"}))
public class Calificacion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Usuario repartidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario clienteUsuario;

    @Column(nullable = false)
    private Integer calificacion;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    private LocalDateTime fecha = LocalDateTime.now();

    public Calificacion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public Usuario getRepartidor() { return repartidor; }
    public void setRepartidor(Usuario repartidor) { this.repartidor = repartidor; }
    public Usuario getClienteUsuario() { return clienteUsuario; }
    public void setClienteUsuario(Usuario clienteUsuario) { this.clienteUsuario = clienteUsuario; }
    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public static CalificacionBuilder builder() { return new CalificacionBuilder(); }
    public static class CalificacionBuilder {
        private final Calificacion c = new Calificacion();
        public CalificacionBuilder pedido(Pedido v) { c.pedido = v; return this; }
        public CalificacionBuilder repartidor(Usuario v) { c.repartidor = v; return this; }
        public CalificacionBuilder clienteUsuario(Usuario v) { c.clienteUsuario = v; return this; }
        public CalificacionBuilder calificacion(Integer v) { c.calificacion = v; return this; }
        public Calificacion build() { return c; }
    }
}
