
package ies.belgrano.almacen.controller;

import ies.belgrano.almacen.dto.ProductoRequestDTO;
import ies.belgrano.almacen.dto.ProductoResponseDTO;
import ies.belgrano.almacen.model.Producto;
import ies.belgrano.almacen.service.ProductoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET: listar productos
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarTodos() {

        List<ProductoResponseDTO> productos =
                productoService.listarTodos()
                        .stream()
                        .map(productoService::convertirAResponseDTO)
                        .toList();

        return ResponseEntity.ok(productos);
    }

    // GET: buscar producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> buscarPorId(
            @PathVariable Integer id) {

        return productoService.buscarPorId(id)
                .map(productoService::convertirAResponseDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(
            @Valid @RequestBody ProductoRequestDTO datos) {

        // El stock inicial no puede ser negativo
        if (datos.getStock() < 0) {
            return ResponseEntity.badRequest().build();
        }

        ProductoResponseDTO creado = productoService.crear(datos);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }


    // PUT: actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoRequestDTO datos) {

        return productoService.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {

        // Comprobar si el producto existe
        if (productoService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Impedir eliminar productos asociados a ventas
        if (productoService.tieneVentasAsociadas(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Eliminar el producto
        productoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

}
