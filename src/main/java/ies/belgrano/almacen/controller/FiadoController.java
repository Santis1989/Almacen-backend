package ies.belgrano.almacen.controller;

import ies.belgrano.almacen.model.Fiado;
import ies.belgrano.almacen.service.FiadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fiados")
public class FiadoController {

    private final FiadoService fiadoService;

    public FiadoController(FiadoService fiadoService) {
        this.fiadoService = fiadoService;
    }

    // GET - Listar todos los fiados
    @GetMapping
    public List<Fiado> listarTodos() {
        return fiadoService.listarTodos();
    }

    // GET - Buscar fiado por ID
    @GetMapping("/{id}")
    public ResponseEntity<Fiado> buscarPorId(@PathVariable Integer id) {
        return fiadoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST - Crear un nuevo fiado
    @PostMapping
    public Fiado crear(@RequestBody Fiado fiado) {
        return fiadoService.guardar(fiado);
    }

    // PUT - Modificar un fiado
    @PutMapping("/{id}")
    public ResponseEntity<Fiado> actualizar(
            @PathVariable Integer id,
            @RequestBody Fiado fiado) {

        try {
            Fiado actualizado = fiadoService.actualizar(id, fiado);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE - Eliminar un fiado
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {

        if (fiadoService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        fiadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}