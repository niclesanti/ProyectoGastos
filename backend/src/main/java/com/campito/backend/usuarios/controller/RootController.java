package com.campito.backend.usuarios.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String root(Authentication authentication) {
        // Si el usuario ya está autenticado (y no es el usuario anónimo por defecto),
        // lo redirigimos directamente al dashboard.
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard.html";
        }
        // Si no está autenticado, lo enviamos a la página de login.
        return "redirect:/login.html";
    }
}