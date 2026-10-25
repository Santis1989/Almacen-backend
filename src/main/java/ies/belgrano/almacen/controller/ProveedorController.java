package ies.belgrano.almacen.controller;

import ies.belgrano.almacen.dto.ProveedorRequestDTO;
import ies.belgrano.almacen.dto.ProveedorResponseDTO;
import ies.belgrano.almacen.service.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public ResponseEntity<List<ProveedorResponseDTO>> listar(
            @RequestParam(required = false) Boolean soloActivos,
            @RequestParam(required = false) String busqueda) {
        List<ProveedorResponseDTO> lista = proveedorService.listar(soloActivos, busqueda);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> buscarPorId(@PathVariable Integer id) {
        ProveedorResponseDTO proveedor = proveedorService.buscarPorId(id);
        return ResponseEntity.ok(proveedor);
    }

    @GetMapping("/cuit/{cuit}")
    public ResponseEntity<ProveedorResponseDTO> buscarPorCuit(@PathVariable String cuit) {
        ProveedorResponseDTO proveedor = proveedorService.buscarPorCuit(cuit);
        return ResponseEntity.ok(proveedor);
    }

    @PostMapping
    public ResponseEntity<ProveedorResponseDTO> crear(@Valid @RequestBody ProveedorRequestDTO request) {
        ProveedorResponseDTO nuevo = proveedorService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(nuevo.getIdProveedor())
                .toUri();
        return ResponseEntity.created(location).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProveedorRequestDTO request) {
        ProveedorResponseDTO actualizado = proveedorService.actualizar(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProveedorResponseDTO> desactivar(@PathVariable Integer id) {
        ProveedorResponseDTO desactivado = proveedorService.cambiarEstado(id, false);
        return ResponseEntity.ok(desactivado);
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ProveedorResponseDTO> reactivar(@PathVariable Integer id) {
        ProveedorResponseDTO reactivado = proveedorService.cambiarEstado(id, true);
        return ResponseEntity.ok(reactivado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFisico(@PathVariable Integer id) {
        proveedorService.eliminarFisico(id);
        return ResponseEntity.noContent().build();
    }
}
