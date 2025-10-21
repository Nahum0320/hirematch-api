package com.hirematch.hirematch_api.tasks;

import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DesbloqueoTask {

    private final UsuarioRepository usuarioRepository;

    public DesbloqueoTask(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Ejecuta cada hora
    @Scheduled(cron = "0 0 * * * *")
    public void desbloquearUsuariosExpirados() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Usuario> bloqueados = usuarioRepository.findAll();
        for (Usuario u : bloqueados) {
            if (u.getFechaFinBloqueo() != null && ahora.isAfter(u.getFechaFinBloqueo())) {
                u.setNivelBloqueo(0);
                u.setFechaFinBloqueo(null);
                u.setActivo(true);
                usuarioRepository.save(u);
            }
        }
    }
}
