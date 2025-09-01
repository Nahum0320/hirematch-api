package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.DTO.CrearOfertaRequest;
import com.hirematch.hirematch_api.DTO.OfertaResponse;
import com.hirematch.hirematch_api.DTO.OfertaFeedResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.*;
import com.hirematch.hirematch_api.repository.EmpresaRepository;
import com.hirematch.hirematch_api.repository.OfertaLaboralRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class OfertaService {

    private final OfertaLaboralRepository ofertaRepository;
    private final EmpresaRepository empresaRepository;

    public OfertaService(OfertaLaboralRepository ofertaRepository, EmpresaRepository empresaRepository) {
        this.ofertaRepository = ofertaRepository;
        this.empresaRepository = empresaRepository;
    }

    public OfertaResponse crearOferta(CrearOfertaRequest request) {
        return crearOferta(request, null);
    }

    public OfertaResponse crearOferta(CrearOfertaRequest request, Usuario usuarioAutenticado) {
        Empresa empresa = obtenerEmpresa(request, usuarioAutenticado);

        OfertaLaboral oferta = new OfertaLaboral();
        mapearRequestAEntidad(request, oferta);
        oferta.setEmpresa(empresa);

        OfertaLaboral saved = ofertaRepository.save(oferta);
        return mapearAOfertaResponse(saved);
    }

    public Page<OfertaFeedResponse> obtenerFeed(Pageable pageable) {
        return ofertaRepository.findByEstadoOrderByFechaPublicacionDesc(EstadoOferta.ACTIVA, pageable)
                .map(this::mapearAFeedResponse);
    }

    public Page<OfertaFeedResponse> obtenerFeedParaUsuario(Pageable pageable, Usuario usuario) {
        // Aquí puedes implementar lógica de matching más sofisticada
        // Por ejemplo, filtrar por ubicación, habilidades, área de trabajo, etc.
        return obtenerFeed(pageable);
    }

    public OfertaResponse obtenerOfertaPorId(Long id) {
        OfertaLaboral oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));

        // Incrementar vistas
        oferta.incrementarVistas();
        ofertaRepository.save(oferta);

        return mapearAOfertaResponse(oferta);
    }

    private Empresa obtenerEmpresa(CrearOfertaRequest request, Usuario usuarioAutenticado) {
        if (request.getEmpresaId() != null) {
            return empresaRepository.findById(request.getEmpresaId())
                    .orElseThrow(() -> new ValidacionException("Empresa no encontrada"));
        } else if (usuarioAutenticado != null) {
            return obtenerEmpresaDelUsuario(usuarioAutenticado);
        } else {
            throw new ValidacionException("Debe especificar una empresa o estar autenticado como empresa");
        }
    }

    private void mapearRequestAEntidad(CrearOfertaRequest request, OfertaLaboral oferta) {
        // Información básica
        oferta.setTitulo(request.getTitulo());
        oferta.setDescripcion(request.getDescripcion());
        oferta.setUbicacion(request.getUbicacion());

        // Enums
        if (request.getTipoTrabajo() != null) {
            oferta.setTipoTrabajo(TipoTrabajo.valueOf(request.getTipoTrabajo().toUpperCase()));
        }
        if (request.getTipoContrato() != null) {
            oferta.setTipoContrato(TipoContrato.valueOf(request.getTipoContrato().toUpperCase()));
        }
        if (request.getNivelExperiencia() != null) {
            oferta.setNivelExperiencia(NivelExperiencia.valueOf(request.getNivelExperiencia().toUpperCase()));
        }
        if (request.getMoneda() != null) {
            oferta.setMoneda(Moneda.valueOf(request.getMoneda().toUpperCase()));
        }

        // Detalles del trabajo
        oferta.setAreaTrabajo(request.getAreaTrabajo());

        // Compensación
        oferta.setSalarioMinimo(request.getSalarioMinimo());
        // Compensación
        oferta.setSalarioMinimo(request.getSalarioMinimo());
        oferta.setSalarioMaximo(request.getSalarioMaximo());
        oferta.setSalarioNegociable(request.getSalarioNegociable());
        oferta.setMostrarSalario(request.getMostrarSalario());

        // Beneficios y requisitos
        oferta.setBeneficios(request.getBeneficios());
        oferta.setRequisitos(request.getRequisitos());
        oferta.setHabilidadesRequeridas(request.getHabilidadesRequeridas());
        oferta.setIdiomas(request.getIdiomas());

        // Configuración
        oferta.setFechaCierre(request.getFechaCierre());
        oferta.setVacantesDisponibles(request.getVacantesDisponibles());
        oferta.setAplicacionRapida(request.getAplicacionRapida());
        oferta.setPreguntasAdicionales(request.getPreguntasAdicionales());
        oferta.setUrgente(request.getUrgente());
        oferta.setDestacada(request.getDestacada());
        oferta.setEtiquetas(request.getEtiquetas());
        oferta.setPermiteAplicacionExterna(request.getPermiteAplicacionExterna());
        oferta.setUrlAplicacionExterna(request.getUrlAplicacionExterna());
    }

    private OfertaResponse mapearAOfertaResponse(OfertaLaboral oferta) {
        OfertaResponse response = new OfertaResponse();

        // Información básica
        response.setId(oferta.getId());
        response.setTitulo(oferta.getTitulo());
        response.setDescripcion(oferta.getDescripcion());
        response.setUbicacion(oferta.getUbicacion());

        // Empresa
        response.setEmpresaId(oferta.getEmpresa().getEmpresaId());
        response.setEmpresaNombre(oferta.getEmpresa().getNombreEmpresa());
        response.setEmpresaDescripcion(oferta.getEmpresa().getDescripcion());
        response.setEmpresaSitioWeb(oferta.getEmpresa().getSitioWeb());

        // Detalles del trabajo con descripciones
        if (oferta.getTipoTrabajo() != null) {
            response.setTipoTrabajo(oferta.getTipoTrabajo().name());
            response.setTipoTrabajoDescripcion(oferta.getTipoTrabajo().getDescripcion());
        }
        if (oferta.getTipoContrato() != null) {
            response.setTipoContrato(oferta.getTipoContrato().name());
            response.setTipoContratoDescripcion(oferta.getTipoContrato().getDescripcion());
        }
        if (oferta.getNivelExperiencia() != null) {
            response.setNivelExperiencia(oferta.getNivelExperiencia().name());
            response.setNivelExperienciaDescripcion(oferta.getNivelExperiencia().getDescripcion());
        }
        response.setAreaTrabajo(oferta.getAreaTrabajo());

        // Compensación
        response.setSalarioMinimo(oferta.getSalarioMinimo());
        response.setSalarioMaximo(oferta.getSalarioMaximo());
        if (oferta.getMoneda() != null) {
            response.setMoneda(oferta.getMoneda().name());
            response.setMonedaSimbolo(oferta.getMoneda().getSimbolo());
        }
        response.setSalarioNegociable(oferta.getSalarioNegociable());
        response.setMostrarSalario(oferta.getMostrarSalario());
        response.setSalarioFormateado(oferta.getSalarioFormateado());

        // Beneficios y requisitos
        response.setBeneficios(oferta.getBeneficios());
        response.setRequisitos(oferta.getRequisitos());
        response.setHabilidadesRequeridas(oferta.getHabilidadesRequeridas());
        response.setIdiomas(oferta.getIdiomas());

        // Fechas y estado
        response.setFechaPublicacion(oferta.getFechaPublicacion());
        response.setFechaCierre(oferta.getFechaCierre());
        response.setFechaActualizacion(oferta.getFechaActualizacion());
        response.setEstado(oferta.getEstado().name());
        response.setEstadoDescripcion(oferta.getEstado().getDescripcion());

        // Configuración
        response.setVacantesDisponibles(oferta.getVacantesDisponibles());
        response.setAplicacionRapida(oferta.getAplicacionRapida());
        response.setPreguntasAdicionales(oferta.getPreguntasAdicionales());

        // Información adicional
        response.setUrgente(oferta.getUrgente());
        response.setDestacada(oferta.getDestacada());
        response.setEtiquetas(oferta.getEtiquetasLista());
        response.setPermiteAplicacionExterna(oferta.getPermiteAplicacionExterna());
        response.setUrlAplicacionExterna(oferta.getUrlAplicacionExterna());

        // Estadísticas
        response.setVistas(oferta.getVistas());
        response.setAplicacionesRecibidas(oferta.getAplicacionesRecibidas());

        // Campos calculados
        response.setIsActiva(oferta.isActiva());
        response.setIsPausada(oferta.isPausada());
        response.setIsCerrada(oferta.isCerrada());
        response.setIsExpirada(oferta.isExpirada());
        response.setTiempoPublicacion(calcularTiempoPublicacion(oferta.getFechaPublicacion()));
        response.setDiasParaCierre(calcularDiasParaCierre(oferta.getFechaCierre()));

        return response;
    }


    private OfertaFeedResponse mapearAFeedResponse(OfertaLaboral oferta) {
        OfertaFeedResponse response = new OfertaFeedResponse();

        response.setId(oferta.getId());
        response.setTitulo(oferta.getTitulo());
        response.setDescripcion(limitarTexto(oferta.getDescripcion(), 200));
        response.setUbicacion(oferta.getUbicacion());

        // Empresa
        response.setEmpresaNombre(oferta.getEmpresa().getNombreEmpresa());
        response.setEmpresaDescripcion(limitarTexto(oferta.getEmpresa().getDescripcion(), 150));

        // Detalles básicos
        response.setTipoTrabajo(oferta.getTipoTrabajo() != null ?
                oferta.getTipoTrabajo().getDescripcion() : null);
        response.setTipoContrato(oferta.getTipoContrato() != null ?
                oferta.getTipoContrato().getDescripcion() : null);
        response.setNivelExperiencia(oferta.getNivelExperiencia() != null ?
                oferta.getNivelExperiencia().getDescripcion() : null);
        response.setAreaTrabajo(oferta.getAreaTrabajo());

        // Salario
        response.setSalarioFormateado(oferta.getSalarioFormateado());
        response.setMostrarSalario(oferta.getMostrarSalario());

        // Fechas
        response.setFechaPublicacion(oferta.getFechaPublicacion());
        response.setTiempoPublicacion(calcularTiempoPublicacion(oferta.getFechaPublicacion()));
        response.setDiasParaCierre(calcularDiasParaCierre(oferta.getFechaCierre()));

        // UI/UX
        response.setUrgente(oferta.getUrgente());
        response.setDestacada(oferta.getDestacada());
        response.setEtiquetas(oferta.getEtiquetasLista());
        response.setAplicacionRapida(oferta.getAplicacionRapida());

        // Estadísticas
        response.setVistas(oferta.getVistas());
        response.setAplicacionesRecibidas(oferta.getAplicacionesRecibidas());

        // Estado
        response.setIsActiva(oferta.isActiva());

        return response;
    }

    private String calcularTiempoPublicacion(LocalDateTime fechaPublicacion) {
        if (fechaPublicacion == null) return "Fecha no disponible";

        LocalDateTime ahora = LocalDateTime.now();
        long minutos = ChronoUnit.MINUTES.between(fechaPublicacion, ahora);
        long horas = ChronoUnit.HOURS.between(fechaPublicacion, ahora);
        long dias = ChronoUnit.DAYS.between(fechaPublicacion, ahora);
        long semanas = ChronoUnit.WEEKS.between(fechaPublicacion, ahora);

        if (minutos < 60) {
            return minutos <= 1 ? "Hace un momento" : "Hace " + minutos + " minutos";
        } else if (horas < 24) {
            return horas == 1 ? "Hace 1 hora" : "Hace " + horas + " horas";
        } else if (dias < 7) {
            return dias == 1 ? "Hace 1 día" : "Hace " + dias + " días";
        } else if (semanas < 4) {
            return semanas == 1 ? "Hace 1 semana" : "Hace " + semanas + " semanas";
        } else {
            return fechaPublicacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    private Integer calcularDiasParaCierre(LocalDateTime fechaCierre) {
        if (fechaCierre == null) return null;

        LocalDateTime ahora = LocalDateTime.now();
        long dias = ChronoUnit.DAYS.between(ahora, fechaCierre);

        return dias < 0 ? 0 : (int) dias;
    }

    private String limitarTexto(String texto, int limite) {
        if (texto == null) return null;
        if (texto.length() <= limite) return texto;
        return texto.substring(0, limite) + "...";
    }

    private Empresa obtenerEmpresaDelUsuario(Usuario usuario) {
        return empresaRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Usuario no está asociado a ninguna empresa"));
    }



    public Page<OfertaResponse> obtenerOfertasEmpresa(Long id, Pageable pageable) {

    // Mas personalizacion a futuro
       return ofertaRepository.findByEmpresa_EmpresaId(id, pageable)
               .map(this::mapearaOfertaResponse);
   }

   public OfertaResponse mapearaOfertaResponse(OfertaLaboral ofertaLaboral) {
        OfertaResponse oferta = new OfertaResponse();
        oferta.setId(ofertaLaboral.getId());
        oferta.setTitulo(ofertaLaboral.getTitulo());
        oferta.setDescripcion(limitarTexto(ofertaLaboral.getDescripcion(), 200));
        oferta.setUbicacion(ofertaLaboral.getUbicacion());

        // Empresa
        oferta.setEmpresaNombre(ofertaLaboral.getEmpresa().getNombreEmpresa());
        oferta.setEmpresaDescripcion(limitarTexto(ofertaLaboral.getEmpresa().getDescripcion(), 150));

        // Detalles básicos
        oferta.setTipoTrabajo(ofertaLaboral.getTipoTrabajo() != null ?
                ofertaLaboral.getTipoTrabajo().getDescripcion() : null);
        oferta.setTipoContrato(ofertaLaboral.getTipoContrato() != null ?
                ofertaLaboral.getTipoContrato().getDescripcion() : null);
        oferta.setNivelExperiencia(ofertaLaboral.getNivelExperiencia() != null ?
                ofertaLaboral.getNivelExperiencia().getDescripcion() : null);
        oferta.setAreaTrabajo(ofertaLaboral.getAreaTrabajo());

        // Salario
        oferta.setSalarioFormateado(ofertaLaboral.getSalarioFormateado());
        oferta.setMostrarSalario(ofertaLaboral.getMostrarSalario());

        // Fechas
        oferta.setFechaPublicacion(ofertaLaboral.getFechaPublicacion());
        oferta.setTiempoPublicacion(calcularTiempoPublicacion(ofertaLaboral.getFechaPublicacion()));
        oferta.setDiasParaCierre(calcularDiasParaCierre(ofertaLaboral.getFechaCierre()));

        // UI/UX
        oferta.setUrgente(ofertaLaboral.getUrgente());
        oferta.setDestacada(ofertaLaboral.getDestacada());
        oferta.setEtiquetas(ofertaLaboral.getEtiquetasLista());
        oferta.setAplicacionRapida(ofertaLaboral.getAplicacionRapida());

        // Estadísticas
        oferta.setVistas(ofertaLaboral.getVistas());
        oferta.setAplicacionesRecibidas(ofertaLaboral.getAplicacionesRecibidas());

        // Estado
        oferta.setIsActiva(ofertaLaboral.isActiva());
        return oferta;
    }

}