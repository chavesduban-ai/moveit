package com.movelt.service;

import com.movelt.model.Pedido;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public CompletableFuture<Map<String, Boolean>> enviarMasivoAsync(List<String> emails, String asunto, String mensaje) {
        return CompletableFuture.completedFuture(enviarMasivo(emails, asunto, mensaje));
    }

    public Map<String, Boolean> enviarMasivo(List<String> emails, String asunto, String mensaje) {
        Map<String, Boolean> resultados = new LinkedHashMap<>();
        String html = plantillaCorreo(asunto, mensaje);

        for (String email : emails) {
            try {
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
                helper.setTo(email);
                helper.setSubject(asunto);
                helper.setText(html, true);
                mailSender.send(mime);
                resultados.put(email, true);
                Thread.sleep(300);
            } catch (Exception e) {
                resultados.put(email, false);
            }
        }
        return resultados;
    }

    @Async
    public void enviarBienvenida(String email, String usuario) {
        try {
            String contenido = "Hola <strong>" + usuario + "</strong>,<br><br>" +
                "¡Bienvenido a MoveIt! Tu cuenta ha sido creada exitosamente.<br><br>" +
                "Ya puedes iniciar sesión y comenzar a disfrutar de nuestro servicio de domicilios rápido y confiable en toda Bogotá.<br><br>" +
                "<strong>Usuario:</strong> " + usuario + "<br>" +
                "<strong>Correo:</strong> " + email + "<br><br>" +
                "Si tienes alguna duda, estamos para ayudarte.<br><br>" +
                "¡Gracias por unirte a MoveIt! 🚚";

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("¡Bienvenido a MoveIt! 🚚");
            helper.setText(plantillaCorreo("¡Bienvenido a MoveIt!", contenido), true);
            mailSender.send(mime);
        } catch (Exception e) {
            System.err.println("Error enviando correo de bienvenida: " + e.getMessage());
        }
    }

    @Async
    public void enviarConfirmacionPago(String email, String usuario, Pedido pedido) {
        try {
            String metodo = pedido.getMetodoPago().name().toUpperCase();
            String servicio = pedido.getServicio().name().toUpperCase();
            int precio = pedido.getServicio().getPrecio();
            String precioStr = String.format("%,d", precio).replace(",", ".");

            String contenido = "Hola <strong>" + usuario + "</strong>,<br><br>" +
                "Hemos recibido tu pago exitosamente. ✅<br><br>" +
                "<div style='background:#ecfdf5;padding:18px;border-radius:10px;border-left:4px solid #10b981;margin:16px 0'>" +
                "<strong style='font-size:15px;color:#065f46'>📄 Recibo de pago — Pedido #" + pedido.getId() + "</strong><br><br>" +
                "<strong>Servicio:</strong> " + servicio + "<br>" +
                "<strong>Método de pago:</strong> " + metodo + "<br>" +
                "<strong>Recogida:</strong> " + pedido.getRecogida() + "<br>" +
                "<strong>Entrega:</strong> " + pedido.getEntrega() + "<br>" +
                "<strong>Destinatario:</strong> " + pedido.getDestinatario() + "<br>" +
                "<strong style='font-size:16px;color:#047857'>Total pagado: $" + precioStr + " COP</strong>" +
                "</div>" +
                "Un repartidor tomará tu pedido pronto y te avisaremos cuando esté en camino.<br><br>" +
                "Gracias por confiar en MoveIt! 🚚";

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("✅ Pago confirmado — Pedido #" + pedido.getId());
            helper.setText(plantillaCorreo("✅ Pago Confirmado", contenido), true);
            mailSender.send(mime);
        } catch (Exception e) {
            System.err.println("Error enviando confirmación de pago: " + e.getMessage());
        }
    }

    private String plantillaCorreo(String titulo, String contenido) {
        return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f8f9fa;border-radius:12px;overflow:hidden">
            <div style="background:linear-gradient(135deg,#2c3e7d,#3bb4e5);padding:30px;text-align:center">
                <h1 style="color:#fff;margin:0;font-size:28px">🚚 MoveIt!</h1>
                <p style="color:rgba(255,255,255,0.8);margin:8px 0 0">Sistema de Domicilios</p>
            </div>
            <div style="padding:30px">
                <h2 style="color:#2c3e7d;margin-top:0">%s</h2>
                <div style="color:#555;line-height:1.8">%s</div>
            </div>
            <div style="background:#e9ecef;padding:15px;text-align:center;font-size:12px;color:#888">
                Correo enviado automáticamente por MoveIt!
            </div>
        </div>
        """.formatted(titulo, contenido.replace("\n", "<br>"));
    }
}
