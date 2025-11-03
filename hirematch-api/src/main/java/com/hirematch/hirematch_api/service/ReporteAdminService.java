package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.entity.EstadoReporte;
import com.hirematch.hirematch_api.entity.Reporte;
import com.hirematch.hirematch_api.entity.TipoReporte;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.ReporteRepository;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReporteAdminService {

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public ReporteAdminService(ReporteRepository reporteRepository,
                               UsuarioRepository usuarioRepository,
                               EmailService emailService) {
        this.reporteRepository = reporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    public Page<Reporte> listarReportesPendientes(Pageable pageable,
                                                 TipoReporte tipo,
                                                 EstadoReporte estado,
                                                 LocalDateTime desde,
                                                 LocalDateTime hasta) {
        // No forzar estado a PENDIENTE si es null - esto permite traer todos los reportes
        return reporteRepository.buscarFiltros(estado, tipo, desde, hasta, pageable);
    }

    public Reporte resolverReporte(Long reporteId, boolean aprobar, String razon) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));

        if (aprobar) {
            reporte.setEstado(EstadoReporte.RESUELTO);
            aplicarConsecuencias(reporte);
        } else {
            reporte.setEstado(EstadoReporte.RECHAZADO);
        }

        reporteRepository.save(reporte);
        return reporte;
    }

    private void aplicarConsecuencias(Reporte reporte) {
        Usuario objetivo = null;
        String tipoReporte = "";
        String detallesAdicionales = "";
        
        if (reporte.getReportado() != null) {
            objetivo = reporte.getReportado();
            tipoReporte = "perfil de usuario";
            detallesAdicionales = String.format(
                "Usuario reportado: %s\nEmail: %s",
                objetivo.getNombre(),
                objetivo.getEmail()
            );
        } else if (reporte.getOferta() != null && reporte.getOferta().getEmpresa() != null) {
            objetivo = reporte.getOferta().getEmpresa().getUsuario();
            tipoReporte = "oferta laboral";
            detallesAdicionales = String.format(
                "Oferta reportada: %s\nEmpresa: %s\nUbicación: %s\nTipo de trabajo: %s\nID de oferta: %d",
                reporte.getOferta().getTitulo(),
                reporte.getOferta().getEmpresa().getNombreEmpresa(),
                reporte.getOferta().getUbicacion() != null ? reporte.getOferta().getUbicacion() : "No especificada",
                reporte.getOferta().getTipoTrabajo() != null ? reporte.getOferta().getTipoTrabajo().toString() : "No especificado",
                reporte.getOferta().getId()
            );
        }

        if (objetivo == null) return;

        Integer acumulados = objetivo.getReportesAcumulados() == null ? 0 : objetivo.getReportesAcumulados();
        acumulados++;
        objetivo.setReportesAcumulados(acumulados);

        String motivoReporte = reporte.getMotivo();
        LocalDateTime ahora = LocalDateTime.now();
        
        if (acumulados >= 10) {
            objetivo.setNivelBloqueo(3);
            objetivo.setFechaFinBloqueo(null); // bloqueo permanente
            objetivo.setActivo(false);
            emailService.enviarCorreoNotificacionReporte(
                objetivo.getEmail(), 
                objetivo.getNombre(), 
                "Su cuenta ha sido bloqueada permanentemente por múltiples violaciones.",
                motivoReporte,
                tipoReporte,
                detallesAdicionales
            );
        } else if (acumulados >= 5) {
            objetivo.setNivelBloqueo(2);
            objetivo.setFechaFinBloqueo(ahora.plusDays(7));
            objetivo.setActivo(false);
            emailService.enviarCorreoNotificacionReporte(
                objetivo.getEmail(), 
                objetivo.getNombre(), 
                "Su cuenta ha sido bloqueada por 7 días debido a reportes acumulados.",
                motivoReporte,
                tipoReporte,
                detallesAdicionales
            );
        } else if (acumulados >= 3) {
            objetivo.setNivelBloqueo(1);
            objetivo.setFechaFinBloqueo(ahora.plusDays(1));
            objetivo.setActivo(false);
            emailService.enviarCorreoNotificacionReporte(
                objetivo.getEmail(), 
                objetivo.getNombre(), 
                "Su cuenta ha sido bloqueada por 1 día debido a reportes acumulados.",
                motivoReporte,
                tipoReporte,
                detallesAdicionales
            );
        } else {
            emailService.enviarCorreoNotificacionReporte(
                objetivo.getEmail(), 
                objetivo.getNombre(), 
                "Se ha resuelto un reporte en su contra. Mantenga buenas prácticas para evitar sanciones.",
                motivoReporte,
                tipoReporte,
                detallesAdicionales
            );
        }

        usuarioRepository.save(objetivo);
    }
}
