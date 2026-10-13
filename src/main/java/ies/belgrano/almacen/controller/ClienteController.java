package ies.belgrano.almacen.controller;

import ies.belgrano.almacen.dto.ClienteRequest;
import ies.belgrano.almacen.dto.ClienteResponse;
import ies.belgrano.almacen.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // LISTAR TODOS LOS CLIENTES
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos() {

        return ResponseEntity.ok(clienteService.listarTodos());
    }

    // BUSCAR CLIENTE POR ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    // BUSCAR CLIENTE POR DNI
    @GetMapping("/dni/{dni}")
    public ResponseEntity<ClienteResponse> buscarPorDni(
            @PathVariable String dni) {

        return ResponseEntity.ok(clienteService.buscarPorDni(dni));
    }

    // CREAR CLIENTE
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(
            @Valid @RequestBody ClienteRequest request) {

        ClienteResponse clienteCreado = clienteService.guardar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clienteCreado);
    }

    // ACTUALIZAR CLIENTE
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteRequest request) {

        return ResponseEntity.ok(
                clienteService.actualizar(id, request));
    }

    // ELIMINAR CLIENTE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer id) {

        clienteService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // MANEJAR ERRORES DE CLIENTES
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> manejarError(
            ResponseStatusException ex) {

        Map<String, Object> respuesta = new HashMap<>();

        respuesta.put("status", ex.getStatusCode().value());
        respuesta.put("message", ex.getReason());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(respuesta);
    }
}