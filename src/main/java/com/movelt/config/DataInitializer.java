package com.movelt.config;

import com.movelt.model.Pedido;
import com.movelt.model.Usuario;
import com.movelt.model.enums.*;
import com.movelt.repository.PedidoRepository;
import com.movelt.repository.UsuarioRepository;
import com.movelt.service.ComisionService;
import com.movelt.service.GeoDatosService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository repo;
    private final PedidoRepository pedidoRepo;
    private final PasswordEncoder encoder;
    private final ComisionService comisionService;
    private final GeoDatosService geoDatosService;

    public DataInitializer(UsuarioRepository repo, PedidoRepository pedidoRepo, PasswordEncoder encoder,
                           ComisionService comisionService, GeoDatosService geoDatosService) {
        this.repo = repo;
        this.pedidoRepo = pedidoRepo;
        this.encoder = encoder;
        this.comisionService = comisionService;
        this.geoDatosService = geoDatosService;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        System.out.println("=== Creando datos iniciales de MoveIt! ===");

        Usuario admin = repo.save(Usuario.builder()
            .usuario("admin").email("admin@movelt.com").telefono("3100000000")
            .clave(encoder.encode("admin123")).rol(Rol.administrador)
            .ciudad("Bogotá").estado(EstadoUsuario.activo).build());

        Usuario andres = repo.save(Usuario.builder()
            .usuario("andres").email("andres@gmail.com").telefono("3100000001")
            .clave(encoder.encode("123456")).rol(Rol.repartidor)
            .ciudad("Bogotá").estado(EstadoUsuario.activo).build());

        Usuario kevin = repo.save(Usuario.builder()
            .usuario("kevin").email("kevin@gmail.com").telefono("3100000002")
            .clave(encoder.encode("123456")).rol(Rol.repartidor)
            .ciudad("Bogotá").estado(EstadoUsuario.activo).build());

        Usuario duban = repo.save(Usuario.builder()
            .usuario("duban").email("duban@gmail.com").telefono("3100000003")
            .clave(encoder.encode("123456")).rol(Rol.cliente)
            .ciudad("Bogotá").estado(EstadoUsuario.activo).build());

        Usuario mario = repo.save(Usuario.builder()
            .usuario("mario").email("mario@gmail.com").telefono("3100000004")
            .clave(encoder.encode("123456")).rol(Rol.cliente)
            .ciudad("Bogotá").estado(EstadoUsuario.activo).build());

        pedidoRepo.save(crearPedido(duban, null, "Calle 72 #10-34", "Carrera 7 #32-16",
            "María Rodríguez", "3201234567", "Caja mediana frágil",
            Servicio.express, EstadoPedido.pendiente, MetodoPago.efectivo, EstadoPago.pendiente, 1, null));

        pedidoRepo.save(crearPedido(mario, null, "Avenida 68 #45-12", "Calle 100 #15-50",
            "Carlos Pérez", "3187654321", "Documentos importantes",
            Servicio.standard, EstadoPedido.pendiente, MetodoPago.tarjeta, EstadoPago.pagado, 2, null));

        pedidoRepo.save(crearPedido(duban, null, "Calle 45 #67-89", "Carrera 26 #7-40",
            "Laura Gómez", "3159876543", "Ropa empacada",
            Servicio.economy, EstadoPedido.pendiente, MetodoPago.nequi, EstadoPago.pagado, 3, null));

        pedidoRepo.save(crearPedido(mario, andres, "Carrera 15 #93-47", "Calle 85 #11-53",
            "Juan Martínez", "3141237890", "Paquete de libros",
            Servicio.standard, EstadoPedido.aceptado, MetodoPago.pse, EstadoPago.pagado, 4, null));

        pedidoRepo.save(crearPedido(duban, kevin, "Calle 26 #68-35", "Avenida Suba #100-22",
            "Ana Torres", "3001112233", "Regalo de cumpleaños",
            Servicio.express, EstadoPedido.aceptado, MetodoPago.tarjeta, EstadoPago.pagado, 5, null));

        pedidoRepo.save(crearPedido(duban, andres, "Calle 100 #15-50", "Carrera 13 #65-20",
            "Pedro López", "3045678912", "Medicamentos",
            Servicio.express, EstadoPedido.entregado, MetodoPago.efectivo, EstadoPago.pagado, 10,
            new BigDecimal("5.0")));

        pedidoRepo.save(crearPedido(mario, kevin, "Avenida Caracas #45-67", "Calle 72 #10-34",
            "Sofía Herrera", "3198765432", "Comida restaurante",
            Servicio.standard, EstadoPedido.entregado, MetodoPago.nequi, EstadoPago.pagado, 12,
            new BigDecimal("4.0")));

        pedidoRepo.save(crearPedido(duban, andres, "Carrera 7 #32-16", "Calle 127 #15-30",
            "Diego Ramírez", "3162345678", "Electrónicos",
            Servicio.express, EstadoPedido.entregado, MetodoPago.tarjeta, EstadoPago.pagado, 15,
            new BigDecimal("5.0")));

        pedidoRepo.save(crearPedido(mario, kevin, "Calle 93 #13-45", "Carrera 68 #90-11",
            "Valentina Castro", "3125556677", "Flores",
            Servicio.economy, EstadoPedido.entregado, MetodoPago.efectivo, EstadoPago.pagado, 20,
            new BigDecimal("4.0")));

        System.out.println("=== Datos creados: 5 usuarios + 9 pedidos de ejemplo (3 pendientes, 2 aceptados, 4 entregados) ===");
    }

    private Pedido crearPedido(Usuario cliente, Usuario repartidor, String recogida, String entrega,
                                String destinatario, String tel, String desc, Servicio serv,
                                EstadoPedido estado, MetodoPago metodo, EstadoPago estadoPago,
                                int diasAtras, BigDecimal calificacion) {
        Pedido p = new Pedido();
        p.setCliente(cliente);
        if (repartidor != null) p.setRepartidorAsignado(repartidor);
        p.setRecogida(recogida);
        p.setEntrega(entrega);
        p.setDestinatario(destinatario);
        p.setTelefono(tel);
        p.setDescripcion(desc);
        p.setPeso(new BigDecimal("1.5"));
        p.setServicio(serv);
        p.setEstado(estado);
        p.setMetodoPago(metodo);
        p.setEstadoPago(estadoPago);
        p.setFecha(LocalDateTime.now().minusDays(diasAtras));
        if (calificacion != null) p.setCalificacion(calificacion);

        double[] coordR = coordAleatoriaBogota(p.getId(), diasAtras);
        double[] coordE = coordAleatoriaBogota(p.getId(), diasAtras + 7);
        p.setZonaRecogida(geoDatosService.identificarZona(coordR[0], coordR[1]));
        p.setZonaEntrega(geoDatosService.identificarZona(coordE[0], coordE[1]));
        java.math.BigDecimal dist = geoDatosService.calcularDistancia(coordR[0], coordR[1], coordE[0], coordE[1]);
        p.setDistanciaKm(dist);
        p.setEtaMinutos(geoDatosService.calcularEta(dist));
        comisionService.calcularGanancias(p);
        return p;
    }

    private double[] coordAleatoriaBogota(Long seed, int variacion) {
        double[][] puntos = {
            {4.7106, -74.0306}, {4.6492, -74.0628}, {4.7596, -74.0838},
            {4.7152, -74.1135}, {4.6280, -74.1545}, {4.6736, -74.1469},
            {4.6390, -74.0840}, {4.6040, -74.0820}, {4.5981, -74.0758},
            {4.6680, -74.0720}, {4.6240, -74.1050}, {4.6110, -74.1950}
        };
        int idx = Math.abs((int) (variacion * 31 + (seed == null ? variacion : seed))) % puntos.length;
        return puntos[idx];
    }
}
