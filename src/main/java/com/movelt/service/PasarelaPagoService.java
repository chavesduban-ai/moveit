package com.movelt.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PasarelaPagoService {

    public String generarReferencia(Long pedidoId) {
        return "MOV-" + pedidoId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public ResultadoPago procesarPago(Long pedidoId, String numeroTarjeta) {
        String limpio = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("\\s+", "");
        if (limpio.length() < 13 || !limpio.matches("\\d+")) {
            return new ResultadoPago(false, null, "Número de tarjeta inválido");
        }
        if (limpio.endsWith("0000")) {
            return new ResultadoPago(false, null, "Pago rechazado por el banco emisor");
        }
        String ultimos = limpio.substring(limpio.length() - 4);
        String referencia = generarReferencia(pedidoId);
        return new ResultadoPago(true, referencia, "Pago aprobado", ultimos);
    }

    public static class ResultadoPago {
        private final boolean aprobado;
        private final String referencia;
        private final String mensaje;
        private final String ultimosDigitos;

        public ResultadoPago(boolean aprobado, String referencia, String mensaje) {
            this(aprobado, referencia, mensaje, null);
        }

        public ResultadoPago(boolean aprobado, String referencia, String mensaje, String ultimosDigitos) {
            this.aprobado = aprobado;
            this.referencia = referencia;
            this.mensaje = mensaje;
            this.ultimosDigitos = ultimosDigitos;
        }

        public boolean isAprobado() { return aprobado; }
        public String getReferencia() { return referencia; }
        public String getMensaje() { return mensaje; }
        public String getUltimosDigitos() { return ultimosDigitos; }
    }
}
