package com.movelt.controller;

import com.movelt.model.*;
import com.movelt.model.enums.EstadoUsuario;
import com.movelt.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/repartidor")
public class RepartidorController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final CalificacionService calificacionService;

    public RepartidorController(PedidoService pedidoService, UsuarioService usuarioService,
                                CalificacionService calificacionService) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
        this.calificacionService = calificacionService;
    }

    private Usuario getUsuario(UserDetails user) {
        return usuarioService.buscarPorUsuario(user.getUsername()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails user, Model model) {
        Usuario u = getUsuario(user);
        boolean activo = u.getEstado() == EstadoUsuario.activo;

        model.addAttribute("usuario", u);
        model.addAttribute("activo", activo);
        model.addAttribute("pendientes", activo ? pedidoService.pedidosPendientes() : List.of());
        model.addAttribute("asignados", pedidoService.pedidosAsignados(u.getId()));
        model.addAttribute("historial", pedidoService.historialRepartidor(u.getId()));
        model.addAttribute("stats", pedidoService.estadisticasRepartidor(u.getId()));
        model.addAttribute("promedio", String.format("%.1f", calificacionService.promedioRepartidor(u.getId())));
        model.addAttribute("totalCalif", calificacionService.totalCalificaciones(u.getId()));
        return "repartidor/dashboard";
    }

    @PostMapping("/pedido/aceptar")
    @ResponseBody
    public ResponseEntity<?> aceptar(@AuthenticationPrincipal UserDetails user,
                                     @RequestParam Long id) {
        try {
            Usuario rep = getUsuario(user);
            pedidoService.aceptarPedido(id, rep);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @PostMapping("/pedido/entregar")
    @ResponseBody
    public ResponseEntity<?> entregar(@AuthenticationPrincipal UserDetails user,
                                      @RequestParam Long id) {
        try {
            Usuario rep = getUsuario(user);
            pedidoService.entregarPedido(id, rep.getId());
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @PostMapping("/estado/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleEstado(@AuthenticationPrincipal UserDetails user,
                                          @RequestBody Map<String, String> body) {
        try {
            Usuario u = getUsuario(user);
            EstadoUsuario nuevo = EstadoUsuario.valueOf(body.get("estado"));
            u.setEstado(nuevo);
            usuarioService.actualizar(u);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/stats/ajax")
    @ResponseBody
    public ResponseEntity<?> statsAjax(@AuthenticationPrincipal UserDetails user) {
        Usuario u = getUsuario(user);
        Map<String, Object> stats = pedidoService.estadisticasRepartidor(u.getId());
        stats = new java.util.HashMap<>(stats);
        stats.put("promedio", String.format("%.1f", calificacionService.promedioRepartidor(u.getId())));
        stats.put("totalCalif", calificacionService.totalCalificaciones(u.getId()));
        return ResponseEntity.ok(stats);
    }
}
