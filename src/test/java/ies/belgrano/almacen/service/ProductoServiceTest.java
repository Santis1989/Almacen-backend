
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.ProductoRequestDTO;
import ies.belgrano.almacen.dto.ProductoResponseDTO;
import ies.belgrano.almacen.model.Producto;
import ies.belgrano.almacen.repository.ProductoRepository;
import ies.belgrano.almacen.repository.DetalleVentaRepository;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
class ProductoServiceTest {

    @Test
    void listarTodos_devuelveProductosDelRepositorio() {

        ProductoRepository productoRepository =
                mock(ProductoRepository.class);

        DetalleVentaRepository detalleVentaRepository =
                mock(DetalleVentaRepository.class);

        when(productoRepository.findAll())
                .thenReturn(List.of());

        ProductoService productoService =
                new ProductoService(
                        productoRepository,
                        detalleVentaRepository
                );

        var resultado = productoService.listarTodos();

        assertEquals(0, resultado.size());
    }

    @Test
    void crear_guardaProductoYDevuelveDTO() {

        ProductoRepository productoRepository =
                mock(ProductoRepository.class);

        DetalleVentaRepository detalleVentaRepository =
                mock(DetalleVentaRepository.class);

        ProductoRequestDTO datos = new ProductoRequestDTO();

        datos.setNombre("Azúcar");
        datos.setDescripcion("Paquete de 1 kg");
        datos.setPrecioCompra(new BigDecimal("1000.00"));
        datos.setPrecioVenta(new BigDecimal("1500.00"));
        datos.setStock(10.0);
        datos.setStockMinimo(2);

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocacion -> {

                    Producto producto = invocacion.getArgument(0);
                    producto.setIdProducto(15);

                    return producto;
                });

        ProductoService productoService =
                new ProductoService(
                        productoRepository,
                        detalleVentaRepository
                );

        ProductoResponseDTO resultado =
                productoService.crear(datos);

        assertEquals(15, resultado.getIdProducto());
        assertEquals("Azúcar", resultado.getNombre());
        assertEquals(
                new BigDecimal("1500.00"),
                resultado.getPrecioVenta()
        );
        assertEquals(10.0, resultado.getStock());
    }

    @Test
    void actualizar_productoExistenteDevuelveDatosActualizados() {

        // 1. Simulamos los repositorios
        ProductoRepository productoRepository =
                mock(ProductoRepository.class);

        DetalleVentaRepository detalleVentaRepository =
                mock(DetalleVentaRepository.class);

        // 2. Creamos un producto existente
        Producto productoExistente = new Producto();
        productoExistente.setIdProducto(15);
        productoExistente.setNombre("Azúcar");
        productoExistente.setPrecioCompra(new BigDecimal("1000.00"));
        productoExistente.setPrecioVenta(new BigDecimal("1500.00"));
        productoExistente.setStock(10.0);
        productoExistente.setStockMinimo(2);

        // 3. Preparamos los nuevos datos
        ProductoRequestDTO datosNuevos = new ProductoRequestDTO();

        datosNuevos.setNombre("Azúcar refinada");
        datosNuevos.setDescripcion("Paquete de 1 kg");
        datosNuevos.setPrecioCompra(new BigDecimal("1200.00"));
        datosNuevos.setPrecioVenta(new BigDecimal("1800.00"));
        datosNuevos.setStock(20.0);
        datosNuevos.setStockMinimo(5);

        // 4. Simulamos la búsqueda del producto
        when(productoRepository.findById(15))
                .thenReturn(java.util.Optional.of(productoExistente));

        // 5. Simulamos el guardado
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        // 6. Creamos el servicio
        ProductoService productoService =
                new ProductoService(
                        productoRepository,
                        detalleVentaRepository
                );

        // 7. Ejecutamos la actualización
        var resultado = productoService.actualizar(15, datosNuevos);

        // 8. Verificamos los datos actualizados
        assertEquals(true, resultado.isPresent());

        ProductoResponseDTO productoActualizado = resultado.get();

        assertEquals(15, productoActualizado.getIdProducto());
        assertEquals("Azúcar refinada", productoActualizado.getNombre());
        assertEquals(new BigDecimal("1800.00"),
                productoActualizado.getPrecioVenta());
        assertEquals(20.0, productoActualizado.getStock());
        assertEquals(5, productoActualizado.getStockMinimo());
    }

    @Test
    void actualizar_productoInexistenteDevuelveVacio() {

        // Simulamos los repositorios
        ProductoRepository productoRepository =
                mock(ProductoRepository.class);

        DetalleVentaRepository detalleVentaRepository =
                mock(DetalleVentaRepository.class);

        // Preparamos los datos para actualizar
        ProductoRequestDTO datos = new ProductoRequestDTO();

        datos.setNombre("Azúcar");
        datos.setPrecioCompra(new BigDecimal("1000.00"));
        datos.setPrecioVenta(new BigDecimal("1500.00"));
        datos.setStock(10.0);
        datos.setStockMinimo(2);

        // Simulamos que el producto 999 no existe
        when(productoRepository.findById(999))
                .thenReturn(java.util.Optional.empty());

        // Creamos el servicio
        ProductoService productoService =
                new ProductoService(
                        productoRepository,
                        detalleVentaRepository
                );

        // Intentamos actualizar
        var resultado = productoService.actualizar(999, datos);

        // Comprobamos que no devuelve ningún producto
        assertEquals(true, resultado.isEmpty());
    }

    @Test
    void eliminar_productoLlamaAlRepositorio() {

        // 1. Simulamos los repositorios
        ProductoRepository productoRepository =
                mock(ProductoRepository.class);

        DetalleVentaRepository detalleVentaRepository =
                mock(DetalleVentaRepository.class);

        // 2. Creamos el servicio
        ProductoService productoService =
                new ProductoService(
                        productoRepository,
                        detalleVentaRepository
                );

        // 3. Ejecutamos la eliminación
        productoService.eliminar(15);

        // 4. Verificamos que se solicitó eliminar el ID 15
        org.mockito.Mockito.verify(productoRepository)
                .deleteById(15);
    }

    @Test
    void validar_productoSinNombreGeneraError() {

        // Creamos un producto con nombre vacío
        ProductoRequestDTO datos = new ProductoRequestDTO();

        datos.setNombre("");
        datos.setDescripcion("Paquete de 1 kg");
        datos.setPrecioCompra(new BigDecimal("1000.00"));
        datos.setPrecioVenta(new BigDecimal("1500.00"));
        datos.setStock(10.0);
        datos.setStockMinimo(2);

        // Ejecutamos las validaciones del DTO
        try (var factory = Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            var errores = validator.validate(datos);

            // Debe existir un error de validación
            assertEquals(1, errores.size());
        }
    }

    @Test
    void validar_precioVentaNegativoGeneraError() {

        ProductoRequestDTO datos = new ProductoRequestDTO();

        datos.setNombre("Azúcar");
        datos.setDescripcion("Paquete de 1 kg");
        datos.setPrecioCompra(new BigDecimal("1000.00"));

        // Precio incorrecto
        datos.setPrecioVenta(new BigDecimal("-1500.00"));

        datos.setStock(10.0);
        datos.setStockMinimo(2);

        // Validamos los datos sin conectarnos a MySQL
        try (var factory = Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            var errores = validator.validate(datos);

            // Debe detectar un error de validación
            assertEquals(1, errores.size());
        }
    }

    @Test
    void validar_stockMinimoNegativoGeneraError() {

        ProductoRequestDTO datos = new ProductoRequestDTO();

        datos.setNombre("Azúcar");
        datos.setDescripcion("Paquete de 1 kg");
        datos.setPrecioCompra(new BigDecimal("1000.00"));
        datos.setPrecioVenta(new BigDecimal("1500.00"));
        datos.setStock(10.0);

        // Stock mínimo incorrecto
        datos.setStockMinimo(-2);

        try (var factory = Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            var errores = validator.validate(datos);

            assertEquals(1, errores.size());
        }
    }


}
