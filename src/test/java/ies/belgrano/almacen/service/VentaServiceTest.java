
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.VentaRequest;
import ies.belgrano.almacen.dto.DetalleVentaRequest;
import ies.belgrano.almacen.dto.VentaResponse;

import ies.belgrano.almacen.exception.ConflictException;
import ies.belgrano.almacen.exception.ResourceNotFoundException;

import ies.belgrano.almacen.model.Producto;
import ies.belgrano.almacen.model.Venta;
import ies.belgrano.almacen.model.DetalleVenta;

import ies.belgrano.almacen.repository.ClienteRepository;
import ies.belgrano.almacen.repository.DetalleVentaRepository;
import ies.belgrano.almacen.repository.ProductoRepository;
import ies.belgrano.almacen.repository.VentaRepository;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

class VentaServiceTest {

    // PRUEBA 1: Venta sin productos.

    @Test
    void validar_ventaSinDetallesGeneraError() {

        VentaRequest request = new VentaRequest();

        request.setIdCliente(null);
        request.setPorcentajeDescuento(BigDecimal.ZERO);
        request.setDetalles(List.of());

        try (var factory = Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            var errores = validator.validate(request);

            assertEquals(1, errores.size());
        }
    }

    // PRUEBA 2: Cantidad igual a cero.

    @Test
    void validar_cantidadCeroGeneraError() {

        DetalleVentaRequest detalle = new DetalleVentaRequest();

        detalle.setIdProducto(1);
        detalle.setCantidad(BigDecimal.ZERO);

        VentaRequest request = new VentaRequest();

        request.setIdCliente(null);
        request.setPorcentajeDescuento(BigDecimal.ZERO);
        request.setDetalles(List.of(detalle));

        try (var factory = Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            var errores = validator.validate(request);

            assertEquals(1, errores.size());
        }
    }

    // PRUEBA 3: Stock insuficiente.

    @Test
    void registrarVenta_stockInsuficienteGeneraConflicto() {

        VentaRepository ventaRepository = mock(VentaRepository.class);

        DetalleVentaRepository detalleVentaRepository = mock(DetalleVentaRepository.class);

        ProductoRepository productoRepository = mock(ProductoRepository.class);

        ClienteRepository clienteRepository = mock(ClienteRepository.class);

        VentaService ventaService = new VentaService(
                ventaRepository,
                detalleVentaRepository,
                productoRepository,
                clienteRepository);

        Producto producto = new Producto();

        producto.setIdProducto(1);
        producto.setNombre("Arroz 1kg");
        producto.setPrecioVenta(new BigDecimal("1800.00"));
        producto.setStock(5.0);

        when(productoRepository.findById(1))
                .thenReturn(Optional.of(producto));

        DetalleVentaRequest detalle = new DetalleVentaRequest();

        detalle.setIdProducto(1);
        detalle.setCantidad(new BigDecimal("10"));

        VentaRequest request = new VentaRequest();

        request.setIdCliente(null);
        request.setPorcentajeDescuento(BigDecimal.ZERO);
        request.setDetalles(List.of(detalle));

        assertThrows(
                ConflictException.class,
                () -> ventaService.registrarVenta(request));

        verify(ventaRepository, never()).save(any());
        verify(productoRepository, never()).save(any());

        assertEquals(5.0, producto.getStock());
    }

    // PRUEBA 4: Calcular total y descontar stock.

    @Test
    void registrarVenta_calculaTotalYDescuentaStock() {

        VentaRepository ventaRepository = mock(VentaRepository.class);

        DetalleVentaRepository detalleVentaRepository = mock(DetalleVentaRepository.class);

        ProductoRepository productoRepository = mock(ProductoRepository.class);

        ClienteRepository clienteRepository = mock(ClienteRepository.class);

        VentaService ventaService = new VentaService(
                ventaRepository,
                detalleVentaRepository,
                productoRepository,
                clienteRepository);

        Producto producto = new Producto();

        producto.setIdProducto(1);
        producto.setNombre("Arroz 1kg");
        producto.setPrecioVenta(new BigDecimal("1800.00"));
        producto.setStock(5.0);

        when(productoRepository.findById(1))
                .thenReturn(Optional.of(producto));

        when(ventaRepository.save(any(Venta.class)))
                .thenAnswer(invocacion -> {
                    Venta venta = invocacion.getArgument(0);
                    venta.setIdVenta(1);
                    return venta;
                });

        when(detalleVentaRepository.save(any(DetalleVenta.class)))
                .thenAnswer(invocacion -> {
                    DetalleVenta detalleGuardado = invocacion.getArgument(0);
                    detalleGuardado.setIdDetalle(1);
                    return detalleGuardado;
                });

        when(detalleVentaRepository.findByVenta_IdVenta(1))
                .thenReturn(List.of());

        DetalleVentaRequest detalle = new DetalleVentaRequest();

        detalle.setIdProducto(1);
        detalle.setCantidad(new BigDecimal("2"));

        VentaRequest request = new VentaRequest();

        request.setIdCliente(null);
        request.setPorcentajeDescuento(new BigDecimal("10"));
        request.setDetalles(List.of(detalle));

        VentaResponse respuesta = ventaService.registrarVenta(request);

        assertEquals(
                0,
                new BigDecimal("3600.00")
                        .compareTo(respuesta.getSubtotal()));

        assertEquals(
                0,
                new BigDecimal("360.00")
                        .compareTo(respuesta.getMontoDescuento()));

        assertEquals(
                0,
                new BigDecimal("3240.00")
                        .compareTo(respuesta.getTotal()));

        assertEquals(3.0, producto.getStock());

        verify(ventaRepository).save(any(Venta.class));
        verify(detalleVentaRepository)
                .save(any(DetalleVenta.class));
        verify(productoRepository).save(producto);
    }

    // PRUEBA 5: Producto inexistente.

    @Test
    void registrarVenta_productoInexistenteGeneraError() {

        VentaRepository ventaRepository = mock(VentaRepository.class);

        DetalleVentaRepository detalleVentaRepository = mock(DetalleVentaRepository.class);

        ProductoRepository productoRepository = mock(ProductoRepository.class);

        ClienteRepository clienteRepository = mock(ClienteRepository.class);

        VentaService ventaService = new VentaService(
                ventaRepository,
                detalleVentaRepository,
                productoRepository,
                clienteRepository);

        when(productoRepository.findById(9999))
                .thenReturn(Optional.empty());

        DetalleVentaRequest detalle = new DetalleVentaRequest();

        detalle.setIdProducto(9999);
        detalle.setCantidad(new BigDecimal("1"));

        VentaRequest request = new VentaRequest();

        request.setIdCliente(null);
        request.setPorcentajeDescuento(BigDecimal.ZERO);
        request.setDetalles(List.of(detalle));

        ResourceNotFoundException error = assertThrows(
                ResourceNotFoundException.class,
                () -> ventaService.registrarVenta(request));

        assertEquals(
                "Producto no encontrado: 9999",
                error.getMessage());

        verify(ventaRepository, never()).save(any());
        verify(detalleVentaRepository, never()).save(any());
        verify(productoRepository, never()).save(any());
    }

    // PRUEBA 6: Cliente inexistente.

    @Test
    void registrarVenta_clienteInexistenteGeneraError() {

        VentaRepository ventaRepository = mock(VentaRepository.class);

        DetalleVentaRepository detalleVentaRepository = mock(DetalleVentaRepository.class);

        ProductoRepository productoRepository = mock(ProductoRepository.class);

        ClienteRepository clienteRepository = mock(ClienteRepository.class);

        VentaService ventaService = new VentaService(
                ventaRepository,
                detalleVentaRepository,
                productoRepository,
                clienteRepository);

        when(clienteRepository.findById(9999))
                .thenReturn(Optional.empty());

        DetalleVentaRequest detalle = new DetalleVentaRequest();

        detalle.setIdProducto(1);
        detalle.setCantidad(BigDecimal.ONE);

        VentaRequest request = new VentaRequest();

        request.setIdCliente(9999);
        request.setPorcentajeDescuento(BigDecimal.ZERO);
        request.setDetalles(List.of(detalle));

        ResourceNotFoundException error = assertThrows(
                ResourceNotFoundException.class,
                () -> ventaService.registrarVenta(request));

        assertEquals(
                "Cliente no encontrado: 9999",
                error.getMessage());

        verify(ventaRepository, never()).save(any());
        verify(detalleVentaRepository, never()).save(any());
        verify(productoRepository, never()).save(any());
    }

    // PRUEBA 7:
    // Registrar una venta con dos productos diferentes.

    @Test
    void registrarVenta_dosProductosCalculaTotalYActualizaStock() {

        // Repositorios simulados.
        VentaRepository ventaRepository = mock(VentaRepository.class);

        DetalleVentaRepository detalleVentaRepository = mock(DetalleVentaRepository.class);

        ProductoRepository productoRepository = mock(ProductoRepository.class);

        ClienteRepository clienteRepository = mock(ClienteRepository.class);

        VentaService ventaService = new VentaService(
                ventaRepository,
                detalleVentaRepository,
                productoRepository,
                clienteRepository);

        // Producto 1: Arroz.
        Producto arroz = new Producto();

        arroz.setIdProducto(1);
        arroz.setNombre("Arroz 1kg");
        arroz.setPrecioVenta(new BigDecimal("1800.00"));
        arroz.setStock(10.0);

        // Producto 2: Azúcar.
        Producto azucar = new Producto();

        azucar.setIdProducto(2);
        azucar.setNombre("Azúcar 1kg");
        azucar.setPrecioVenta(new BigDecimal("1200.00"));
        azucar.setStock(8.0);

        // Simulamos las búsquedas de los productos.
        when(productoRepository.findById(1))
                .thenReturn(Optional.of(arroz));

        when(productoRepository.findById(2))
                .thenReturn(Optional.of(azucar));

        // Simulamos que la venta recibe ID 1.
        when(ventaRepository.save(any(Venta.class)))
                .thenAnswer(invocacion -> {
                    Venta venta = invocacion.getArgument(0);
                    venta.setIdVenta(1);
                    return venta;
                });

        // Guardamos los detalles simulados en una lista.
        List<DetalleVenta> detallesGuardados = new ArrayList<>();

        when(detalleVentaRepository.save(any(DetalleVenta.class)))
                .thenAnswer(invocacion -> {
                    DetalleVenta detalleGuardado = invocacion.getArgument(0);

                    detalleGuardado.setIdDetalle(
                            detallesGuardados.size() + 1);

                    detallesGuardados.add(detalleGuardado);

                    return detalleGuardado;
                });

        // Simulamos la consulta de los detalles guardados.
        when(detalleVentaRepository.findByVenta_IdVenta(1))
                .thenAnswer(invocacion -> new ArrayList<>(detallesGuardados));

        // Detalle 1: dos unidades de arroz.
        DetalleVentaRequest detalleArroz = new DetalleVentaRequest();

        detalleArroz.setIdProducto(1);
        detalleArroz.setCantidad(new BigDecimal("2"));

        // Detalle 2: tres unidades de azúcar.
        DetalleVentaRequest detalleAzucar = new DetalleVentaRequest();

        detalleAzucar.setIdProducto(2);
        detalleAzucar.setCantidad(new BigDecimal("3"));

        // Creamos la venta.
        VentaRequest request = new VentaRequest();

        request.setIdCliente(null);
        request.setPorcentajeDescuento(BigDecimal.ZERO);
        request.setDetalles(
                List.of(detalleArroz, detalleAzucar));

        // Registramos la venta.
        VentaResponse respuesta = ventaService.registrarVenta(request);

        // Subtotal esperado: 3600 + 3600 = 7200.
        assertEquals(
                0,
                new BigDecimal("7200.00")
                        .compareTo(respuesta.getSubtotal()));

        // Sin descuento, el total también debe ser 7200.
        assertEquals(
                0,
                new BigDecimal("7200.00")
                        .compareTo(respuesta.getTotal()));

        // Verificamos el stock final.
        assertEquals(8.0, arroz.getStock());
        assertEquals(5.0, azucar.getStock());

        // Comprobamos que se guardaron dos detalles.
        assertEquals(2, detallesGuardados.size());

        // Comprobamos que la respuesta incluye dos detalles.
        assertNotNull(respuesta.getDetalles());
        assertEquals(2, respuesta.getDetalles().size());

        // Verificamos las llamadas a los repositorios.
        verify(ventaRepository).save(any(Venta.class));

        verify(detalleVentaRepository, times(2))
                .save(any(DetalleVenta.class));

        verify(productoRepository).save(arroz);
        verify(productoRepository).save(azucar);
    }
}
