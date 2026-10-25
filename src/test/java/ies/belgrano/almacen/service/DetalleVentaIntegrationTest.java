package ies.belgrano.almacen.service;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ies.belgrano.almacen.dto.DetalleVentaRequest;
import ies.belgrano.almacen.dto.DetalleVentaResponse;
import ies.belgrano.almacen.model.Producto;
import ies.belgrano.almacen.model.Venta;
import ies.belgrano.almacen.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import ies.belgrano.almacen.exception.ResourceNotFoundException;
import ies.belgrano.almacen.repository.DetalleVentaRepository;
import ies.belgrano.almacen.repository.VentaRepository;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:detalle_venta_test;DB_CLOSE_DELAY=-1"
})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DetalleVentaIntegrationTest {
	@Autowired
	private VentaRepository ventaRepository;

	@Autowired
	private DetalleVentaRepository detalleVentaRepository;
    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void comprobarBaseDePruebas() throws SQLException {
        try (Connection conexion = dataSource.getConnection()) {
            assertTrue(
                    conexion.getMetaData().getURL()
                            .startsWith("jdbc:h2:mem:detalle_venta_test"),
                    "La prueba debe usar exclusivamente la base H2 de pruebas."
            );
        }
    }

    @Test
    void registraDetalleCalculaSubtotalYDescuentaStock() {

        Producto producto = new Producto();
        producto.setNombre("Producto de prueba");
        producto.setDescripcion("Prueba automatizada de detalle");
        producto.setPrecioCompra(new BigDecimal("5.00"));
        producto.setPrecioVenta(new BigDecimal("10.50"));
        producto.setStock(10.0);
        producto.setStockMinimo(1);

        producto = productoRepository.save(producto);

        Venta venta = new Venta();
        venta.setSubtotal(new BigDecimal("2.63"));
        venta.setPorcentajeDescuento(new BigDecimal("0.00"));
        venta.setMontoDescuento(new BigDecimal("0.00"));
        venta.setTotal(new BigDecimal("2.63"));

        DetalleVentaRequest entrada = new DetalleVentaRequest();
        entrada.setIdProducto(producto.getIdProducto());
        entrada.setCantidad(new BigDecimal("0.250"));

        Venta guardada = ventaService.registrarVenta(
                venta,
                List.of(entrada)
        );

        assertNotNull(guardada.getIdVenta());

        List<DetalleVentaResponse> detalles =
                ventaService.listarDetalles(guardada.getIdVenta());

        assertEquals(1, detalles.size());

        DetalleVentaResponse detalle = detalles.get(0);

        assertNotNull(detalle.getIdDetalle());
        assertEquals(producto.getIdProducto(), detalle.getIdProducto());
        assertEquals("Producto de prueba", detalle.getNombreProducto());

        assertEquals(
                0,
                new BigDecimal("0.250").compareTo(detalle.getCantidad())
        );

        assertEquals(
                0,
                new BigDecimal("10.50").compareTo(detalle.getPrecio())
        );

        assertEquals(
                0,
                new BigDecimal("2.63").compareTo(detalle.getSubtotal())
        );

        Producto actualizado = productoRepository
                .findById(producto.getIdProducto())
                .orElseThrow();

        assertEquals(9.75, actualizado.getStock().doubleValue(), 0.000001);
    }
    @Test
    void productoInexistenteRevierteVentaDetallesYStock() {

        Producto producto = new Producto();
        producto.setNombre("Producto para rollback");
        producto.setPrecioCompra(new BigDecimal("5.00"));
        producto.setPrecioVenta(new BigDecimal("10.50"));
        producto.setStock(10.0);
        producto.setStockMinimo(1);

        producto = productoRepository.save(producto);
        Integer idProducto = producto.getIdProducto();

        Integer idInexistente = Integer.MAX_VALUE;
        assertFalse(productoRepository.existsById(idInexistente));

        long ventasAntes = ventaRepository.count();
        long detallesAntes = detalleVentaRepository.count();

        Venta venta = new Venta();
        venta.setSubtotal(new BigDecimal("10.50"));
        venta.setPorcentajeDescuento(new BigDecimal("0.00"));
        venta.setMontoDescuento(new BigDecimal("0.00"));
        venta.setTotal(new BigDecimal("10.50"));

        DetalleVentaRequest primero = new DetalleVentaRequest();
        primero.setIdProducto(idProducto);
        primero.setCantidad(BigDecimal.ONE);

        DetalleVentaRequest segundo = new DetalleVentaRequest();
        segundo.setIdProducto(idInexistente);
        segundo.setCantidad(BigDecimal.ONE);

        assertThrows(
                ResourceNotFoundException.class,
                () -> ventaService.registrarVenta(
                        venta,
                        List.of(primero, segundo)
                )
        );

        assertEquals(ventasAntes, ventaRepository.count());
        assertEquals(detallesAntes, detalleVentaRepository.count());

        Producto actualizado = productoRepository
                .findById(idProducto)
                .orElseThrow();

        assertEquals(10.0, actualizado.getStock().doubleValue(), 0.000001);
    }
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "0",
            "-2",
            "1.1234",
            "1000000000"
    })
    void cantidadInvalidaNoGuardaNiModificaStock(String valor) {

        Producto producto = new Producto();
        producto.setNombre("Producto para cantidad invalida");
        producto.setPrecioCompra(new BigDecimal("5.00"));
        producto.setPrecioVenta(new BigDecimal("10.50"));
        producto.setStock(10.0);
        producto.setStockMinimo(1);

        producto = productoRepository.save(producto);
        Integer idProducto = producto.getIdProducto();

        long ventasAntes = ventaRepository.count();
        long detallesAntes = detalleVentaRepository.count();

        Venta venta = new Venta();
        venta.setSubtotal(new BigDecimal("10.50"));
        venta.setPorcentajeDescuento(new BigDecimal("0.00"));
        venta.setMontoDescuento(new BigDecimal("0.00"));
        venta.setTotal(new BigDecimal("10.50"));

        DetalleVentaRequest entrada = new DetalleVentaRequest();
        entrada.setIdProducto(idProducto);
        entrada.setCantidad(
                valor == null ? null : new BigDecimal(valor)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrarVenta(
                        venta,
                        List.of(entrada)
                )
        );

        assertEquals(ventasAntes, ventaRepository.count());
        assertEquals(detallesAntes, detalleVentaRepository.count());

        Producto actualizado = productoRepository
                .findById(idProducto)
                .orElseThrow();

        assertEquals(10.0, actualizado.getStock().doubleValue(), 0.000001);
    }
}
