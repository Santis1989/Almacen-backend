
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.DetalleVentaRequest;
import ies.belgrano.almacen.model.Producto;
import ies.belgrano.almacen.model.Venta;
import ies.belgrano.almacen.repository.ProductoRepository;
import ies.belgrano.almacen.repository.VentaRepository;
import ies.belgrano.almacen.repository.DetalleVentaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class VentaIntegracionTest {

        @Autowired
        private VentaService ventaService;

        @Autowired
        private ProductoRepository productoRepository;

        @Autowired
        private VentaRepository ventaRepository;

        @Autowired
        private DetalleVentaRepository detalleVentaRepository;

        // PRUEBA 1:
        // Permitir una venta que deja stock negativo.
        @Test
        void registrarVenta_stockInsuficientePermiteVenta() {

                long ventasAntes = ventaRepository.count();
                long detallesAntes = detalleVentaRepository.count();

                Producto arroz = crearProducto(
                                "Arroz stock insuficiente", "1800.00", 10.0);

                Producto azucar = crearProducto(
                                "Azucar stock insuficiente", "1200.00", 5.0);

                DetalleVentaRequest detalleArroz = crearDetalle(
                                arroz.getIdProducto(), "2");

                DetalleVentaRequest detalleAzucar = crearDetalle(
                                azucar.getIdProducto(), "10");

                Venta venta = new Venta();
                venta.setPorcentajeDescuento(BigDecimal.ZERO);

                Venta respuesta = ventaService.registrarVenta(
                                venta,
                                List.of(detalleArroz, detalleAzucar));

                assertNotNull(respuesta);
                assertNotNull(respuesta.getIdVenta());

                // 2 x 1800 + 10 x 1200 = 15600.
                assertImporte("15600.00", respuesta.getSubtotal());
                assertImporte("15600.00", respuesta.getTotal());

                // Los dos detalles deben estar guardados.
                assertEquals(
                                detallesAntes + 2,
                                detalleVentaRepository.count());

                assertEquals(
                                2,
                                ventaService.listarDetalles(
                                                respuesta.getIdVenta()).size());

                Producto arrozDespues = productoRepository
                                .findById(arroz.getIdProducto())
                                .orElseThrow();

                Producto azucarDespues = productoRepository
                                .findById(azucar.getIdProducto())
                                .orElseThrow();

                assertEquals(8.0, arrozDespues.getStock());
                assertEquals(-5.0, azucarDespues.getStock());

                assertEquals(ventasAntes + 1, ventaRepository.count());

                assertTrue(
                                ventaRepository.existsById(
                                                respuesta.getIdVenta()));
        }

        // PRUEBA 2:
        // Registrar una venta con descuento del 10%.
        @Test
        void registrarVenta_exitosaGuardaDetallesYActualizaStock() {

                long ventasAntes = ventaRepository.count();
                long detallesAntes = detalleVentaRepository.count();

                Producto arroz = crearProducto(
                                "Arroz venta exitosa", "1800.00", 10.0);

                Producto azucar = crearProducto(
                                "Azucar venta exitosa", "1200.00", 8.0);

                DetalleVentaRequest detalleArroz = crearDetalle(
                                arroz.getIdProducto(), "2");

                DetalleVentaRequest detalleAzucar = crearDetalle(
                                azucar.getIdProducto(), "3");

                Venta venta = new Venta();
                venta.setPorcentajeDescuento(new BigDecimal("10"));

                Venta respuesta = ventaService.registrarVenta(
                                venta,
                                List.of(detalleArroz, detalleAzucar));

                assertNotNull(respuesta);
                assertNotNull(respuesta.getIdVenta());

                assertImporte("7200.00", respuesta.getSubtotal());
                assertImporte("10.00", respuesta.getPorcentajeDescuento());
                assertImporte("720.00", respuesta.getMontoDescuento());
                assertImporte("6480.00", respuesta.getTotal());

                assertEquals(
                                2,
                                ventaService.listarDetalles(
                                                respuesta.getIdVenta()).size());

                Producto arrozDespues = productoRepository
                                .findById(arroz.getIdProducto())
                                .orElseThrow();

                Producto azucarDespues = productoRepository
                                .findById(azucar.getIdProducto())
                                .orElseThrow();

                assertEquals(8.0, arrozDespues.getStock());
                assertEquals(5.0, azucarDespues.getStock());

                assertEquals(ventasAntes + 1, ventaRepository.count());
                assertEquals(
                                detallesAntes + 2,
                                detalleVentaRepository.count());

                assertTrue(
                                ventaRepository.existsById(
                                                respuesta.getIdVenta()));
        }

        // PRUEBA 3:
        // Descontar dos veces el mismo producto.
        @Test
        void registrarVenta_productoRepetidoPermiteStockNegativo() {

                long ventasAntes = ventaRepository.count();
                long detallesAntes = detalleVentaRepository.count();

                Producto arroz = crearProducto(
                                "Arroz producto repetido", "1800.00", 10.0);

                DetalleVentaRequest primerDetalle = crearDetalle(
                                arroz.getIdProducto(), "7");

                DetalleVentaRequest segundoDetalle = crearDetalle(
                                arroz.getIdProducto(), "6");

                Venta venta = new Venta();
                venta.setPorcentajeDescuento(BigDecimal.ZERO);

                Venta respuesta = ventaService.registrarVenta(
                                venta,
                                List.of(primerDetalle, segundoDetalle));

                assertNotNull(respuesta);
                assertNotNull(respuesta.getIdVenta());

                // 13 unidades x 1800 = 23400.
                assertImporte("23400.00", respuesta.getSubtotal());
                assertImporte("23400.00", respuesta.getTotal());

                assertEquals(
                                2,
                                ventaService.listarDetalles(
                                                respuesta.getIdVenta()).size());

                Producto arrozDespues = productoRepository
                                .findById(arroz.getIdProducto())
                                .orElseThrow();

                // Stock inicial: 10.
                // Cantidad vendida: 7 + 6 = 13.
                // Stock final: -3.
                assertEquals(-3.0, arrozDespues.getStock());

                assertEquals(ventasAntes + 1, ventaRepository.count());
                assertEquals(
                                detallesAntes + 2,
                                detalleVentaRepository.count());

                assertTrue(
                                ventaRepository.existsById(
                                                respuesta.getIdVenta()));
        }

        // MÉTODOS AUXILIARES

        private Producto crearProducto(
                        String nombre,
                        String precio,
                        Double stock) {

                Producto producto = new Producto();
                producto.setNombre(nombre);
                producto.setPrecioVenta(new BigDecimal(precio));
                producto.setStock(stock);

                return productoRepository.save(producto);
        }

        private DetalleVentaRequest crearDetalle(
                        Integer idProducto,
                        String cantidad) {

                DetalleVentaRequest detalle = new DetalleVentaRequest();
                detalle.setIdProducto(idProducto);
                detalle.setCantidad(new BigDecimal(cantidad));

                return detalle;
        }

        private void assertImporte(
                        String esperado,
                        BigDecimal obtenido) {

                assertNotNull(obtenido);

                assertEquals(
                                0,
                                new BigDecimal(esperado).compareTo(obtenido));
        }
}
