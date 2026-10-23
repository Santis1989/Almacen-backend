
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.DetalleVentaRequest;
import ies.belgrano.almacen.dto.VentaRequest;
import ies.belgrano.almacen.dto.VentaResponse;

import ies.belgrano.almacen.exception.ConflictException;

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
    // PRUEBA 1: ROLLBACK POR STOCK INSUFICIENTE
    // =====================================================

    @Test
    void registrarVenta_stockInsuficienteRevierteTransaccion() {

        long ventasAntes = ventaRepository.count();
        long detallesAntes = detalleVentaRepository.count();

        Producto arroz = new Producto();
        arroz.setNombre("Arroz 1kg");
        arroz.setPrecioVenta(new BigDecimal("1800.00"));
        arroz.setStock(10.0);
        arroz = productoRepository.save(arroz);

        Producto azucar = new Producto();
        azucar.setNombre("Azucar 1kg");
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
        request.setDetalles(
                List.of(detalleArroz, detalleAzucar));

        assertThrows(
                ConflictException.class,
                () -> ventaService.registrarVenta(request));

        Producto arrozDespues = productoRepository
                .findById(arroz.getIdProducto())
                .orElseThrow();

        Producto azucarDespues = productoRepository
                .findById(azucar.getIdProducto())
                .orElseThrow();

        assertEquals(10.0, arrozDespues.getStock());
        assertEquals(5.0, azucarDespues.getStock());

        assertEquals(ventasAntes, ventaRepository.count());
        assertEquals(detallesAntes, detalleVentaRepository.count());
    }

    // =====================================================
    // PRUEBA 2: REGISTRO EXITOSO DE UNA VENTA
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
        request.setDetalles(
                List.of(detalleArroz, detalleAzucar));

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
    // PRUEBA 3: MISMO PRODUCTO REPETIDO SIN STOCK SUFICIENTE
    // =====================================================

    @Test
    void registrarVenta_productoRepetidoSuperaStockGeneraConflicto() {

        // Guardamos la cantidad de registros antes de probar.
        long ventasAntes = ventaRepository.count();
        long detallesAntes = detalleVentaRepository.count();

        // 1. Crear un producto con 10 unidades.
        Producto arroz = new Producto();

        arroz.setNombre("Arroz producto repetido");
        arroz.setPrecioVenta(new BigDecimal("1800.00"));
        arroz.setStock(10.0);

        arroz = productoRepository.save(arroz);

        // 2. Primer detalle: 7 unidades.
        DetalleVentaRequest primerDetalle = new DetalleVentaRequest();

        primerDetalle.setIdProducto(arroz.getIdProducto());
        primerDetalle.setCantidad(new BigDecimal("7"));

        // 3. Segundo detalle: 6 unidades del MISMO producto.
        DetalleVentaRequest segundoDetalle = new DetalleVentaRequest();

        segundoDetalle.setIdProducto(arroz.getIdProducto());
        segundoDetalle.setCantidad(new BigDecimal("6"));

        // En total se solicitan 13 unidades.
        // El stock disponible es solamente 10.

        // 4. Crear la solicitud de venta.
        VentaRequest request = new VentaRequest();

        request.setIdCliente(null);
        request.setPorcentajeDescuento(BigDecimal.ZERO);
        request.setDetalles(
                List.of(primerDetalle, segundoDetalle));

        // 5. La venta debe rechazarse por stock insuficiente.
        assertThrows(
                ConflictException.class,
                () -> ventaService.registrarVenta(request));

        // 6. Verificar que el stock permanezca en 10.
        Producto arrozDespues = productoRepository
                .findById(arroz.getIdProducto())
                .orElseThrow();

        assertEquals(10.0, arrozDespues.getStock());

        // 7. Verificar que no se haya registrado la venta.
        assertEquals(ventasAntes, ventaRepository.count());

        // 8. Verificar que no se hayan guardado detalles.
        assertEquals(detallesAntes, detalleVentaRepository.count());
    }
}
