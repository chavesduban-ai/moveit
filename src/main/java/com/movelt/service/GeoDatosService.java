package com.movelt.service;

import com.movelt.model.Pedido;
import com.movelt.model.enums.EstadoPedido;
import com.movelt.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeoDatosService {

    private final PedidoRepository pedidoRepository;

    public GeoDatosService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    private static final double RADIO_TIERRA_KM = 6371.0;
    private static final double VELOCIDAD_PROMEDIO_KMH = 22.0;

    public BigDecimal calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(RADIO_TIERRA_KM * c).setScale(3, RoundingMode.HALF_UP);
    }

    public Integer calcularEta(BigDecimal distanciaKm) {
        if (distanciaKm == null) return null;
        double horas = distanciaKm.doubleValue() / VELOCIDAD_PROMEDIO_KMH;
        return (int) Math.ceil(horas * 60);
    }

    public String identificarZona(double lat, double lon) {
        for (Zona z : ZONAS_BOGOTA) {
            if (lat >= z.latMin && lat <= z.latMax && lon >= z.lonMin && lon <= z.lonMax) {
                return z.nombre;
            }
        }
        return "Otra zona";
    }

    public List<Map<String, Object>> demandaPorZona() {
        List<Pedido> todos = pedidoRepository.findAll();
        Map<String, Long> conteo = todos.stream()
            .filter(p -> p.getZonaEntrega() != null)
            .collect(Collectors.groupingBy(Pedido::getZonaEntrega, Collectors.counting()));

        List<Map<String, Object>> resultado = new ArrayList<>();
        conteo.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .forEach(e -> {
                Zona z = buscarZona(e.getKey());
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("zona", e.getKey());
                m.put("pedidos", e.getValue());
                m.put("lat", z != null ? z.latCentro : 4.7110);
                m.put("lon", z != null ? z.lonCentro : -74.0721);
                resultado.add(m);
            });
        return resultado;
    }

    public Map<String, Object> estadisticasOperacion() {
        List<Pedido> todos = pedidoRepository.findAll();
        List<Pedido> entregados = todos.stream()
            .filter(p -> p.getEstado() == EstadoPedido.entregado)
            .collect(Collectors.toList());

        BigDecimal distanciaTotal = entregados.stream()
            .map(Pedido::getDistanciaKm)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        double distanciaProm = entregados.isEmpty() ? 0 :
            distanciaTotal.doubleValue() / entregados.size();

        double etaProm = entregados.stream()
            .map(Pedido::getEtaMinutos)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average().orElse(0);

        Map<String, Object> m = new HashMap<>();
        m.put("distanciaTotal", distanciaTotal.setScale(1, RoundingMode.HALF_UP));
        m.put("distanciaPromedio", BigDecimal.valueOf(distanciaProm).setScale(2, RoundingMode.HALF_UP));
        m.put("etaPromedio", (int) Math.round(etaProm));
        m.put("pedidosConGeo", entregados.stream().filter(p -> p.getDistanciaKm() != null).count());
        return m;
    }

    public List<Map<String, Object>> puntosEntrega() {
        return pedidoRepository.findAll().stream()
            .filter(p -> p.getZonaEntrega() != null)
            .map(p -> {
                Zona z = buscarZona(p.getZonaEntrega());
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", p.getId());
                m.put("zona", p.getZonaEntrega());
                m.put("estado", p.getEstado().name());
                m.put("lat", z != null ? z.latCentro : 4.7110);
                m.put("lon", z != null ? z.lonCentro : -74.0721);
                return m;
            })
            .collect(Collectors.toList());
    }

    private Zona buscarZona(String nombre) {
        for (Zona z : ZONAS_BOGOTA) {
            if (z.nombre.equals(nombre)) return z;
        }
        return null;
    }

    private static class Zona {
        final String nombre;
        final double latMin, latMax, lonMin, lonMax, latCentro, lonCentro;
        Zona(String nombre, double latMin, double latMax, double lonMin, double lonMax, double latCentro, double lonCentro) {
            this.nombre = nombre;
            this.latMin = latMin; this.latMax = latMax;
            this.lonMin = lonMin; this.lonMax = lonMax;
            this.latCentro = latCentro; this.lonCentro = lonCentro;
        }
    }

    private static final Zona[] ZONAS_BOGOTA = {
        new Zona("Usaquén",      4.690, 4.770, -74.060, -74.000, 4.7106, -74.0306),
        new Zona("Chapinero",    4.630, 4.680, -74.075, -74.030, 4.6492, -74.0628),
        new Zona("Suba",         4.730, 4.800, -74.120, -74.040, 4.7596, -74.0838),
        new Zona("Engativá",     4.680, 4.745, -74.140, -74.090, 4.7152, -74.1135),
        new Zona("Kennedy",      4.590, 4.660, -74.180, -74.120, 4.6280, -74.1545),
        new Zona("Fontibón",     4.650, 4.700, -74.165, -74.115, 4.6736, -74.1469),
        new Zona("Teusaquillo",  4.620, 4.660, -74.095, -74.060, 4.6390, -74.0840),
        new Zona("Los Mártires", 4.595, 4.625, -74.095, -74.065, 4.6040, -74.0820),
        new Zona("Centro",       4.590, 4.625, -74.080, -74.050, 4.5981, -74.0758),
        new Zona("Bosa",         4.580, 4.640, -74.220, -74.170, 4.6110, -74.1950),
        new Zona("Puente Aranda",4.610, 4.640, -74.120, -74.085, 4.6240, -74.1050),
        new Zona("Barrios Unidos",4.660,4.695, -74.090, -74.055, 4.6680, -74.0720)
    };
}
