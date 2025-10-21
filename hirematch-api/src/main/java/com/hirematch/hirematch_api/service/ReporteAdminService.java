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
        if (estado == null) estado = EstadoReporte.PENDIENTE;
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
        if (reporte.getReportado() != null) {
            objetivo = reporte.getReportado();
        } else if (reporte.getOferta() != null && reporte.getOferta().getEmpresa() != null) {
            objetivo = reporte.getOferta().getEmpresa().getUsuario();
        }

        if (objetivo == null) return;

        Integer acumulados = objetivo.getReportesAcumulados() == null ? 0 : objetivo.getReportesAcumulados();
        acumulados++;
        objetivo.setReportesAcumulados(acumulados);

        LocalDateTime ahora = LocalDateTime.now();
        if (acumulados >= 10) {
            objetivo.setNivelBloqueo(3);
            objetivo.setFechaFinBloqueo(null); // bloqueo permanente
            objetivo.setActivo(false);
            emailService.enviarCorreoVerificacion(objetivo.getEmail(), objetivo.getNombre(), "Su cuenta ha sido bloqueada permanentemente por múltiples violaciones.");
        } else if (acumulados >= 5) {
            objetivo.setNivelBloqueo(2);
            objetivo.setFechaFinBloqueo(ahora.plusDays(7));
            objetivo.setActivo(false);
            emailService.enviarCorreoRecuperacion(objetivo.getEmail(), objetivo.getNombre(), "Su cuenta ha sido bloqueada por 7 días debido a reportes acumulados.");
        } else if (acumulados >= 3) {
            objetivo.setNivelBloqueo(1);
            objetivo.setFechaFinBloqueo(ahora.plusDays(1));
            objetivo.setActivo(false);
            emailService.enviarCorreoRecuperacion(objetivo.getEmail(), objetivo.getNombre(), "Su cuenta ha sido bloqueada por 1 día debido a reportes acumulados.");
        } else {
            emailService.enviarCorreoVerificacion(objetivo.getEmail(), objetivo.getNombre(), "Se ha resuelto un reporte en su contra. Mantenga buenas prácticas para evitar sanciones.");
        }

        usuarioRepository.save(objetivo);
    }
}
