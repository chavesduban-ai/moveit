package com.movelt.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.movelt.model.Pedido;
import com.movelt.model.Usuario;
import com.movelt.model.enums.EstadoPedido;
import com.movelt.model.enums.Rol;
import com.movelt.model.enums.Servicio;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    private static final Color AZUL_OSCURO = new Color(30, 42, 74);
    private static final Color AZUL_CLARO = new Color(59, 180, 229);
    private static final Color GRIS_CLARO = new Color(248, 250, 252);
    private static final Color GRIS_TEXTO = new Color(100, 116, 139);
    private static final Color VERDE = new Color(16, 185, 129);
    private static final Color ROJO = new Color(220, 38, 38);

    public ReportService(PedidoService pedidoService, UsuarioService usuarioService) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
    }

    public byte[] generarReporteEstadistico() throws Exception {
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, AZUL_OSCURO);
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, AZUL_OSCURO);
        Font fontTextoNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font fontTextoGris = FontFactory.getFont(FontFactory.HELVETICA, 9, GRIS_TEXTO);
        Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font fontCelda = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        Font fontCeldaNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, AZUL_OSCURO);

        PdfPTable encabezado = new PdfPTable(2);
        encabezado.setWidthPercentage(100);
        encabezado.setWidths(new float[]{1, 3});

        PdfPCell logoCell = new PdfPCell(new Phrase("MoveIt!", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, AZUL_CLARO)));
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        encabezado.addCell(logoCell);

        PdfPCell tituloCell = new PdfPCell();
        tituloCell.setBorder(Rectangle.NO_BORDER);
        tituloCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph tituloHeader = new Paragraph("Reporte Estadístico del Sistema", fontTitulo);
        tituloHeader.setAlignment(Element.ALIGN_RIGHT);
        tituloCell.addElement(tituloHeader);
        Paragraph fechaHeader = new Paragraph("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontTextoGris);
        fechaHeader.setAlignment(Element.ALIGN_RIGHT);
        tituloCell.addElement(fechaHeader);
        encabezado.addCell(tituloCell);
        document.add(encabezado);

        PdfPTable lineaSeparadora = new PdfPTable(1);
        lineaSeparadora.setWidthPercentage(100);
        PdfPCell lineaCell = new PdfPCell();
        lineaCell.setFixedHeight(3);
        lineaCell.setBackgroundColor(AZUL_CLARO);
        lineaCell.setBorder(Rectangle.NO_BORDER);
        lineaSeparadora.addCell(lineaCell);
        lineaSeparadora.setSpacingBefore(10);
        lineaSeparadora.setSpacingAfter(20);
        document.add(lineaSeparadora);

        List<Pedido> todosPedidos = pedidoService.listarTodos();
        List<Usuario> todosUsuarios = usuarioService.listarTodos();
        long totalPedidos = todosPedidos.size();
        long pedidosActivos = pedidoService.contarActivos();
        long pedidosEntregados = todosPedidos.stream().filter(p -> p.getEstado() == EstadoPedido.entregado).count();
        long totalClientes = usuarioService.contarPorRol(Rol.cliente);
        long totalRepartidores = usuarioService.contarPorRol(Rol.repartidor);

        Paragraph tituloResumen = new Paragraph("1. Resumen General", fontSubtitulo);
        tituloResumen.setSpacingAfter(10);
        document.add(tituloResumen);

        PdfPTable tablaResumen = new PdfPTable(4);
        tablaResumen.setWidthPercentage(100);
        tablaResumen.setSpacingAfter(20);

        agregarCeldaStat(tablaResumen, "TOTAL PEDIDOS", String.valueOf(totalPedidos), AZUL_OSCURO);
        agregarCeldaStat(tablaResumen, "PEDIDOS ACTIVOS", String.valueOf(pedidosActivos), new Color(245, 158, 11));
        agregarCeldaStat(tablaResumen, "ENTREGADOS", String.valueOf(pedidosEntregados), VERDE);
        agregarCeldaStat(tablaResumen, "USUARIOS TOTALES", String.valueOf(todosUsuarios.size()), AZUL_CLARO);
        document.add(tablaResumen);

        Paragraph tituloServicios = new Paragraph("2. Pedidos por Tipo de Servicio", fontSubtitulo);
        tituloServicios.setSpacingAfter(10);
        document.add(tituloServicios);

        Map<Servicio, Long> porServicio = new HashMap<>();
        Map<Servicio, Integer> ingresosPorServicio = new HashMap<>();
        for (Servicio s : Servicio.values()) {
            porServicio.put(s, 0L);
            ingresosPorServicio.put(s, 0);
        }
        for (Pedido p : todosPedidos) {
            porServicio.merge(p.getServicio(), 1L, Long::sum);
            if (p.getEstado() == EstadoPedido.entregado) {
                ingresosPorServicio.merge(p.getServicio(), p.getServicio().getPrecio(), Integer::sum);
            }
        }

        PdfPTable tablaServicios = new PdfPTable(4);
        tablaServicios.setWidthPercentage(100);
        tablaServicios.setWidths(new float[]{2, 2, 2, 3});
        tablaServicios.setSpacingAfter(20);
        agregarHeaderCelda(tablaServicios, "Servicio", fontHeaderTabla);
        agregarHeaderCelda(tablaServicios, "Precio Unitario", fontHeaderTabla);
        agregarHeaderCelda(tablaServicios, "Total Pedidos", fontHeaderTabla);
        agregarHeaderCelda(tablaServicios, "Ingresos Generados", fontHeaderTabla);

        for (Servicio s : Servicio.values()) {
            agregarCelda(tablaServicios, capitalizar(s.name()), fontCeldaNegrita);
            agregarCelda(tablaServicios, "$" + formatNumero(s.getPrecio()), fontCelda);
            agregarCelda(tablaServicios, String.valueOf(porServicio.get(s)), fontCelda);
            agregarCelda(tablaServicios, "$" + formatNumero(ingresosPorServicio.get(s)), fontCelda);
        }
        document.add(tablaServicios);

        Paragraph tituloEstados = new Paragraph("3. Distribución por Estado", fontSubtitulo);
        tituloEstados.setSpacingAfter(10);
        document.add(tituloEstados);

        Map<EstadoPedido, Long> porEstado = new HashMap<>();
        for (EstadoPedido e : EstadoPedido.values()) porEstado.put(e, 0L);
        for (Pedido p : todosPedidos) porEstado.merge(p.getEstado(), 1L, Long::sum);

        PdfPTable tablaEstados = new PdfPTable(3);
        tablaEstados.setWidthPercentage(100);
        tablaEstados.setSpacingAfter(20);
        agregarHeaderCelda(tablaEstados, "Estado", fontHeaderTabla);
        agregarHeaderCelda(tablaEstados, "Cantidad", fontHeaderTabla);
        agregarHeaderCelda(tablaEstados, "Porcentaje", fontHeaderTabla);
        for (EstadoPedido e : EstadoPedido.values()) {
            long cantidad = porEstado.get(e);
            double porcentaje = totalPedidos > 0 ? (cantidad * 100.0 / totalPedidos) : 0;
            agregarCelda(tablaEstados, capitalizar(e.name().replace("_", " ")), fontCeldaNegrita);
            agregarCelda(tablaEstados, String.valueOf(cantidad), fontCelda);
            agregarCelda(tablaEstados, String.format("%.1f%%", porcentaje), fontCelda);
        }
        document.add(tablaEstados);

        Paragraph tituloRepartidores = new Paragraph("4. Top Repartidores por Entregas", fontSubtitulo);
        tituloRepartidores.setSpacingAfter(10);
        document.add(tituloRepartidores);

        Map<String, Long> entregasPorRepartidor = new HashMap<>();
        Map<String, Integer> gananciasPorRepartidor = new HashMap<>();
        for (Pedido p : todosPedidos) {
            if (p.getEstado() == EstadoPedido.entregado && p.getRepartidorAsignado() != null) {
                String nombre = p.getRepartidorAsignado().getUsuario();
                entregasPorRepartidor.merge(nombre, 1L, Long::sum);
                gananciasPorRepartidor.merge(nombre, p.getServicio().getPrecio(), Integer::sum);
            }
        }

        PdfPTable tablaRepartidores = new PdfPTable(4);
        tablaRepartidores.setWidthPercentage(100);
        tablaRepartidores.setWidths(new float[]{1, 3, 2, 3});
        tablaRepartidores.setSpacingAfter(20);
        agregarHeaderCelda(tablaRepartidores, "#", fontHeaderTabla);
        agregarHeaderCelda(tablaRepartidores, "Repartidor", fontHeaderTabla);
        agregarHeaderCelda(tablaRepartidores, "Entregas", fontHeaderTabla);
        agregarHeaderCelda(tablaRepartidores, "Ganancias Totales", fontHeaderTabla);

        int[] contador = {1};
        entregasPorRepartidor.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> {
                agregarCelda(tablaRepartidores, String.valueOf(contador[0]++), fontCeldaNegrita);
                agregarCelda(tablaRepartidores, entry.getKey(), fontCelda);
                agregarCelda(tablaRepartidores, String.valueOf(entry.getValue()), fontCelda);
                agregarCelda(tablaRepartidores, "$" + formatNumero(gananciasPorRepartidor.getOrDefault(entry.getKey(), 0)), fontCelda);
            });

        if (entregasPorRepartidor.isEmpty()) {
            PdfPCell sinDatos = new PdfPCell(new Phrase("Sin datos de entregas aún", fontTextoGris));
            sinDatos.setColspan(4);
            sinDatos.setHorizontalAlignment(Element.ALIGN_CENTER);
            sinDatos.setPadding(12);
            tablaRepartidores.addCell(sinDatos);
        }
        document.add(tablaRepartidores);

        Paragraph tituloUsuarios = new Paragraph("5. Distribución de Usuarios por Rol", fontSubtitulo);
        tituloUsuarios.setSpacingAfter(10);
        document.add(tituloUsuarios);

        long admins = usuarioService.contarPorRol(Rol.administrador);
        PdfPTable tablaUsuarios = new PdfPTable(3);
        tablaUsuarios.setWidthPercentage(100);
        tablaUsuarios.setSpacingAfter(20);
        agregarHeaderCelda(tablaUsuarios, "Rol", fontHeaderTabla);
        agregarHeaderCelda(tablaUsuarios, "Cantidad", fontHeaderTabla);
        agregarHeaderCelda(tablaUsuarios, "Porcentaje", fontHeaderTabla);
        long totalUsuarios = todosUsuarios.size();
        agregarCelda(tablaUsuarios, "Administradores", fontCeldaNegrita);
        agregarCelda(tablaUsuarios, String.valueOf(admins), fontCelda);
        agregarCelda(tablaUsuarios, String.format("%.1f%%", totalUsuarios > 0 ? admins * 100.0 / totalUsuarios : 0), fontCelda);
        agregarCelda(tablaUsuarios, "Repartidores", fontCeldaNegrita);
        agregarCelda(tablaUsuarios, String.valueOf(totalRepartidores), fontCelda);
        agregarCelda(tablaUsuarios, String.format("%.1f%%", totalUsuarios > 0 ? totalRepartidores * 100.0 / totalUsuarios : 0), fontCelda);
        agregarCelda(tablaUsuarios, "Clientes", fontCeldaNegrita);
        agregarCelda(tablaUsuarios, String.valueOf(totalClientes), fontCelda);
        agregarCelda(tablaUsuarios, String.format("%.1f%%", totalUsuarios > 0 ? totalClientes * 100.0 / totalUsuarios : 0), fontCelda);
        document.add(tablaUsuarios);

        int totalIngresos = ingresosPorServicio.values().stream().mapToInt(Integer::intValue).sum();

        PdfPTable resumenFinal = new PdfPTable(1);
        resumenFinal.setWidthPercentage(100);
        resumenFinal.setSpacingBefore(20);
        PdfPCell cellResumen = new PdfPCell();
        cellResumen.setBackgroundColor(AZUL_OSCURO);
        cellResumen.setPadding(20);
        cellResumen.setBorder(Rectangle.NO_BORDER);

        Paragraph tituloFinal = new Paragraph("INGRESOS TOTALES GENERADOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(148, 163, 184)));
        tituloFinal.setAlignment(Element.ALIGN_CENTER);
        cellResumen.addElement(tituloFinal);

        Paragraph montoFinal = new Paragraph("$" + formatNumero(totalIngresos) + " COP", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, Color.WHITE));
        montoFinal.setAlignment(Element.ALIGN_CENTER);
        cellResumen.addElement(montoFinal);

        Paragraph notaFinal = new Paragraph("Solo se contabilizan pedidos entregados", FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(148, 163, 184)));
        notaFinal.setAlignment(Element.ALIGN_CENTER);
        cellResumen.addElement(notaFinal);

        resumenFinal.addCell(cellResumen);
        document.add(resumenFinal);

        Paragraph footer = new Paragraph("\n\nMoveIt! Sistema de Gestión de Domicilios — Reporte confidencial para uso interno", fontTextoGris);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    private void agregarCeldaStat(PdfPTable tabla, String label, String valor, Color color) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(15);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setBorderWidth(1);
        cell.setBackgroundColor(GRIS_CLARO);

        Paragraph lbl = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, GRIS_TEXTO));
        lbl.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(lbl);

        Paragraph val = new Paragraph(valor, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, color));
        val.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(val);

        tabla.addCell(cell);
    }

    private void agregarHeaderCelda(PdfPTable tabla, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(AZUL_OSCURO);
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorderColor(AZUL_OSCURO);
        tabla.addCell(cell);
    }

    private void agregarCelda(PdfPTable tabla, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setPadding(9);
        cell.setBorderColor(new Color(226, 232, 240));
        tabla.addCell(cell);
    }

    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String formatNumero(int numero) {
        return String.format("%,d", numero).replace(",", ".");
    }
}
