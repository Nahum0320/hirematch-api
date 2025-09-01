package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.DTO.CrearOfertaRequest;
import com.hirematch.hirematch_api.DTO.OfertaResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Empresa;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.EmpresaRepository;
import com.hirematch.hirematch_api.repository.OfertaLaboralRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
        Empresa empresa;

        if (request.getEmpresaId() != null) {
            // Si se especifica empresaId, usarla
            empresa = empresaRepository.findById(request.getEmpresaId())
                    .orElseThrow(() -> new ValidacionException("Empresa no encontrada"));
        } else if (usuarioAutenticado != null) {
            // Si no se especifica empresaId, buscar la empresa del usuario autenticado
            // Asumiendo que existe una relación entre Usuario y Empresa
            // Esto depende de tu modelo de datos específico
            empresa = obtenerEmpresaDelUsuario(usuarioAutenticado);
        } else {
            throw new ValidacionException("Debe especificar una empresa o estar autenticado como empresa");
        }

        OfertaLaboral oferta = new OfertaLaboral();
        oferta.setEmpresa(empresa);
        oferta.setTitulo(request.getTitulo());
        oferta.setDescripcion(request.getDescripcion());
        oferta.setUbicacion(request.getUbicacion());

        OfertaLaboral saved = ofertaRepository.save(oferta);

        return mapearAResponse(saved);
    }

    public OfertaResponse updateOferta(Long id, CrearOfertaRequest request, Usuario usuarioAutenticado) {
        // Buscar la oferta por ID
        OfertaLaboral oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));

        // Verificar que la oferta pertenece a la empresa del usuario autenticado
        Empresa empresa = obtenerEmpresaDelUsuario(usuarioAutenticado);
        if (!oferta.getEmpresa().getEmpresaId().equals(empresa.getEmpresaId())) {
            throw new ValidacionException("No tienes permiso para actualizar esta oferta");
        }

        // Actualizar los campos de la oferta
        oferta.setTitulo(request.getTitulo());
        oferta.setDescripcion(request.getDescripcion());
        oferta.setUbicacion(request.getUbicacion());

        // Guardar la oferta actualizada
        OfertaLaboral updated = ofertaRepository.save(oferta);

        return mapearAResponse(updated);
    }

    public Page<OfertaResponse> obtenerFeed(Pageable pageable) {
        return ofertaRepository.findAll(pageable)
                .map(this::mapearAResponse);
    }

    public Page<OfertaResponse> obtenerFeedParaUsuario(Pageable pageable, Usuario usuario) {
        // Aquí puedes implementar lógica de matching más sofisticada
        // Por ejemplo, filtrar por ubicación, habilidades, etc.
        return obtenerFeed(pageable);
    }

    private OfertaResponse mapearAResponse(OfertaLaboral oferta) {
        OfertaResponse response = new OfertaResponse();
        response.setId(oferta.getId());
        response.setTitulo(oferta.getTitulo());
        response.setDescripcion(oferta.getDescripcion());
        response.setUbicacion(oferta.getUbicacion());
        response.setEmpresaNombre(oferta.getEmpresa().getNombreEmpresa());
        return response;
    }

   public Page<OfertaResponse> obtenerOfertasEmpresa(Long id, Pageable pageable) {

    // Mas personalizacion a futuro
       return ofertaRepository.findByEmpresa_EmpresaId(id, pageable)
               .map(this::mapearaOfertaResponse);
   }

   public OfertaResponse mapearaOfertaResponse(OfertaLaboral ofertaLaboral) {
        OfertaResponse response = new OfertaResponse();
        response.setId(ofertaLaboral.getId());
        response.setTitulo(ofertaLaboral.getTitulo());
        response.setDescripcion(ofertaLaboral.getDescripcion());
        response.setUbicacion(ofertaLaboral.getUbicacion());
        response.setEmpresaNombre(ofertaLaboral.getEmpresa().getNombreEmpresa());
        return response;
    }

    private Empresa obtenerEmpresaDelUsuario(Usuario usuario) {
        // Opción más eficiente: buscar directamente por el objeto Usuario
        return empresaRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Usuario no está asociado a ninguna empresa"));

        // Alternativa usando el método con consulta JPQL:
        // return empresaRepository.findByUsuarioId(usuario.getUsuarioId())
        //         .orElseThrow(() -> new ValidacionException("Usuario no está asociado a ninguna empresa"));

        // Alternativa usando el método default que ya tienes:
        // return empresaRepository.findPrimeraEmpresaByUsuarioId(usuario.getUsuarioId())
        //         .orElseThrow(() -> new ValidacionException("Usuario no está asociado a ninguna empresa"));
    }
}