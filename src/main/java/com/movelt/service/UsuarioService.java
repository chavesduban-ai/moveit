package com.movelt.service;

import com.movelt.model.Usuario;
import com.movelt.model.enums.Rol;
import com.movelt.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public Usuario registrar(String usuario, String email, String telefono,
                             String password, String ciudad, String direccion) {
        if (repo.existsByUsuario(usuario))
            throw new RuntimeException("El nombre de usuario ya existe");
        if (repo.existsByEmail(email))
            throw new RuntimeException("El email ya está registrado");

        Usuario u = Usuario.builder()
            .usuario(usuario).email(email).telefono(telefono)
            .clave(encoder.encode(password))
            .rol(Rol.cliente)
            .ciudad(ciudad).direccion(direccion)
            .build();
        return repo.save(u);
    }

    public List<Usuario> listarTodos() { return repo.findAll(); }
    public List<Usuario> listarPorRol(Rol rol) { return repo.findByRol(rol); }
    public Optional<Usuario> buscarPorId(Long id) { return repo.findById(id); }
    public Optional<Usuario> buscarPorUsuario(String usuario) { return repo.findByUsuario(usuario); }
    public long contarPorRol(Rol rol) { return repo.countByRol(rol); }

    public Usuario actualizar(Usuario u) { return repo.save(u); }

    public void cambiarPassword(Long id, String actual, String nueva) {
        Usuario u = repo.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!encoder.matches(actual, u.getClave()))
            throw new RuntimeException("Contraseña actual incorrecta");
        u.setClave(encoder.encode(nueva));
        repo.save(u);
    }

    public void cambiarRol(Long id, Rol nuevoRol) {
        Usuario u = repo.findById(id).orElseThrow();
        u.setRol(nuevoRol);
        repo.save(u);
    }

    public void eliminar(Long id) { repo.deleteById(id); }

    public Usuario guardar(Usuario u) {
        if (u.getClave() != null && !u.getClave().startsWith("$2"))
            u.setClave(encoder.encode(u.getClave()));
        return repo.save(u);
    }
}
