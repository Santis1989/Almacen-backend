
package ies.belgrano.almacen.controller;

import ies.belgrano.almacen.service.ProductoService;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ies.belgrano.almacen.dto.ProductoRequestDTO;
import ies.belgrano.almacen.dto.ProductoResponseDTO;

import java.math.BigDecimal;
import ies.belgrano.almacen.model.Producto;
class ProductoControllerTest {

    @Test
    void buscarPorId_productoInexistenteDevuelve404() {

        // Creamos un servicio simulado con Mockito
        ProductoService productoService =
                mock(ProductoService.class);

        // Simulamos que el producto 999 no existe
        when(productoService.buscarPorId(999))
                .thenReturn(Optional.empty());

        // Creamos el controlador
        ProductoController controller =
                new ProductoController(productoService);

        // Buscamos el producto
        var respuesta = controller.buscarPorId(999);

        // Verificamos el código 404
        assertEquals(
                HttpStatus.NOT_FOUND,
                respuesta.getStatusCode()
        );
    }

    @Test
    void crear_productoValidoDevuelve201() {

        // Simulamos el servicio
        ProductoService productoService =
                mock(ProductoService.class);

        ProductoController controller =
                new ProductoController(productoService);

        // Preparamos los datos del producto
        ProductoRequestDTO datos = new ProductoRequestDTO();

        datos.setNombre("Azúcar");
        datos.setDescripcion("Paquete de 1 kg");
        datos.setPrecioCompra(new BigDecimal("1000.00"));
        datos.setPrecioVenta(new BigDecimal("1500.00"));
        datos.setStock(10.0);
        datos.setStockMinimo(2);

        // Simulamos el producto creado
        ProductoResponseDTO creado = new ProductoResponseDTO();

        creado.setIdProducto(15);
        creado.setNombre("Azúcar");

        when(productoService.crear(datos))
                .thenReturn(creado);

        // Ejecutamos el método POST del controlador
        var respuesta = controller.crear(datos);

        // Verificamos el código HTTP 201
        assertEquals(
                HttpStatus.CREATED,
                respuesta.getStatusCode()
        );

        // Verificamos que devuelve el ID generado
        assertEquals(
                15,
                respuesta.getBody().getIdProducto()
        );
    }

    @Test
    void crear_stockInicialNegativoDevuelve400() {

        ProductoService productoService =
                mock(ProductoService.class);

        ProductoController controller =
                new ProductoController(productoService);

        ProductoRequestDTO datos = new ProductoRequestDTO();

        datos.setNombre("Azúcar");
        datos.setDescripcion("Paquete de 1 kg");
        datos.setPrecioCompra(new BigDecimal("1000.00"));
        datos.setPrecioVenta(new BigDecimal("1500.00"));

        // Stock inicial inválido
        datos.setStock(-5.0);

        datos.setStockMinimo(2);

        // Ejecutamos el método del controlador
        var respuesta = controller.crear(datos);

        // Debe responder 400 Bad Request
        assertEquals(
                HttpStatus.BAD_REQUEST,
                respuesta.getStatusCode()
        );

        // No debe intentar guardar el producto
        org.mockito.Mockito.verify(
                productoService,
                org.mockito.Mockito.never()
        ).crear(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void actualizar_productoInexistenteDevuelve404() {

        ProductoService productoService =
                mock(ProductoService.class);

        ProductoController controller =
                new ProductoController(productoService);

        ProductoRequestDTO datos = new ProductoRequestDTO();

        datos.setNombre("Yerba");
        datos.setDescripcion("Paquete de 1 kg");
        datos.setPrecioCompra(new BigDecimal("2000.00"));
        datos.setPrecioVenta(new BigDecimal("3000.00"));
        datos.setStock(15.0);
        datos.setStockMinimo(3);

        // Simulamos que el producto no existe
        when(productoService.actualizar(999, datos))
                .thenReturn(Optional.empty());

        // Intentamos actualizarlo
        var respuesta = controller.actualizar(999, datos);

        // Debe responder 404 Not Found
        assertEquals(
                HttpStatus.NOT_FOUND,
                respuesta.getStatusCode()
        );
    }

    @Test
    void eliminar_productoConVentasDevuelve409() {

        ProductoService productoService =
                mock(ProductoService.class);

        ProductoController controller =
                new ProductoController(productoService);

        // Simulamos un producto existente
        Producto producto = new Producto();
        producto.setIdProducto(15);

        when(productoService.buscarPorId(15))
                .thenReturn(Optional.of(producto));

        // Simulamos que tiene ventas asociadas
        when(productoService.tieneVentasAsociadas(15))
                .thenReturn(true);

        // Intentamos eliminarlo
        var respuesta = controller.eliminar(15);

        // Debe responder 409 Conflict
        assertEquals(
                HttpStatus.CONFLICT,
                respuesta.getStatusCode()
        );

        // Verificamos que NO se elimine
        org.mockito.Mockito.verify(
                productoService,
                org.mockito.Mockito.never()
        ).eliminar(15);
    }

    @Test
    void eliminar_productoSinVentasDevuelve204() {

        ProductoService productoService =
                mock(ProductoService.class);

        ProductoController controller =
                new ProductoController(productoService);

        // Simulamos un producto existente
        Producto producto = new Producto();
        producto.setIdProducto(15);

        when(productoService.buscarPorId(15))
                .thenReturn(Optional.of(producto));

        // Simulamos que NO tiene ventas asociadas
        when(productoService.tieneVentasAsociadas(15))
                .thenReturn(false);

        // Intentamos eliminarlo
        var respuesta = controller.eliminar(15);

        // Debe responder 204 No Content
        assertEquals(
                HttpStatus.NO_CONTENT,
                respuesta.getStatusCode()
        );

        // Comprobamos que llamó al método eliminar
        org.mockito.Mockito.verify(
                productoService
        ).eliminar(15);
    }

    @Test
    void eliminar_productoInexistenteDevuelve404() {

        ProductoService productoService =
                mock(ProductoService.class);

        ProductoController controller =
                new ProductoController(productoService);

        // Simulamos que el producto 999 no existe
        when(productoService.buscarPorId(999))
                .thenReturn(Optional.empty());

        // Intentamos eliminarlo
        var respuesta = controller.eliminar(999);

        // Debe responder 404 Not Found
        assertEquals(
                HttpStatus.NOT_FOUND,
                respuesta.getStatusCode()
        );

        // Verificamos que NO se intente eliminar
        org.mockito.Mockito.verify(
                productoService,
                org.mockito.Mockito.never()
        ).eliminar(999);
    }

    @Test
    void actualizar_productoExistenteDevuelve200() {

        ProductoService productoService =
                mock(ProductoService.class);

        ProductoController controller =
                new ProductoController(productoService);

        // Datos que enviamos para actualizar
        ProductoRequestDTO datos = new ProductoRequestDTO();

        datos.setNombre("Yerba");
        datos.setDescripcion("Paquete de 1 kg");
        datos.setPrecioCompra(new BigDecimal("2000.00"));
        datos.setPrecioVenta(new BigDecimal("3000.00"));
        datos.setStock(15.0);
        datos.setStockMinimo(3);

        // Simulamos el producto actualizado
        ProductoResponseDTO actualizado =
                new ProductoResponseDTO();

        actualizado.setIdProducto(15);
        actualizado.setNombre("Yerba");

        when(productoService.actualizar(15, datos))
                .thenReturn(Optional.of(actualizado));

        // Ejecutamos la actualización
        var respuesta = controller.actualizar(15, datos);

        // Debe devolver 200 OK
        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        // Verificamos los datos devueltos
        assertEquals(
                15,
                respuesta.getBody().getIdProducto()
        );

        assertEquals(
                "Yerba",
                respuesta.getBody().getNombre()
        );
    }


}
