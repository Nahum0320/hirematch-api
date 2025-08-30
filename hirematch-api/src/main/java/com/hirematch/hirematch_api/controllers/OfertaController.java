package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.CrearOfertaRequest;
import com.hirematch.hirematch_api.DTO.OfertaResponse;
import com.hirematch.hirematch_api.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ofertas")
public class OfertaController {

    private final OfertaService ofertaService;

    public OfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    @PostMapping
    public OfertaResponse crearOferta(@Valid @RequestBody CrearOfertaRequest request) {
        // Aquí puedes agregar validación de que el usuario logueado sea de tipo EMPRESA
        return ofertaService.crearOferta(request);
    }

    @GetMapping("/feed")
    public Page<OfertaResponse> obtenerFeed(Pageable pageable) {
        return ofertaService.obtenerFeed(pageable);
    }
}

