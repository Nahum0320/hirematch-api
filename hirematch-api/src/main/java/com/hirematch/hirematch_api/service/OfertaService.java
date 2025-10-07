package com.hirematch.hirematch_api.service;
import com.hirematch.hirematch_api.DTO.CrearOfertaRequest;
import com.hirematch.hirematch_api.DTO.OfertaResponse;
import com.hirematch.hirematch_api.DTO.OfertaFeedResponse;
import com.hirematch.hirematch_api.DTO.EstadisticasOfertaResponse;
import com.hirematch.hirematch_api.DTO.EstadisticasEmpresaResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.*;
import com.hirematch.hirematch_api.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
@Service
public class OfertaService {
    private final OfertaLaboralRepository ofertaRepository;
    private final EmpresaRepository empresaRepository;
    private final PerfilRepository perfilRepository;
    private final PostulantePorOfertaRepository postulantePorOfertaRepository;
    private final PassRepository passRepository;
    private final ChatRepository chatRepository;
    private final UsuarioBadgeRepository usuarioBadgeRepository;
    private final OfertaGuardadaRepository ofertaGuardadaRepository;

    public OfertaService(OfertaLaboralRepository ofertaRepository, EmpresaRepository empresaRepository, PerfilRepository perfilRepository, PostulantePorOfertaRepository postulantePorOfertaRepository, PassRepository passRepository, ChatRepository chatRepository, UsuarioBadgeRepository usuarioBadgeRepository, OfertaGuardadaRepository ofertaGuardadaRepository) {
        this.ofertaRepository = ofertaRepository;
        this.empresaRepository = empresaRepository;
        this.perfilRepository = perfilRepository;
        this.postulantePorOfertaRepository = postulantePorOfertaRepository;
        this.passRepository = passRepository;
        this.chatRepository = chatRepository;
        this.usuarioBadgeRepository = usuarioBadgeRepository;
        this.ofertaGuardadaRepository = ofertaGuardadaRepository;
    }

    //@Transactional
    public OfertaResponse guardarOferta(Long ofertaId, Usuario usuario) {
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));
        if (!"POSTULANTE".equalsIgnoreCase(perfil.getTipoPerfil())) {
            throw new ValidacionException("Solo postulantes pueden guardar ofertas");
        }
        OfertaLaboral oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));
        if (!oferta.isActiva()) {
            throw new ValidacionException("No se puede guardar una oferta inactiva");
        }

        Optional<OfertaGuardada> existing = ofertaGuardadaRepository.findByPerfilAndOferta(perfil, oferta);
        if (existing.isPresent()) {
            // Unsave the offer
            ofertaGuardadaRepository.delete(existing.get());
            return mapearAOfertaResponse(oferta);
        }

        OfertaGuardada ofertaGuardada = new OfertaGuardada();
        ofertaGuardada.setPerfil(perfil);
        ofertaGuardada.setOferta(oferta);
        ofertaGuardada.setFechaGuardado(LocalDateTime.now());
        ofertaGuardadaRepository.save(ofertaGuardada);

        return mapearAOfertaResponse(oferta);
    }

    //@Transactional(readOnly = true)
    public Page<OfertaResponse> obtenerOfertasGuardadas(Usuario usuario, Pageable pageable) {
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));
        if (!"POSTULANTE".equalsIgnoreCase(perfil.getTipoPerfil())) {
            throw new ValidacionException("Solo postulantes pueden ver ofertas guardadas");
        }
        return ofertaGuardadaRepository.findByPerfilOrderByFechaGuardadoDesc(perfil, pageable)
                .map(ofertaGuardada -> mapearAOfertaResponse(ofertaGuardada.getOferta()));
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
    public boolean perteneceAUsuario(Long ofertaId, Usuario usuario) {
        OfertaLaboral oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));
        return oferta.getEmpresa().getUsuario().equals(usuario);
    }
    public OfertaResponse updateOferta(Long id, CrearOfertaRequest request, Usuario usuarioAutenticado) {
        OfertaLaboral oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));
        Empresa empresa = obtenerEmpresaDelUsuario(usuarioAutenticado);
        if (!oferta.getEmpresa().getEmpresaId().equals(empresa.getEmpresaId())) {
            throw new ValidacionException("No tienes permiso para actualizar esta oferta");
        }
        mapearRequestAEntidad(request, oferta);
        OfertaLaboral updated = ofertaRepository.save(oferta);
        return mapearAOfertaResponse(updated);
    }
    public Page<OfertaFeedResponse> obtenerFeed(Pageable pageable) {
        return ofertaRepository.findByEstadoOrderByFechaPublicacionDesc(EstadoOferta.ACTIVA, pageable)
                .map(this::mapearAFeedResponse);
    }

    public Page<OfertaFeedResponse> obtenerFeedParaUsuario(Pageable pageable, Usuario usuario) {
        Perfil perfil = this.perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));
        Page<OfertaLaboral> page = ofertaRepository.findMatchingOffers(
                EstadoOferta.ACTIVA.name(),
                null,
                null, 
                usuario.getUsuarioId(),
                pageable
        );
        return page.map(this::mapearAFeedResponse);
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
    public void eliminarOferta(Long id, Usuario usuarioAutenticado) {
// Validar que la oferta existe
        OfertaLaboral oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));
// Verificar que el usuario autenticado sea el propietario de la oferta
        if (!oferta.getEmpresa().getUsuario().equals(usuarioAutenticado)) {
            throw new ValidacionException("No tiene permiso para eliminar esta oferta");
        }
        ofertaRepository.delete(oferta);
    }
    public EstadisticasOfertaResponse obtenerEstadisticasOferta(Long ofertaId, Usuario usuario) {
        OfertaLaboral oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));

        if (!oferta.getEmpresa().getUsuario().getUsuarioId().equals(usuario.getUsuarioId())) {
            throw new ValidacionException("No tienes permiso para ver las estadísticas de esta oferta");
        }

        // Obtener postulaciones, passes y chats
        List<PostulantePorOferta> postulaciones = postulantePorOfertaRepository.findByOfertaOrderBySuperLikeDescFechaPostulacionDesc(oferta);
        List<Pass> passes = passRepository.findByOferta_Id(oferta.getId());
        List<Chat> chats = chatRepository.findByOferta_Id(oferta.getId());


        // Crear el DTO y asignar todos los campos
        EstadisticasOfertaResponse response = new EstadisticasOfertaResponse();

        // ID de la oferta
        response.setOfertaId(ofertaId);

        // Título y descripción de la oferta
        response.setTitulo(oferta.getTitulo());
        response.setDescripcion(oferta.getDescripcion());

        // Estadísticas de postulaciones - TODAS las postulaciones, no solo superlikes
        int totalPostulaciones = postulaciones.size(); // Total de personas que se postularon
        int totalSuperlikes = (int) postulaciones.stream().filter(PostulantePorOferta::isSuperLike).count(); // Postulaciones con superlike
        int totalMatches = (int) postulaciones.stream().filter(p -> p.getEstado() == EstadoPostulacion.MATCHED).count();
        int totalRechazosEmpresa = (int) postulaciones.stream().filter(p -> p.getEstado() == EstadoPostulacion.REJECTED).count();
        int totalContactados = chats.size(); // Los contactados son los que tienen chats creados

        response.setTotalPostulaciones(totalPostulaciones);
        response.setTotalMatches(totalMatches);
        response.setTotalSuperlikes(totalSuperlikes);
        response.setTotalRechazosEmpresa(totalRechazosEmpresa);
        response.setTotalRechazosPostulante(passes.size()); // Passes = postulantes que rechazaron la oferta
        response.setTotalContactados(totalContactados);

        // Estadísticas de la oferta
        response.setVistasOferta(oferta.getVistas());
        response.setVacantesDisponibles(oferta.getVacantesDisponibles());
        response.setEstadoOferta(oferta.getEstado());
        response.setNivelExperiencia(oferta.getNivelExperiencia());
        response.setTipoTrabajo(oferta.getTipoTrabajo());
        response.setTipoContrato(oferta.getTipoContrato());

        // Estadísticas de actividad
        response.setDiasActiva(oferta.getFechaPublicacion() != null ?
            (int) ChronoUnit.DAYS.between(oferta.getFechaPublicacion(), LocalDateTime.now()) : 0);
        response.setFechaCreacion(oferta.getFechaPublicacion());
        response.setFechaActualizacion(oferta.getFechaActualizacion());

        // Estadísticas calculadas
        response.setTasaAceptacion(totalPostulaciones > 0 ? (double) totalMatches / totalPostulaciones : 0.0);

        response.setTasaRechazoEmpresa(totalPostulaciones > 0 ? (double)
        (totalRechazosEmpresa) / totalPostulaciones : 0.0);

        int totalInteracciones = totalPostulaciones + passes.size();
        response.setTasaRechazo(totalInteracciones > 0 ?
            (double) passes.size() / totalInteracciones : 0.0);

        response.setTasaContacto(totalMatches > 0 ? (double) totalContactados / totalMatches : 0.0);

        return response;
    }

    public EstadisticasEmpresaResponse obtenerEstadisticasEmpresa(Usuario usuario) {
        Empresa empresa = empresaRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("No se encontró empresa asociada al usuario"));

        return calcularEstadisticasEmpresa(empresa);
    }

    private EstadisticasEmpresaResponse calcularEstadisticasEmpresa(Empresa empresa) {
        // Obtener todas las ofertas de la empresa
        List<OfertaLaboral> ofertas = ofertaRepository.findByEmpresaOrderByFechaPublicacionDesc(empresa, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        // Crear el DTO y asignar información básica
        EstadisticasEmpresaResponse response = new EstadisticasEmpresaResponse();
        
        response.setEmpresaId(empresa.getEmpresaId());
        response.setNombreEmpresa(empresa.getNombreEmpresa());
        response.setDescripcion(empresa.getDescripcion());
        response.setSitioWeb(empresa.getSitioWeb());
        
        // Estadísticas de ofertas
        response.setTotalOfertas(ofertas.size());
        response.setOfertasActivas((int) ofertas.stream()
                .filter(o -> o.getEstado() == EstadoOferta.ACTIVA).count());
        response.setOfertasInactivas((int) ofertas.stream()
                .filter(o -> o.getEstado() == EstadoOferta.PAUSADA || o.getEstado() == EstadoOferta.CERRADA).count());
        response.setOfertasDestacadas((int) ofertas.stream()
                .filter(o -> o.getDestacada() != null && o.getDestacada()).count());
        
        // Estadísticas agregadas de todas las ofertas
        int totalPostulaciones = 0;
        int totalMatches = 0;
        int totalSuperlikes = 0;
        int totalRechazosEmpresa = 0;
        int totalRechazosPostulante = 0;
        int totalContactados = 0;
        int totalVistas = 0;
        int totalVacantes = 0;
        int totalDiasActivos = 0;
        
        for (OfertaLaboral oferta : ofertas) {
            List<PostulantePorOferta> postulaciones = postulantePorOfertaRepository
                    .findByOfertaOrderBySuperLikeDescFechaPostulacionDesc(oferta);
            List<Pass> passes = passRepository.findByOferta_Id(oferta.getId());
            List<Chat> chats = chatRepository.findByOferta_Id(oferta.getId());
            
            totalPostulaciones += postulaciones.size();
            totalMatches += (int) postulaciones.stream()
                    .filter(p -> p.getEstado() == EstadoPostulacion.MATCHED).count();
            totalSuperlikes += (int) postulaciones.stream()
                    .filter(PostulantePorOferta::isSuperLike).count();
            totalRechazosEmpresa += (int) postulaciones.stream()
                    .filter(p -> p.getEstado() == EstadoPostulacion.REJECTED).count();
            totalRechazosPostulante += passes.size();
            totalContactados += chats.size();
            totalVistas += oferta.getVistas() != null ? oferta.getVistas() : 0;
            totalVacantes += oferta.getVacantesDisponibles() != null ? oferta.getVacantesDisponibles() : 0;
            
            // Calcular días activos de cada oferta
            if (oferta.getFechaPublicacion() != null) {
                totalDiasActivos += (int) ChronoUnit.DAYS.between(oferta.getFechaPublicacion(), LocalDateTime.now());
            }
        }
        
        response.setTotalPostulaciones(totalPostulaciones);
        response.setTotalMatches(totalMatches);
        response.setTotalSuperlikes(totalSuperlikes);
        response.setTotalRechazosEmpresa(totalRechazosEmpresa);
        response.setTotalRechazosPostulante(totalRechazosPostulante);
        response.setTotalContactados(totalContactados);
        response.setTotalVistasOfertas(totalVistas);
        response.setTotalVacantesDisponibles(totalVacantes);
        
        // Estadísticas de actividad
        LocalDateTime fechaRegistro = empresa.getUsuario().getFechaRegistro();
        response.setFechaRegistro(fechaRegistro);
        response.setDiasActiva(totalDiasActivos); // Suma de días activos de todas las ofertas
        
        // Buscar la última actividad (última actualización de ofertas)
        Optional<LocalDateTime> ultimaActividad = ofertas.stream()
                .map(OfertaLaboral::getFechaActualizacion)
                .filter(fecha -> fecha != null)
                .max(LocalDateTime::compareTo);
        response.setUltimaActividad(ultimaActividad.orElse(fechaRegistro));
        
        // Estadísticas calculadas
        response.setTasaAceptacion(totalPostulaciones > 0 ? 
                (double) totalMatches / totalPostulaciones : 0.0);
        
        response.setTasaRechazoEmpresa(totalPostulaciones > 0 ? 
                (double) totalRechazosEmpresa / totalPostulaciones : 0.0);
        
        int totalInteracciones = totalPostulaciones + totalRechazosPostulante;
        response.setTasaRechazo(totalInteracciones > 0 ? 
                (double) totalRechazosPostulante / totalInteracciones : 0.0);
        
        response.setTasaContacto(totalMatches > 0 ? 
                (double) totalContactados / totalMatches : 0.0);
        
        // Estadísticas de perfil de empresa
        Integer porcentajePerfil = calcularPorcentajePerfilEmpresa(empresa);
        response.setPorcentajePerfil(porcentajePerfil);
        response.setPerfilCompletado(porcentajePerfil >= 80);
        
        // Índice de engagement
        response.setIndiceEngagement(totalVistas > 0 ? 
                (double) (totalMatches + totalContactados) / totalVistas : 0.0);
        
        // Contar badges
        Long totalBadges = usuarioBadgeRepository.countBadgesByPerfil(empresa.getPerfil());
        response.setTotalBadges(totalBadges.intValue());
        
        return response;
    }

    private Integer calcularPorcentajePerfilEmpresa(Empresa empresa) {
        int campos = 0;
        int camposCompletos = 0;

        // Campos básicos
        campos += 2; // email, tipoPerfil
        camposCompletos += 2;

        // Campos de empresa
        if (empresa.getNombreEmpresa() != null && !empresa.getNombreEmpresa().trim().isEmpty()) {
            camposCompletos++;
        }
        campos++;

        if (empresa.getDescripcion() != null && !empresa.getDescripcion().trim().isEmpty()) {
            camposCompletos++;
        }
        campos++;

        if (empresa.getSitioWeb() != null && !empresa.getSitioWeb().trim().isEmpty()) {
            camposCompletos++;
        }
        campos++;

        // Campos del perfil asociado
        Perfil perfil = empresa.getPerfil();
        if (perfil.getUbicacion() != null && !perfil.getUbicacion().trim().isEmpty()) {
            camposCompletos++;
        }
        campos++;

        if (perfil.getTelefono() != null && !perfil.getTelefono().trim().isEmpty()) {
            camposCompletos++;
        }
        campos++;

        return (int) Math.round((double) camposCompletos / campos * 100);
    }
}