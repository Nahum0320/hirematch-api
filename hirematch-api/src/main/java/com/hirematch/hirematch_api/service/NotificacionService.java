package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.entity.Notificacion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.NotificacionRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public void enviarNotificacion(Usuario usuario, String mensaje) {
        Notificacion n = new Notificacion();
        n.setUsuario(usuario);
        n.setMensaje(mensaje);
        n.setLeida(false);
        notificacionRepository.save(n);
        // Aquí se pueden integrar canales externos (email, push) en el futuro
    }
}
