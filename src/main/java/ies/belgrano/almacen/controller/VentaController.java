
package ies.belgrano.almacen.controller;

import ies.belgrano.almacen.dto.DetalleVentaResponse;
import ies.belgrano.almacen.dto.VentaRequest;
import ies.belgrano.almacen.dto.VentaResponse;
import ies.belgrano.almacen.service.VentaService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // Obtener todas las ventas
    @GetMapping
    public ResponseEntity<List<VentaResponse>> listarVentas() {

        List<VentaResponse> ventas = ventaService.listarTodas();

        return ResponseEntity.ok(ventas);
    }

    // Obtener una venta por su ID
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerVenta(
            @PathVariable Integer id) {

        return ventaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Obtener los detalles de una venta
    @GetMapping("/{id}/detalles")
    public ResponseEntity<List<DetalleVentaResponse>> obtenerDetalles(
            @PathVariable Integer id) {

        List<DetalleVentaResponse> detalles = ventaService.listarDetalles(id);

        return ResponseEntity.ok(detalles);
    }

    // Registrar una nueva venta
    @PostMapping
    public ResponseEntity<VentaResponse> registrarVenta(
            @Valid @RequestBody VentaRequest request) {

        VentaResponse ventaGuardada = ventaService.registrarVenta(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ventaGuardada);
    }
}
