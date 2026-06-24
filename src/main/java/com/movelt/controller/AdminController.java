package com.movelt.controller;

import com.movelt.model.*;
import com.movelt.model.enums.*;
import com.movelt.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final EmailService emailService;
    private final ReportService reportService;
    private final ComisionService comisionService;
    private final GeoDatosService geoDatosService;

    public AdminController(UsuarioService usuarioService, PedidoService pedidoService,
                           EmailService emailService, ReportService reportService,
                           ComisionService comisionService, GeoDatosService geoDatosService) {
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;
        this.emailService = emailService;
        this.reportService = reportService;
        this.comisionService = comisionService;
        this.geoDatosService = geoDatosService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails user, Model model) {
        model.addAttribute("nombreAdmin", user.getUsername());
        model.addAttribute("pedidosActivos", pedidoService.contarActivos());
        model.addAttribute("totalRepartidores", usuarioService.contarPorRol(Rol.repartidor));
        model.addAttribute("totalClientes", usuarioService.contarPorRol(Rol.cliente));
        model.addAttribute("totalPedidos", pedidoService.contarTotal());
        model.addAttribute("pedidos", pedidoService.listarTodos());
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("repartidores", usuarioService.listarPorRol(Rol.repartidor));
        model.addAttribute("gananciasPlataforma", pedidoService.totalGananciaPlataforma());
        model.addAttribute("gananciasRepartidores", pedidoService.totalGananciaRepartidores());
        model.addAttribute("pctPlataforma", comisionService.getPorcentajePlataforma());
        model.addAttribute("pctRepartidor", comisionService.getPorcentajeRepartidor());
        return "admin/dashboard";
    }

    @GetMapping("/geodatos")
    public String geodatos(Model model) {
        model.addAttribute("demandaZonas", geoDatosService.demandaPorZona());
        model.addAttribute("statsOperacion", geoDatosService.estadisticasOperacion());
        model.addAttribute("puntosEntrega", geoDatosService.puntosEntrega());
        model.addAttribute("gananciasPlataforma", pedidoService.totalGananciaPlataforma());
        model.addAttribute("gananciasRepartidores", pedidoService.totalGananciaRepartidores());
        return "admin/geodatos";
    }

    @PostMapping("/usuario/crear")
    public String crearUsuario(@RequestParam String usuario, @RequestParam String email,
                               @RequestParam String telefono, @RequestParam String password,
                               @RequestParam String rol, RedirectAttributes flash) {
        try {
            Usuario u = Usuario.builder()
                .usuario(usuario).email(email).telefono(telefono)
                .clave(password).rol(Rol.valueOf(rol)).build();
            usuarioService.guardar(u);
            flash.addFlashAttribute("mensaje", "Usuario '" + usuario + "' creado como " + rol);
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/usuario/editar")
    public String editarUsuario(@AuthenticationPrincipal UserDetails user,
                                @RequestParam Long id, @RequestParam String usuario,
                                @RequestParam String email, @RequestParam String telefono,
                                @RequestParam String rol, RedirectAttributes flash) {
        try {
            Usuario admin = usuarioService.buscarPorUsuario(user.getUsername()).orElseThrow();
            if (admin.getId().equals(id) && !"administrador".equals(rol)) {
                flash.addFlashAttribute("error", "No puedes quitarte el rol de administrador");
                return "redirect:/admin/dashboard";
            }

            Usuario u = usuarioService.buscarPorId(id).orElseThrow();
            u.setUsuario(usuario);
            u.setEmail(email);
            u.setTelefono(telefono);
            u.setRol(Rol.valueOf(rol));
            usuarioService.actualizar(u);
            flash.addFlashAttribute("mensaje", "Usuario actualizado. Rol: " + rol);
        } catch (Exception e) {
            String msg = e.getMessage().contains("Duplicate") ?
                "Ese usuario o email ya lo usa otra cuenta" : e.getMessage();
            flash.addFlashAttribute("error", msg);
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/usuario/eliminar")
    public String eliminarUsuario(@AuthenticationPrincipal UserDetails user,
                                  @RequestParam Long id, RedirectAttributes flash) {
        Usuario admin = usuarioService.buscarPorUsuario(user.getUsername()).orElseThrow();
        if (admin.getId().equals(id)) {
            flash.addFlashAttribute("error", "No puedes eliminar tu propia cuenta");
        } else {
            usuarioService.eliminar(id);
            flash.addFlashAttribute("mensaje", "Usuario eliminado");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/usuario/resetPassword")
    public String resetPassword(@RequestParam Long id, @RequestParam String newPassword,
                                RedirectAttributes flash) {
        try {
            if (newPassword.length() < 4) {
                flash.addFlashAttribute("error", "La contraseña debe tener mínimo 4 caracteres");
                return "redirect:/admin/dashboard";
            }
            Usuario u = usuarioService.buscarPorId(id).orElseThrow();
            u.setClave(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(newPassword));
            usuarioService.actualizar(u);
            flash.addFlashAttribute("mensaje", "Contraseña de '" + u.getUsuario() + "' actualizada correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al cambiar contraseña: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/correos")
    public String correosForm(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/correos";
    }

    @PostMapping("/correos/enviar")
    public String enviarCorreos(@RequestParam String destinatarios,
                                @RequestParam String asunto,
                                @RequestParam String mensaje,
                                RedirectAttributes flash, Model model) {
        List<String> emails;
        switch (destinatarios) {
            case "clientes" -> emails = usuarioService.listarPorRol(Rol.cliente).stream()
                    .map(Usuario::getEmail).filter(e -> !e.isBlank()).collect(Collectors.toList());
            case "repartidores" -> emails = usuarioService.listarPorRol(Rol.repartidor).stream()
                    .map(Usuario::getEmail).filter(e -> !e.isBlank()).collect(Collectors.toList());
            default -> emails = usuarioService.listarTodos().stream()
                    .map(Usuario::getEmail).filter(e -> !e.isBlank()).collect(Collectors.toList());
        }

        if (emails.isEmpty()) {
            flash.addFlashAttribute("error", "No hay destinatarios con email");
            return "redirect:/admin/correos";
        }

        emailService.enviarMasivoAsync(emails, asunto, mensaje);
        flash.addFlashAttribute("mensaje", "Envío iniciado a " + emails.size() + " destinatarios. Los correos se están enviando en segundo plano.");
        return "redirect:/admin/correos";
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        List<Pedido> pedidos = pedidoService.listarTodos();
        long totalPedidos = pedidos.size();
        long pedidosEntregados = pedidos.stream().filter(p -> p.getEstado() == EstadoPedido.entregado).count();
        long pedidosActivos = pedidoService.contarActivos();

        Map<String, Long> porServicio = new LinkedHashMap<>();
        Map<String, Integer> ingresosPorServicio = new LinkedHashMap<>();
        for (Servicio s : Servicio.values()) {
            porServicio.put(s.name(), pedidos.stream().filter(p -> p.getServicio() == s).count());
            ingresosPorServicio.put(s.name(),
                pedidos.stream()
                    .filter(p -> p.getServicio() == s && p.getEstado() == EstadoPedido.entregado)
                    .mapToInt(p -> p.getServicio().getPrecio())
                    .sum());
        }

        int totalIngresos = ingresosPorServicio.values().stream().mapToInt(Integer::intValue).sum();

        Map<String, Long> topRepartidores = pedidos.stream()
            .filter(p -> p.getEstado() == EstadoPedido.entregado && p.getRepartidorAsignado() != null)
            .collect(Collectors.groupingBy(
                p -> p.getRepartidorAsignado().getUsuario(),
                Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (a, b) -> a, LinkedHashMap::new));

        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("pedidosEntregados", pedidosEntregados);
        model.addAttribute("pedidosActivos", pedidosActivos);
        model.addAttribute("totalClientes", usuarioService.contarPorRol(Rol.cliente));
        model.addAttribute("totalRepartidores", usuarioService.contarPorRol(Rol.repartidor));
        model.addAttribute("porServicio", porServicio);
        model.addAttribute("ingresosPorServicio", ingresosPorServicio);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("topRepartidores", topRepartidores);
        return "admin/reportes";
    }

    @GetMapping("/reportes/pdf")
    public ResponseEntity<byte[]> descargarReportePDF() {
        try {
            byte[] pdfBytes = reportService.generarReporteEstadistico();
            String nombreArchivo = "reporte-movelt-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
