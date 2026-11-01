
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.DetalleVentaRequest;
import ies.belgrano.almacen.dto.VentaRequest;
import ies.belgrano.almacen.dto.VentaResponse;

import ies.belgrano.almacen.model.Producto;

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

        // =====================================================
        // PRUEBA 1: VENTA CON STOCK INSUFICIENTE
        // La venta debe registrarse y dejar stock negativo.
        // =====================================================

        @Test
        void registrarVenta_stockInsuficientePermiteVentaConAdvertencia() {

                long ventasAntes = ventaRepository.count();
                long detallesAntes = detalleVentaRepository.count();

                Producto arroz = new Producto();
                arroz.setNombre("Arroz stock insuficiente");
                arroz.setPrecioVenta(new BigDecimal("1800.00"));
                arroz.setStock(10.0);
                arroz = productoRepository.save(arroz);

                Producto azucar = new Producto();
                azucar.setNombre("Azucar stock insuficiente");
                azucar.setPrecioVenta(new BigDecimal("1200.00"));
                azucar.setStock(5.0);
                azucar = productoRepository.save(azucar);

                DetalleVentaRequest detalleArroz = new DetalleVentaRequest();
                detalleArroz.setIdProducto(arroz.getIdProducto());
                detalleArroz.setCantidad(new BigDecimal("2"));

                DetalleVentaRequest detalleAzucar = new DetalleVentaRequest();
                detalleAzucar.setIdProducto(azucar.getIdProducto());
                detalleAzucar.setCantidad(new BigDecimal("10"));

                VentaRequest request = new VentaRequest();
                request.setIdCliente(null);
                request.setPorcentajeDescuento(BigDecimal.ZERO);
                request.setDetalles(List.of(detalleArroz, detalleAzucar));

                // La venta debe completarse, sin lanzar excepción.
                VentaResponse respuesta = ventaService.registrarVenta(request);

                assertNotNull(respuesta);
                assertNotNull(respuesta.getIdVenta());

                // 2 arroces x 1800 + 10 azúcares x 1200 = 15600.
                assertEquals(
                                0,
                                new BigDecimal("15600.00")
                                                .compareTo(respuesta.getTotal()));

                // Se deben guardar los dos detalles.
                assertNotNull(respuesta.getDetalles());
                assertEquals(2, respuesta.getDetalles().size());

                // Debe generarse una advertencia para el azúcar.
                assertNotNull(respuesta.getAdvertencias());
                assertEquals(1, respuesta.getAdvertencias().size());

                assertTrue(
                                respuesta.getAdvertencias().get(0)
                                                .contains("Azucar stock insuficiente"));

                // Comprobar los valores finales en la base de datos.
                Producto arrozDespues = productoRepository
                                .findById(arroz.getIdProducto())
                                .orElseThrow();

                Producto azucarDespues = productoRepository
                                .findById(azucar.getIdProducto())
                                .orElseThrow();

                assertEquals(8.0, arrozDespues.getStock());
                assertEquals(-5.0, azucarDespues.getStock());

                // La venta y sus detalles deben estar guardados.
                assertEquals(ventasAntes + 1, ventaRepository.count());
                assertEquals(detallesAntes + 2, detalleVentaRepository.count());

                assertTrue(
                                ventaRepository.existsById(respuesta.getIdVenta()));
        }

        // =====================================================
        // PRUEBA 2: REGISTRO EXITOSO DE UNA VENTA
        // Cuando alcanza el stock, no debe haber advertencias.
        // =====================================================

        @Test
        void registrarVenta_exitosaGuardaDetallesYActualizaStock() {

                long ventasAntes = ventaRepository.count();
                long detallesAntes = detalleVentaRepository.count();

                Producto arroz = new Producto();
                arroz.setNombre("Arroz venta exitosa");
                arroz.setPrecioVenta(new BigDecimal("1800.00"));
                arroz.setStock(10.0);
                arroz = productoRepository.save(arroz);

                Producto azucar = new Producto();
                azucar.setNombre("Azucar venta exitosa");
                azucar.setPrecioVenta(new BigDecimal("1200.00"));
                azucar.setStock(8.0);
                azucar = productoRepository.save(azucar);

                DetalleVentaRequest detalleArroz = new DetalleVentaRequest();
                detalleArroz.setIdProducto(arroz.getIdProducto());
                detalleArroz.setCantidad(new BigDecimal("2"));

                DetalleVentaRequest detalleAzucar = new DetalleVentaRequest();
                detalleAzucar.setIdProducto(azucar.getIdProducto());
                detalleAzucar.setCantidad(new BigDecimal("3"));

                VentaRequest request = new VentaRequest();
                request.setIdCliente(null);
                request.setPorcentajeDescuento(new BigDecimal("10"));
                request.setDetalles(List.of(detalleArroz, detalleAzucar));

                VentaResponse respuesta = ventaService.registrarVenta(request);

                assertNotNull(respuesta);
                assertNotNull(respuesta.getIdVenta());

                assertEquals(
                                0,
                                new BigDecimal("7200.00")
                                                .compareTo(respuesta.getSubtotal()));

                assertEquals(
                                0,
                                new BigDecimal("720.00")
                                                .compareTo(respuesta.getMontoDescuento()));

                assertEquals(
                                0,
                                new BigDecimal("6480.00")
                                                .compareTo(respuesta.getTotal()));

                assertNotNull(respuesta.getDetalles());
                assertEquals(2, respuesta.getDetalles().size());

                // No debe haber advertencias.
                assertNotNull(respuesta.getAdvertencias());
                assertTrue(respuesta.getAdvertencias().isEmpty());

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
                                ventaRepository.existsById(respuesta.getIdVenta()));
        }

        // =====================================================
        // PRUEBA 3: MISMO PRODUCTO REPETIDO
        // Se deben descontar ambas cantidades.
        // =====================================================

        @Test
        void registrarVenta_productoRepetidoPermiteStockNegativo() {

                long ventasAntes = ventaRepository.count();
                long detallesAntes = detalleVentaRepository.count();

                Producto arroz = new Producto();
                arroz.setNombre("Arroz producto repetido");
                arroz.setPrecioVenta(new BigDecimal("1800.00"));
                arroz.setStock(10.0);
                arroz = productoRepository.save(arroz);

                // Primer detalle: 7 unidades.
                DetalleVentaRequest primerDetalle = new DetalleVentaRequest();
                primerDetalle.setIdProducto(arroz.getIdProducto());
                primerDetalle.setCantidad(new BigDecimal("7"));

                // Segundo detalle: 6 unidades del mismo producto.
                DetalleVentaRequest segundoDetalle = new DetalleVentaRequest();
                segundoDetalle.setIdProducto(arroz.getIdProducto());
                segundoDetalle.setCantidad(new BigDecimal("6"));

                // Total vendido: 13 unidades.
                // Stock inicial: 10 unidades.
                // Stock final esperado: -3 unidades.

                VentaRequest request = new VentaRequest();
                request.setIdCliente(null);
                request.setPorcentajeDescuento(BigDecimal.ZERO);
                request.setDetalles(List.of(primerDetalle, segundoDetalle));

                VentaResponse respuesta = ventaService.registrarVenta(request);

                assertNotNull(respuesta);
                assertNotNull(respuesta.getIdVenta());

                // 13 x 1800 = 23400.
                assertEquals(
                                0,
                                new BigDecimal("23400.00")
                                                .compareTo(respuesta.getTotal()));

                assertNotNull(respuesta.getDetalles());
                assertEquals(2, respuesta.getDetalles().size());

                // El segundo detalle deja el stock negativo.
                assertNotNull(respuesta.getAdvertencias());
                assertEquals(1, respuesta.getAdvertencias().size());

                assertTrue(
                                respuesta.getAdvertencias().get(0)
                                                .contains("Arroz producto repetido"));

                Producto arrozDespues = productoRepository
                                .findById(arroz.getIdProducto())
                                .orElseThrow();

                assertEquals(-3.0, arrozDespues.getStock());

                assertEquals(ventasAntes + 1, ventaRepository.count());
                assertEquals(detallesAntes + 2, detalleVentaRepository.count());

                assertTrue(
                                ventaRepository.existsById(respuesta.getIdVenta()));
        }
}
