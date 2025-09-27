package com.hirematch.hirematch_api.config;

import com.hirematch.hirematch_api.entity.Badge;
import com.hirematch.hirematch_api.repository.BadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BadgeDataSeeding implements CommandLineRunner {

    @Autowired
    private BadgeRepository badgeRepository;

    @Override
    public void run(String... args) {
        if (badgeRepository.count() == 0) {
            crearBadgesIniciales();
        }
    }

    private void crearBadgesIniciales() {
        // Badge de primer match
        crearBadge("PRIMER_MATCH", "¡Primer Match!", 
                  "¡Felicidades por tu primer match exitoso!", 
                  "favorite", "#E91E63", Badge.TipoBadge.PRIMER_MATCH, 1);

        // Badges de cantidad de matches
        crearBadge("MATCHES_5", "Conectador", 
                  "Has logrado 5 matches exitosos", 
                  "favorite", "#9C27B0", Badge.TipoBadge.MATCHES_CANTIDAD, 5);
        
        crearBadge("MATCHES_10", "Networker", 
                  "¡Impresionante! 10 matches y contando", 
                  "favorite", "#673AB7", Badge.TipoBadge.MATCHES_CANTIDAD, 10);
        
        crearBadge("MATCHES_25", "Maestro de Conexiones", 
                  "25 matches demuestran tu habilidad para conectar", 
                  "favorite", "#3F51B5", Badge.TipoBadge.MATCHES_CANTIDAD, 25);

        // Badge de primer like
        crearBadge("PRIMER_LIKE", "Primera Impresión", 
                  "Has dado tu primer like", 
                  "thumb_up", "#2196F3", Badge.TipoBadge.PRIMER_LIKE, 1);

        // Badges de cantidad de likes
        crearBadge("LIKES_10", "Me Gusta Activo", 
                  "Has dado 10 likes", 
                  "thumb_up", "#03A9F4", Badge.TipoBadge.LIKES_CANTIDAD, 10);
        
        crearBadge("LIKES_50", "Explorador", 
                  "50 likes dados explorando oportunidades", 
                  "thumb_up", "#00BCD4", Badge.TipoBadge.LIKES_CANTIDAD, 50);
        
        crearBadge("LIKES_100", "Cazador de Oportunidades", 
                  "100 likes demuestran tu búsqueda activa", 
                  "thumb_up", "#009688", Badge.TipoBadge.LIKES_CANTIDAD, 100);

        // Badge de primer superlike
        crearBadge("PRIMER_SUPERLIKE", "¡Super Inicio!", 
                  "Tu primer superlike muestra determinación", 
                  "star", "#FFD700", Badge.TipoBadge.PRIMER_SUPERLIKE, 1);

        // Badges de cantidad de superlikes
        crearBadge("SUPERLIKES_5", "Estrella en Ascenso", 
                  "5 superlikes utilizados estratégicamente", 
                  "star", "#FF9800", Badge.TipoBadge.SUPERLIKES_CANTIDAD, 5);
        
        crearBadge("SUPERLIKES_15", "Super Estratega", 
                  "15 superlikes muestran tu compromiso", 
                  "star", "#FF5722", Badge.TipoBadge.SUPERLIKES_CANTIDAD, 15);

        // Badges especiales
        crearBadge("VETERANO", "Veterano", 
                  "30 días activo en la plataforma", 
                  "military_tech", "#795548", Badge.TipoBadge.VETERANO, 30);
        
        crearBadge("POPULAR", "Popular", 
                  "Has recibido muchos likes", 
                  "trending_up", "#F44336", Badge.TipoBadge.POPULARIDAD, 20);
        
        crearBadge("BETA_TESTER", "Beta Tester", 
                  "Fuiste parte de los primeros usuarios", 
                  "bug_report", "#607D8B", Badge.TipoBadge.ESPECIAL, null);
    }

    private void crearBadge(String nombre, String titulo, String descripcion, 
                           String icono, String color, Badge.TipoBadge tipo, Integer condicion) {
        if (!badgeRepository.findByNombreAndActivoTrue(nombre).isPresent()) {
            Badge badge = new Badge();
            badge.setNombre(nombre);
            badge.setDescripcion(descripcion);
            badge.setIcono(icono);
            badge.setColor(color);
            badge.setTipo(tipo);
            badge.setCondicionRequerida(condicion);
            badge.setActivo(true);
            
            badgeRepository.save(badge);
            System.out.println("Badge creado: " + nombre);
        }
    }
}