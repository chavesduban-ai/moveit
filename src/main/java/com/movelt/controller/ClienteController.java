package com.movelt.controller;

import com.movelt.model.*;
import com.movelt.model.enums.EstadoPago;
import com.movelt.model.enums.MetodoPago;
import com.movelt.model.enums.Servicio;
import com.movelt.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final CalificacionService calificacionService;
    private final EmailService emailService;
    private final PasarelaPagoService pasarelaPagoService;

    public ClienteController(PedidoService pedidoService, UsuarioService usuarioService,
                             CalificacionService calificacionService, EmailService emailService,
                             PasarelaPagoService pasarelaPagoService) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
        this.calificacionService = calificacionService;
        this.emailService = emailService;
        this.pasarelaPagoService = pasarelaPagoService;
    }

    private Usuario getUsuario(UserDetails user) {
        return usuarioService.buscarPorUsuario(user.getUsername()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails user, Model model) {
        Usuario usuario = getUsuario(user);
        Pedido ultimo = pedidoService.ultimoPedidoCliente(usuario.getId());
        List<Pedido> entregados = pedidoService.pedidosEntregadosCliente(usuario.getId());
        Map<String, Object> stats = pedidoService.estadisticasCliente(usuario.getId());

        model.addAttribute("usuario", usuario);
        model.addAttribute("ultimoPedido", ultimo);
        model.addAttribute("entregados", entregados);
        model.addAttribute("stats", stats);
        return "cliente/dashboard";
    }

    @PostMapping("/pedido/crear")
    public String crearPedido(@AuthenticationPrincipal UserDetails user,
                              @RequestParam String recogida,
                              @RequestParam String entrega,
                              @RequestParam String destinatario,
                              @RequestParam String telefono,
                              @RequestParam String descripcion,
                              @RequestParam(defaultValue = "1.0") BigDecimal peso,
                              @RequestParam(defaultValue = "express") Servicio servicio,
                              @RequestParam(defaultValue = "efectivo") MetodoPago metodoPago,
                              @RequestParam(defaultValue = "") String observaciones,
                              RedirectAttributes flash) {
        Usuario cliente = getUsuario(user);
        Pedido p = Pedido.builder()
            .cliente(cliente).recogida(recogida).entrega(entrega)
            .destinatario(destinatario).telefono(telefono)
            .descripcion(descripcion).peso(peso)
            .servicio(servicio).observaciones(observaciones)
            .metodoPago(metodoPago)
            .build();

        Pedido creado = pedidoService.crear(p);

        if (metodoPago == MetodoPago.tarjeta) {
            return "redirect:/cliente/pago/checkout/" + creado.getId();
        }

        if (metodoPago != MetodoPago.efectivo) {
            creado.setEstadoPago(EstadoPago.pagado);
            creado = pedidoService.guardar(creado);
        }

        if (creado.getEstadoPago() == EstadoPago.pagado && cliente.getEmail() != null && !cliente.getEmail().isBlank()) {
            emailService.enviarConfirmacionPago(cliente.getEmail(), cliente.getUsuario(), creado);
            flash.addFlashAttribute("mensaje", "Pedido creado y pago confirmado. Revisa tu correo con el recibo.");
        } else {
            flash.addFlashAttribute("mensaje", "Pedido creado. Pago en efectivo contra entrega.");
        }

        return "redirect:/cliente/dashboard";
    }

    @GetMapping("/pago/checkout/{pedidoId}")
    public String checkout(@AuthenticationPrincipal UserDetails user,
                           @PathVariable Long pedidoId, Model model, RedirectAttributes flash) {
        Usuario cliente = getUsuario(user);
        Pedido pedido = pedidoService.buscarPorId(pedidoId);
        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            flash.addFlashAttribute("mensaje", "Pedido no válido");
            return "redirect:/cliente/dashboard";
        }
        if (pedido.getEstadoPago() == EstadoPago.pagado) {
            flash.addFlashAttribute("mensaje", "Este pedido ya fue pagado");
            return "redirect:/cliente/dashboard";
        }
        int monto = pedido.getServicio().getPrecio();
        model.addAttribute("pedido", pedido);
        model.addAttribute("monto", monto);
        model.addAttribute("referencia", pasarelaPagoService.generarReferencia(pedidoId));
        return "pago/checkout";
    }

    @PostMapping("/pago/procesar")
    public String procesarPago(@AuthenticationPrincipal UserDetails user,
                               @RequestParam Long pedidoId,
                               @RequestParam String numeroTarjeta,
                               RedirectAttributes flash) {
        Usuario cliente = getUsuario(user);
        Pedido pedido = pedidoService.buscarPorId(pedidoId);
        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            flash.addFlashAttribute("mensaje", "Pedido no válido");
            return "redirect:/cliente/dashboard";
        }
        if (pedido.getEstadoPago() == EstadoPago.pagado) {
            flash.addFlashAttribute("mensaje", "Este pedido ya fue pagado");
            return "redirect:/cliente/dashboard";
        }
        PasarelaPagoService.ResultadoPago resultado = pasarelaPagoService.procesarPago(pedidoId, numeroTarjeta);
        if (!resultado.isAprobado()) {
            flash.addFlashAttribute("error", resultado.getMensaje());
            return "redirect:/cliente/pago/checkout/" + pedidoId;
        }
        pedido.setEstadoPago(EstadoPago.pagado);
        Pedido actualizado = pedidoService.guardar(pedido);
        if (cliente.getEmail() != null && !cliente.getEmail().isBlank()) {
            emailService.enviarConfirmacionPago(cliente.getEmail(), cliente.getUsuario(), actualizado);
        }
        flash.addFlashAttribute("mensaje", "Pago aprobado. Referencia " + resultado.getReferencia()
            + ". Tarjeta terminada en " + resultado.getUltimosDigitos() + ". Revisa tu correo con el recibo.");
        return "redirect:/cliente/dashboard";
    }

    @PostMapping("/calificar")
    @ResponseBody
    public ResponseEntity<?> calificar(@AuthenticationPrincipal UserDetails user,
                                       @RequestParam Long pedidoId,
                                       @RequestParam int calificacion) {
        try {
            Usuario cliente = getUsuario(user);
            calificacionService.calificar(pedidoId, cliente.getId(), calificacion);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@AuthenticationPrincipal UserDetails user,
                                   @RequestParam String email,
                                   @RequestParam(defaultValue = "") String telefono,
                                   @RequestParam(defaultValue = "") String ciudad,
                                   @RequestParam(defaultValue = "") String direccion,
                                   RedirectAttributes flash) {
        Usuario u = getUsuario(user);
        u.setEmail(email);
        u.setTelefono(telefono);
        u.setCiudad(ciudad);
        u.setDireccion(direccion);
        usuarioService.actualizar(u);
        flash.addFlashAttribute("mensaje", "Perfil actualizado");
        return "redirect:/cliente/dashboard#perfil";
    }

    @PostMapping("/password/cambiar")
    @ResponseBody
    public ResponseEntity<?> cambiarPassword(@AuthenticationPrincipal UserDetails user,
                                             @RequestBody Map<String, String> body) {
        try {
            Usuario u = getUsuario(user);
            usuarioService.cambiarPassword(u.getId(), body.get("actual"), body.get("nueva"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
