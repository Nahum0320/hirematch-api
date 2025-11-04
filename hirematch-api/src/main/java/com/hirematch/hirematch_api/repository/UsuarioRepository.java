package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    
    // Métodos para gestión de bloqueos por admins
    Page<Usuario> findByNivelBloqueo(Integer nivelBloqueo, Pageable pageable);
    Page<Usuario> findByNivelBloqueoGreaterThan(Integer nivelBloqueo, Pageable pageable);
}
