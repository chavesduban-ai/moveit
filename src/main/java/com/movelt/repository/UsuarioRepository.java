package com.movelt.repository;

import com.movelt.model.Usuario;
import com.movelt.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuario(String usuario);
    Optional<Usuario> findByEmail(String email);
    boolean existsByUsuario(String usuario);
    boolean existsByEmail(String email);
    List<Usuario> findByRol(Rol rol);
    long countByRol(Rol rol);
}
