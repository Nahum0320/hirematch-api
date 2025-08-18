package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.entity.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {


    //para probar funcionamiento correcto de la api
    @GetMapping("/hello")
    public String hello() {
        return "HireMatch API está funcionando 🚀";
    }

    // probar acceso a un endpoint protegido
    @GetMapping("/hello-protected")
    public String helloProtected() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            return "¡Hola " + usuario.getNombre() + "! Tu JWT está funcionando correctamente 🔐✅";
        }

        return "JWT válido, pero no se pudo obtener información del usuario 🔐";
    }
   
}
