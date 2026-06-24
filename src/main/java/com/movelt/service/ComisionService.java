package com.movelt.service;

import com.movelt.model.Pedido;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ComisionService {

    private static final BigDecimal COMISION_PLATAFORMA = new BigDecimal("0.25");

    public void calcularGanancias(Pedido pedido) {
        int precio = pedido.getServicio().getPrecio();
        BigDecimal total = BigDecimal.valueOf(precio);
        BigDecimal plataforma = total.multiply(COMISION_PLATAFORMA).setScale(0, RoundingMode.HALF_UP);
        BigDecimal repartidor = total.subtract(plataforma);
        pedido.setGananciaPlataforma(plataforma);
        pedido.setGananciaRepartidor(repartidor);
    }

    public BigDecimal getPorcentajePlataforma() {
        return COMISION_PLATAFORMA.multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getPorcentajeRepartidor() {
        return BigDecimal.ONE.subtract(COMISION_PLATAFORMA).multiply(BigDecimal.valueOf(100));
    }

    public int comisionPlataforma(int precio) {
        return BigDecimal.valueOf(precio).multiply(COMISION_PLATAFORMA).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public int gananciaRepartidor(int precio) {
        return precio - comisionPlataforma(precio);
    }
}
