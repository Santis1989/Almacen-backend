
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.DetalleVentaRequest;
import ies.belgrano.almacen.dto.VentaRequest;
import ies.belgrano.almacen.exception.ResourceNotFoundException;
import ies.belgrano.almacen.model.Cliente;
import ies.belgrano.almacen.model.DetalleVenta;
import ies.belgrano.almacen.model.Producto;
import ies.belgrano.almacen.model.Venta;
import ies.belgrano.almacen.repository.ClienteRepository;
import ies.belgrano.almacen.repository.DetalleVentaRepository;
import ies.belgrano.almacen.repository.ProductoRepository;
import ies.belgrano.almacen.repository.VentaRepository;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VentaServiceTest {

        private VentaRepository ventaRepository;
        private DetalleVentaRepository detalleVentaRepository;
        private ProductoRepository productoRepository;
        private ClienteRepository clienteRepository;
        private VentaService ventaService;

        @BeforeEach
        void configurar() {
                ventaRepository = mock(VentaRepository.class);
                detalleVentaRepository = mock(DetalleVentaRepository.class);
                productoRepository = mock(ProductoRepository.class);
                clienteRepository = mock(ClienteRepository.class);

                ventaService = new VentaService(
                                ventaRepository,
                                detalleVentaRepository,
                                productoRepository,
                                clienteRepository);

                // Simulamos el guardado de una venta.
                when(ventaRepository.save(any(Venta.class)))
                                .thenAnswer(invocacion -> {
                                        Venta venta = invocacion.getArgument(0);
                                        venta.setIdVenta(1);
                                        return venta;
                                });
        }

        // PRUEBA 1: No se puede registrar una venta sin detalles.
        @Test
        void validar_ventaSinDetallesGeneraError() {

                VentaRequest request = new VentaRequest();
                request.setVenta(new Venta());
                request.setDetalles(List.of());

                try (var factory = Validation.buildDefaultValidatorFactory()) {
                        Validator validator = factory.getValidator();

                        var errores = validator.validate(request);

                        assertEquals(1, errores.size());
                }
        }

        // PRUEBA 2: La cantidad de un detalle debe ser positiva.
        @Test
        void validar_cantidadCeroGeneraError() {

                DetalleVentaRequest detalle = crearDetalle(1, "0");

                VentaRequest request = new VentaRequest();
                request.setVenta(new Venta());
                request.setDetalles(List.of(detalle));

                try (var factory = Validation.buildDefaultValidatorFactory()) {
                        Validator validator = factory.getValidator();

                        var errores = validator.validate(request);

                        assertEquals(1, errores.size());
                }
        }

        // PRUEBA 3: El stock puede quedar negativo.
        @Test
        void registrarVenta_stockInsuficientePermiteVenta() {

                Producto producto = crearProducto(
                                1, "Arroz 1kg", "1800.00", 5.0);

                when(productoRepository.findById(1))
                                .thenReturn(Optional.of(producto));

                List<DetalleVenta> detallesGuardados = prepararDetallesSimulados();

                Venta venta = new Venta();
                venta.setPorcentajeDescuento(BigDecimal.ZERO);

                DetalleVentaRequest detalle = crearDetalle(1, "10");

                Venta respuesta = ventaService.registrarVenta(
                                venta, List.of(detalle));

                assertEquals(1, respuesta.getIdVenta());
                assertImporte("18000.00", respuesta.getTotal());
                assertEquals(-5.0, producto.getStock());

                assertEquals(1, detallesGuardados.size());
                assertImporte(
                                "18000.00",
                                detallesGuardados.get(0).getSubtotal());

                verify(ventaRepository).save(any(Venta.class));
                verify(detalleVentaRepository).save(any(DetalleVenta.class));
                verify(productoRepository).save(producto);
        }

        // PRUEBA 4: Calcular descuento, total y actualizar stock.
        @Test
        void registrarVenta_calculaTotalYDescuentaStock() {

                Producto producto = crearProducto(
                                1, "Arroz 1kg", "1800.00", 5.0);

                when(productoRepository.findById(1))
                                .thenReturn(Optional.of(producto));

                List<DetalleVenta> detallesGuardados = prepararDetallesSimulados();

                Venta venta = new Venta();
                venta.setPorcentajeDescuento(new BigDecimal("10"));

                DetalleVentaRequest detalle = crearDetalle(1, "2");

                Venta respuesta = ventaService.registrarVenta(
                                venta, List.of(detalle));

                assertImporte("3600.00", respuesta.getSubtotal());
                assertImporte("10.00", respuesta.getPorcentajeDescuento());
                assertImporte("360.00", respuesta.getMontoDescuento());
                assertImporte("3240.00", respuesta.getTotal());

                assertEquals(3.0, producto.getStock());
                assertEquals(1, detallesGuardados.size());

                verify(ventaRepository).save(any(Venta.class));
                verify(detalleVentaRepository).save(any(DetalleVenta.class));
                verify(productoRepository).save(producto);
        }

        // PRUEBA 5: No se puede vender un producto inexistente.
        @Test
        void registrarVenta_productoInexistenteGeneraError() {

                when(productoRepository.findById(9999))
                                .thenReturn(Optional.empty());

                Venta venta = new Venta();
                venta.setPorcentajeDescuento(BigDecimal.ZERO);

                DetalleVentaRequest detalle = crearDetalle(9999, "1");

                ResourceNotFoundException error = assertThrows(
                                ResourceNotFoundException.class,
                                () -> ventaService.registrarVenta(
                                                venta, List.of(detalle)));

                assertEquals(
                                "Producto no encontrado: 9999",
                                error.getMessage());

                verify(ventaRepository, never()).save(any());
                verify(detalleVentaRepository, never()).save(any());
                verify(productoRepository, never()).save(any());
        }

        // PRUEBA 6: No se puede asociar un cliente inexistente.
        @Test
        void registrarVenta_clienteInexistenteGeneraError() {

                when(clienteRepository.findById(9999))
                                .thenReturn(Optional.empty());

                Cliente cliente = new Cliente();
                cliente.setIdCliente(9999);

                Venta venta = new Venta();
                venta.setCliente(cliente);
                venta.setPorcentajeDescuento(BigDecimal.ZERO);

                DetalleVentaRequest detalle = crearDetalle(1, "1");

                ResourceNotFoundException error = assertThrows(
                                ResourceNotFoundException.class,
                                () -> ventaService.registrarVenta(
                                                venta, List.of(detalle)));

                assertEquals(
                                "Cliente no encontrado: 9999",
                                error.getMessage());

                verify(ventaRepository, never()).save(any());
                verify(detalleVentaRepository, never()).save(any());
                verify(productoRepository, never()).save(any());
        }

        // PRUEBA 7: Registrar dos productos y calcular el total.
        @Test
        void registrarVenta_dosProductosCalculaTotalYActualizaStock() {

                Producto arroz = crearProducto(
                                1, "Arroz 1kg", "1800.00", 10.0);

                Producto azucar = crearProducto(
                                2, "Azucar 1kg", "1200.00", 8.0);

                when(productoRepository.findById(1))
                                .thenReturn(Optional.of(arroz));

                when(productoRepository.findById(2))
                                .thenReturn(Optional.of(azucar));

                List<DetalleVenta> detallesGuardados = prepararDetallesSimulados();

                Venta venta = new Venta();
                venta.setPorcentajeDescuento(BigDecimal.ZERO);

                DetalleVentaRequest detalleArroz = crearDetalle(1, "2");
                DetalleVentaRequest detalleAzucar = crearDetalle(2, "3");

                Venta respuesta = ventaService.registrarVenta(
                                venta, List.of(detalleArroz, detalleAzucar));

                assertImporte("7200.00", respuesta.getSubtotal());
                assertImporte("0.00", respuesta.getMontoDescuento());
                assertImporte("7200.00", respuesta.getTotal());

                assertEquals(8.0, arroz.getStock());
                assertEquals(5.0, azucar.getStock());

                assertEquals(2, detallesGuardados.size());

                verify(ventaRepository).save(any(Venta.class));
                verify(detalleVentaRepository, times(2))
                                .save(any(DetalleVenta.class));

                verify(productoRepository).save(arroz);
                verify(productoRepository).save(azucar);
        }

        // MÉTODOS AUXILIARES PARA LAS PRUEBAS

        private Producto crearProducto(
                        Integer id,
                        String nombre,
                        String precio,
                        Double stock) {

                Producto producto = new Producto();
                producto.setIdProducto(id);
                producto.setNombre(nombre);
                producto.setPrecioVenta(new BigDecimal(precio));
                producto.setStock(stock);

                return producto;
        }

        private DetalleVentaRequest crearDetalle(
                        Integer idProducto,
                        String cantidad) {

                DetalleVentaRequest detalle = new DetalleVentaRequest();
                detalle.setIdProducto(idProducto);
                detalle.setCantidad(new BigDecimal(cantidad));

                return detalle;
        }

        private List<DetalleVenta> prepararDetallesSimulados() {

                List<DetalleVenta> detallesGuardados = new ArrayList<>();

                when(detalleVentaRepository.save(any(DetalleVenta.class)))
                                .thenAnswer(invocacion -> {
                                        DetalleVenta detalle = invocacion.getArgument(0);
                                        detalle.setIdDetalle(detallesGuardados.size() + 1);
                                        detallesGuardados.add(detalle);
                                        return detalle;
                                });

                return detallesGuardados;
        }

        private void assertImporte(String esperado, BigDecimal obtenido) {
                assertNotNull(obtenido);
                assertEquals(
                                0,
                                new BigDecimal(esperado).compareTo(obtenido));
        }
}
