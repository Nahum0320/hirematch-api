package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.DTO.CrearOfertaRequest;
import com.hirematch.hirematch_api.DTO.OfertaResponse;
import com.hirematch.hirematch_api.entity.Empresa;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
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
        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        OfertaLaboral oferta = new OfertaLaboral();
        oferta.setEmpresa(empresa);
        oferta.setTitulo(request.getTitulo());
        oferta.setDescripcion(request.getDescripcion());
        oferta.setUbicacion(request.getUbicacion());

        OfertaLaboral saved = ofertaRepository.save(oferta);

        OfertaResponse response = new OfertaResponse();
        response.setId(saved.getId());
        response.setTitulo(saved.getTitulo());
        response.setDescripcion(saved.getDescripcion());
        response.setUbicacion(saved.getUbicacion());
        response.setEmpresaNombre(empresa.getNombreEmpresa());

        return response;
    }

    public Page<OfertaResponse> obtenerFeed(Pageable pageable) {
        return ofertaRepository.findAll(pageable)
                .map(oferta -> {
                    OfertaResponse response = new OfertaResponse();
                    response.setId(oferta.getId());
                    response.setTitulo(oferta.getTitulo());
                    response.setDescripcion(oferta.getDescripcion());
                    response.setUbicacion(oferta.getUbicacion());
                    response.setEmpresaNombre(oferta.getEmpresa().getNombreEmpresa());
                    return response;
                });
    }
}

