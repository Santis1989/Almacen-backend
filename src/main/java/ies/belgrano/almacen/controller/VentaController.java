package ies.belgrano.almacen.controller;

import ies.belgrano.almacen.dto.VentaRequest;
import ies.belgrano.almacen.model.DetalleVenta;
import ies.belgrano.almacen.model.Venta;
import ies.belgrano.almacen.service.VentaService;
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


    // ==================================================
    // LISTAR TODAS LAS VENTAS
    // ==================================================

    @GetMapping
    public List<Venta> listarTodas() {
        return ventaService.listarTodas();
    }


    // ==================================================
    // BUSCAR VENTA POR ID
    // ==================================================

    @GetMapping("/{id}")
    public ResponseEntity<Venta> buscarPorId(
            @PathVariable Integer id
    ) {

        return ventaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // ==================================================
    // LISTAR DETALLES DE UNA VENTA
    // ==================================================

    @GetMapping("/{id}/detalles")
    public List<DetalleVenta> listarDetalles(
            @PathVariable Integer id
    ) {

        return ventaService.listarDetalles(id);
    }


    // ==================================================
    // REGISTRAR UNA VENTA
    // ==================================================

    @PostMapping
    public ResponseEntity<Venta> registrarVenta(
            @RequestBody VentaRequest request
    ) {

        Venta ventaGuardada =
                ventaService.registrarVenta(
                        request.getVenta(),
                        request.getDetalles()
                );

        return ResponseEntity.ok(ventaGuardada);
    }
}