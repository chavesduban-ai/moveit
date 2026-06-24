package com.movelt.model;

import com.movelt.model.enums.EstadoUsuario;
import com.movelt.model.enums.Rol;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String usuario;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 20)
    private String telefono = "";

    @Column(nullable = false)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.cliente;

    private String ciudad = "";
    private String direccion = "";

    @Enumerated(EnumType.STRING)
    private EstadoUsuario estado = EstadoUsuario.activo;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Usuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime f) { this.fechaRegistro = f; }

    public static UsuarioBuilder builder() { return new UsuarioBuilder(); }
    public static class UsuarioBuilder {
        private final Usuario u = new Usuario();
        public UsuarioBuilder usuario(String v) { u.usuario = v; return this; }
        public UsuarioBuilder email(String v) { u.email = v; return this; }
        public UsuarioBuilder telefono(String v) { u.telefono = v; return this; }
        public UsuarioBuilder clave(String v) { u.clave = v; return this; }
        public UsuarioBuilder rol(Rol v) { u.rol = v; return this; }
        public UsuarioBuilder ciudad(String v) { u.ciudad = v; return this; }
        public UsuarioBuilder direccion(String v) { u.direccion = v; return this; }
        public UsuarioBuilder estado(EstadoUsuario v) { u.estado = v; return this; }
        public Usuario build() { return u; }
    }
}
