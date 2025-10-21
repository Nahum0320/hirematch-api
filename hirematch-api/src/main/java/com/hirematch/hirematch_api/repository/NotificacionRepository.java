package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Notificacion;
import com.hirematch.hirematch_api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuario(Usuario usuario);
}
