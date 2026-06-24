package com.movelt.controller;

import com.movelt.service.EmailService;
import com.movelt.service.UsuarioService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final EmailService emailService;

    public AuthController(UsuarioService usuarioService, EmailService emailService) {
        this.usuarioService = usuarioService;
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null) model.addAttribute("error", "Usuario o contraseña incorrectos");
        if (logout != null) model.addAttribute("mensaje", "Sesión cerrada correctamente");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam @Email String email,
                           @RequestParam String telefono,
                           @RequestParam @Size(min = 6) String password,
                           @RequestParam(defaultValue = "") String ciudad,
                           @RequestParam(defaultValue = "") String direccion,
                           RedirectAttributes flash) {
        try {
            usuarioService.registrar(username, email, telefono, password, ciudad, direccion);
            emailService.enviarBienvenida(email, username);
            flash.addFlashAttribute("mensaje", "Registro exitoso. Revisa tu correo. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}
