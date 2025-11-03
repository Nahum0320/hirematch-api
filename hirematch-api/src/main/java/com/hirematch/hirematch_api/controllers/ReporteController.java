package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.ReporteUsuarioRequest;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.EstadoReporte;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
import com.hirematch.hirematch_api.entity.Reporte;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.TipoReporte;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.OfertaLaboralRepository;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.ReporteRepository;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import com.hirematch.hirematch_api.service.ReporteAdminService;
import com.hirematch.hirematch_api.service.EmailService;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.DTO.ReporteAdminResponse;
import com.hirematch.hirematch_api.DTO.ReporteOfertaRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private static final Logger log = LoggerFactory.getLogger(ReporteController.class);

    private final ReporteRepository reporteRepository;
    private final TokenService tokenService;
    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final OfertaLaboralRepository ofertaLaboralRepository;
    private final PerfilRepository perfilRepository;
    private final ReporteAdminService reporteAdminService;
    private final EmailService emailService;

    public ReporteController(
            ReporteRepository reporteRepository,
            TokenService tokenService,
            SesionRepository sesionRepository,
            UsuarioRepository usuarioRepository,
            OfertaLaboralRepository ofertaLaboralRepository,
            PerfilRepository perfilRepository,
            ReporteAdminService reporteAdminService,
            EmailService emailService) {
        this.reporteRepository = reporteRepository;
        this.tokenService = tokenService;
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.ofertaLaboralRepository = ofertaLaboralRepository;
        this.perfilRepository = perfilRepository;
        this.reporteAdminService = reporteAdminService;
        this.emailService = emailService;
    }

    @PostMapping("/oferta")
    public ResponseEntity<String> reportarOferta(
            @Valid @RequestBody ReporteOfertaRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Usuario reportante = obtenerUsuarioAutenticado(authHeader);

        TipoReporte tipo;
        try {
            tipo = TipoReporte.valueOf(request.getTipo().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Tipo de reporte inválido. Tipos válidos: SPAM, ACOSO, CONTENIDO_INAPROPIADO, FRAUDE, OFERTA_ENGAÑOSA, OTRO");
        }

        OfertaLaboral oferta = ofertaLaboralRepository.findById(request.getOfertaId())
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));
        // Evitar que el propietario de la oferta se reporte a sí mismo
        if (oferta.getEmpresa() != null && oferta.getEmpresa().getUsuario() != null) {
            Usuario propietario = oferta.getEmpresa().getUsuario();
            if (propietario.getUsuarioId().equals(reportante.getUsuarioId())) {
                throw new ValidacionException("No puedes reportar tu propia oferta");
            }
        }

        // Prevención de reportes duplicados
        Optional<Reporte> existing = reporteRepository.findByReportanteAndOfertaAndTipoAndEstado(
                reportante, oferta, tipo, EstadoReporte.PENDIENTE);

        if (existing.isPresent()) {
            throw new ValidacionException("Ya existe un reporte pendiente para esta oferta con este tipo");
        }

        Reporte reporte = new Reporte();
        reporte.setReportante(reportante);
        reporte.setOferta(oferta);
        reporte.setTipo(tipo);
        reporte.setMotivo(request.getMotivo());
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte.setFecha(LocalDateTime.now());

        reporteRepository.save(reporte);

        return ResponseEntity.ok("Reporte de oferta creado exitosamente. Gracias por ayudar a mantener la comunidad segura.");
    }

    // Endpoint administrativo: listar reportes con filtros
    @GetMapping("/admin")
    public ResponseEntity<org.springframework.data.domain.Page<ReporteAdminResponse>> listarReportesAdmin(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "tipo", required = false) String tipoStr,
            @RequestParam(value = "estado", required = false) String estadoStr,
            @RequestParam(value = "desde", required = false) String desdeStr,
            @RequestParam(value = "hasta", required = false) String hastaStr,
            @RequestHeader("Authorization") String authHeader
    ) {
    // Log de diagnóstico: mostrar header y Authentication para depuración local
    log.debug("Authorization header present: {}", authHeader != null);
    var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    log.debug("SecurityContext Authentication: {}", auth);

    // Validación básica de rol: se asume que el token se asocia a sesión y perfil; aquí no se implementa verificación de rol detallada
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        // Verificar rol ADMIN
        if (usuario.getPerfil() == null || !"ADMIN".equalsIgnoreCase(usuario.getPerfil().getTipoPerfil())) {
            throw new ValidacionException("Acceso denegado: se requieren permisos de administrador");
        }

        TipoReporte tipo = null;
        EstadoReporte estado = null;
        java.time.LocalDateTime desde = null;
        java.time.LocalDateTime hasta = null;

        if (tipoStr != null) {
            try { tipo = TipoReporte.valueOf(tipoStr.toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }
        if (estadoStr != null) {
            try { estado = EstadoReporte.valueOf(estadoStr.toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }
        try { if (desdeStr != null) desde = java.time.LocalDateTime.parse(desdeStr); } catch (Exception ignored) {}
        try { if (hastaStr != null) hasta = java.time.LocalDateTime.parse(hastaStr); } catch (Exception ignored) {}

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Reporte> result = reporteAdminService.listarReportesPendientes(pageable, tipo, estado, desde, hasta);

        // Mapear a DTOs para no exponer entidades JPA completas
        var dtoPage = result.map(r -> {
            com.hirematch.hirematch_api.DTO.UsuarioSummary reportanteDto = null;
            com.hirematch.hirematch_api.DTO.UsuarioSummary reportadoDto = null;
            if (r.getReportante() != null) {
                var rep = r.getReportante();
                reportanteDto = new com.hirematch.hirematch_api.DTO.UsuarioSummary(rep.getUsuarioId(), rep.getNombre(), rep.getApellido(), rep.getEmail());
            }
            if (r.getReportado() != null) {
                var rp = r.getReportado();
                reportadoDto = new com.hirematch.hirematch_api.DTO.UsuarioSummary(rp.getUsuarioId(), rp.getNombre(), rp.getApellido(), rp.getEmail());
            }

            Long ofertaId = r.getOferta() != null ? r.getOferta().getId() : null;

            return new com.hirematch.hirematch_api.DTO.ReporteAdminResponse(
                    r.getId(),
                    reportanteDto,
                    reportadoDto,
                    ofertaId,
                    r.getTipo(),
                    r.getMotivo(),
                    r.getEstado(),
                    r.getFecha()
            );
        });

        return ResponseEntity.ok(dtoPage);
    }

    // Resolver un reporte: aprobar o rechazar
    public static class ResolverReporteRequest {
        public boolean aprobar;
        public String razon;
    }

    @PostMapping("/admin/{id}/resolver")
    public ResponseEntity<String> resolverReporteAdmin(
            @PathVariable("id") Long id,
            @RequestBody ResolverReporteRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        if (usuario.getPerfil() == null || !"ADMIN".equalsIgnoreCase(usuario.getPerfil().getTipoPerfil())) {
            throw new ValidacionException("Acceso denegado: se requieren permisos de administrador");
        }
        Reporte reporte = reporteAdminService.resolverReporte(id, request.aprobar, request.razon);

        // Si se aprobo, enviar correo al afectado avisando del bloqueo (si aplica). ReporteAdminService ya aplica bloqueo y envía correo.
        return ResponseEntity.ok("Reporte procesado con estado: " + reporte.getEstado().name());
    }

    @PutMapping("/admin/{id}/revision")
    public ResponseEntity<String> marcarEnRevision(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        if (usuario.getPerfil() == null || !"ADMIN".equalsIgnoreCase(usuario.getPerfil().getTipoPerfil())) {
            throw new ValidacionException("Acceso denegado: se requieren permisos de administrador");
        }
        
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ValidacionException("Reporte no encontrado"));
        
        if (reporte.getEstado() != EstadoReporte.PENDIENTE) {
            throw new ValidacionException("Solo se pueden marcar como EN_REVISION los reportes PENDIENTES");
        }
        
        reporte.setEstado(EstadoReporte.EN_REVISION);
        reporteRepository.save(reporte);
        
        return ResponseEntity.ok("Reporte marcado como EN_REVISION exitosamente");
    }

    @PostMapping("/usuario")
    public ResponseEntity<String> reportarUsuario(
            @Valid @RequestBody ReporteUsuarioRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Usuario reportante = obtenerUsuarioAutenticado(authHeader);

        // Validate that either perfilId or ofertaId is provided, but not both
        if ((request.getPerfilId() == null && request.getOfertaId() == null) ||
                (request.getPerfilId() != null && request.getOfertaId() != null)) {
            throw new ValidacionException("Debe especificar un perfil o una oferta para reportar, pero no ambos");
        }

        TipoReporte tipo;
        try {
            tipo = TipoReporte.valueOf(request.getTipo().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Tipo de reporte inválido. Tipos válidos: SPAM, ACOSO, CONTENIDO_INAPROPIADO, FRAUDE, OFERTA_ENGAÑOSA, OTRO");
        }

        Reporte reporte = new Reporte();
        reporte.setReportante(reportante);
        reporte.setTipo(tipo);
        reporte.setMotivo(request.getMotivo());
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte.setFecha(LocalDateTime.now());

        if (request.getPerfilId() != null) {
            // Reporte de usuario
            Long usuarioId = perfilRepository.findUsuarioIdByPerfilId(request.getPerfilId())
                    .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));

            Usuario reportado = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ValidacionException("Usuario reportado no encontrado"));

            if (reportante.getUsuarioId().equals(reportado.getUsuarioId())) {
                throw new ValidacionException("No puedes reportarte a ti mismo");
            }

            // Prevención de reportes duplicados
            Optional<Reporte> existing = reporteRepository.findByReportanteAndReportadoAndTipoAndEstado(
                    reportante, reportado, tipo, EstadoReporte.PENDIENTE);

            if (existing.isPresent()) {
                throw new ValidacionException("Ya existe un reporte pendiente para este usuario con este tipo");
            }

            reporte.setReportado(reportado);
        } else {
            // Reporte de oferta
            OfertaLaboral oferta = ofertaLaboralRepository.findById(request.getOfertaId())
                    .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));

            // Prevención de reportes duplicados
            Optional<Reporte> existing = reporteRepository.findByReportanteAndOfertaAndTipoAndEstado(
                    reportante, oferta, tipo, EstadoReporte.PENDIENTE);

            if (existing.isPresent()) {
                throw new ValidacionException("Ya existe un reporte pendiente para esta oferta con este tipo");
            }

            reporte.setOferta(oferta);
        }

        reporteRepository.save(reporte);

        return ResponseEntity.ok("Reporte creado exitosamente. Gracias por ayudar a mantener la comunidad segura.");
    }

    // ======== ENDPOINTS DE GESTIÓN DE BLOQUEOS ========
    
    @GetMapping("/admin/usuarios/bloqueados")
    public ResponseEntity<org.springframework.data.domain.Page<UsuarioBloqueoResponse>> listarUsuariosBloqueados(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "nivelBloqueo", required = false) Integer nivelBloqueo,
            @RequestHeader("Authorization") String authHeader
    ) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        if (usuario.getPerfil() == null || !"ADMIN".equalsIgnoreCase(usuario.getPerfil().getTipoPerfil())) {
            throw new ValidacionException("Acceso denegado: se requieren permisos de administrador");
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Usuario> usuarios;
        
        if (nivelBloqueo != null) {
            usuarios = usuarioRepository.findByNivelBloqueo(nivelBloqueo, pageable);
        } else {
            // Listar todos los usuarios con algún nivel de bloqueo (> 0)
            usuarios = usuarioRepository.findByNivelBloqueoGreaterThan(0, pageable);
        }

        var dtoPage = usuarios.map(u -> new UsuarioBloqueoResponse(
                u.getUsuarioId(),
                u.getNombre(),
                u.getApellido(),
                u.getEmail(),
                u.getReportesAcumulados() != null ? u.getReportesAcumulados() : 0,
                u.getNivelBloqueo() != null ? u.getNivelBloqueo() : 0,
                u.getFechaFinBloqueo(),
                u.getActivo(),
                u.getPerfil() != null ? u.getPerfil().getTipoPerfil() : null
        ));

        return ResponseEntity.ok(dtoPage);
    }

    @PutMapping("/admin/usuarios/{usuarioId}/bloqueo")
    public ResponseEntity<String> actualizarBloqueo(
            @PathVariable("usuarioId") Long usuarioId,
            @RequestBody ActualizarBloqueoRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        Usuario admin = obtenerUsuarioAutenticado(authHeader);
        if (admin.getPerfil() == null || !"ADMIN".equalsIgnoreCase(admin.getPerfil().getTipoPerfil())) {
            throw new ValidacionException("Acceso denegado: se requieren permisos de administrador");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ValidacionException("Usuario no encontrado"));

        // Actualizar nivel de bloqueo
        if (request.nivelBloqueo != null) {
            usuario.setNivelBloqueo(request.nivelBloqueo);
        }

        // Actualizar fecha fin de bloqueo
        if (request.fechaFinBloqueo != null) {
            usuario.setFechaFinBloqueo(request.fechaFinBloqueo);
        } else if (request.nivelBloqueo != null && request.nivelBloqueo == 0) {
            // Si se revierte el bloqueo, limpiar fecha
            usuario.setFechaFinBloqueo(null);
        }

        // Actualizar estado activo
        if (request.activo != null) {
            usuario.setActivo(request.activo);
        }

        // Si se está desbloqueando (nivel 0), activar usuario
        if (request.nivelBloqueo != null && request.nivelBloqueo == 0) {
            usuario.setActivo(true);
            usuario.setFechaFinBloqueo(null);
        }

        usuarioRepository.save(usuario);

        String mensaje = request.nivelBloqueo != null && request.nivelBloqueo == 0 
            ? "Usuario desbloqueado exitosamente" 
            : "Bloqueo actualizado exitosamente";

        return ResponseEntity.ok(mensaje);
    }

    @PostMapping("/admin/usuarios/{usuarioId}/desbloquear")
    public ResponseEntity<String> desbloquearUsuario(
            @PathVariable("usuarioId") Long usuarioId,
            @RequestHeader("Authorization") String authHeader
    ) {
        Usuario admin = obtenerUsuarioAutenticado(authHeader);
        if (admin.getPerfil() == null || !"ADMIN".equalsIgnoreCase(admin.getPerfil().getTipoPerfil())) {
            throw new ValidacionException("Acceso denegado: se requieren permisos de administrador");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ValidacionException("Usuario no encontrado"));

        usuario.setNivelBloqueo(0);
        usuario.setFechaFinBloqueo(null);
        usuario.setActivo(true);

        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Usuario desbloqueado exitosamente");
    }

    // DTOs internos
    public static class ActualizarBloqueoRequest {
        public Integer nivelBloqueo;
        public LocalDateTime fechaFinBloqueo;
        public Boolean activo;
    }

    public static class UsuarioBloqueoResponse {
        public Long usuarioId;
        public String nombre;
        public String apellido;
        public String email;
        public Integer reportesAcumulados;
        public Integer nivelBloqueo;
        public LocalDateTime fechaFinBloqueo;
        public Boolean activo;
        public String tipoPerfil;

        public UsuarioBloqueoResponse(Long usuarioId, String nombre, String apellido, String email,
                                     Integer reportesAcumulados, Integer nivelBloqueo,
                                     LocalDateTime fechaFinBloqueo, Boolean activo, String tipoPerfil) {
            this.usuarioId = usuarioId;
            this.nombre = nombre;
            this.apellido = apellido;
            this.email = email;
            this.reportesAcumulados = reportesAcumulados;
            this.nivelBloqueo = nivelBloqueo;
            this.fechaFinBloqueo = fechaFinBloqueo;
            this.activo = activo;
            this.tipoPerfil = tipoPerfil;
        }
    }

    private Usuario obtenerUsuarioAutenticado(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ValidacionException("Token de autorización requerido");
        }
        String token = authHeader.substring(7);
        if (token.trim().isEmpty()) {
            throw new ValidacionException("Token vacío");
        }
        try {
            String subject = tokenService.getSubject(token);
            if (subject == null || subject.trim().isEmpty()) {
                throw new ValidacionException("Token inválido");
            }
            Long numeroSesion = Long.parseLong(subject);
            Sesion sesion = sesionRepository.findById(numeroSesion)
                    .orElseThrow(() -> new ValidacionException("Sesión no encontrada"));
            if (!sesion.isActiva() || sesion.hasExpired()) {
                throw new ValidacionException("Sesión inactiva o expirada");
            }
            return sesion.getUsuario();
        } catch (NumberFormatException e) {
            throw new ValidacionException("Token de sesión inválido");
        } catch (Exception e) {
            throw new ValidacionException("Error al procesar el token: " + e.getMessage());
        }
    }
}