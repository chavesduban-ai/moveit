package com.movelt.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WebServiceClient {

    private final WebClient webClient = WebClient.builder().build();

    public Map<String, Object> obtenerTasaCambio() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map response = webClient.get()
                .uri("https://open.er-api.com/v6/latest/COP")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .block();

            if (response != null && response.containsKey("rates")) {
                Map rates = (Map) response.get("rates");
                Double usd = ((Number) rates.get("USD")).doubleValue();
                result.put("success", true);
                result.put("fallback", false);
                result.put("tasaUSD", usd);
                result.put("fuente", "API en vivo");
                return result;
            }
            return tasaCambioFallback("Respuesta inválida del servicio");
        } catch (Exception e) {
            return tasaCambioFallback(e.getMessage());
        }
    }

    private Map<String, Object> tasaCambioFallback(String errorMsg) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("fallback", true);
        result.put("tasaUSD", 0.00024);
        result.put("fuente", "Datos de respaldo local");
        result.put("errorOriginal", errorMsg != null ? errorMsg : "Servicio no disponible");
        return result;
    }

    public Map<String, Object> obtenerGeolocalizacion() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map response = webClient.get()
                .uri("https://ipapi.co/json/")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .block();

            if (response != null && !response.containsKey("error")) {
                result.put("success", true);
                result.put("fallback", false);
                result.put("data", response);
                result.put("fuente", "API en vivo");
                return result;
            }
            return geolocalizacionFallback("Respuesta inválida del servicio");
        } catch (Exception e) {
            return geolocalizacionFallback(e.getMessage());
        }
    }

    private Map<String, Object> geolocalizacionFallback(String errorMsg) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ip", "181.62.XXX.XXX");
        data.put("city", "Bogotá");
        data.put("region", "Bogotá D.C.");
        data.put("country_name", "Colombia");
        data.put("latitude", 4.7110);
        data.put("longitude", -74.0721);
        data.put("timezone", "America/Bogota");

        result.put("success", true);
        result.put("fallback", true);
        result.put("data", data);
        result.put("fuente", "Datos de respaldo local");
        result.put("errorOriginal", errorMsg != null ? errorMsg : "Servicio no disponible");
        return result;
    }
}
