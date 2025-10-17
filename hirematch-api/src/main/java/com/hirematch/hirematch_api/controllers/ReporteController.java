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
import com.hirematch.hirematch_api.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteRepository reporteRepository;
    private final TokenService tokenService;
    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final OfertaLaboralRepository ofertaLaboralRepository;
    private final PerfilRepository perfilRepository;

    public ReporteController(
            ReporteRepository reporteRepository,
            TokenService tokenService,
            SesionRepository sesionRepository,
            UsuarioRepository usuarioRepository,
            OfertaLaboralRepository ofertaLaboralRepository,
            PerfilRepository perfilRepository) {
        this.reporteRepository = reporteRepository;
        this.tokenService = tokenService;
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.ofertaLaboralRepository = ofertaLaboralRepository;
        this.perfilRepository = perfilRepository;
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