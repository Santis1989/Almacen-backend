package ies.belgrano.almacen.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import ies.belgrano.almacen.model.Usuario;
import ies.belgrano.almacen.service.UsuarioService;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
	@GetMapping("/{id}")
	public ResponseEntity<Usuario> buscarPorId(@PathVariable Integer id) {
	    Usuario usuario = usuarioService.buscarPorId(id);

	    if (usuario == null) {
	        return ResponseEntity.notFound().build();
	    }

	    return ResponseEntity.ok(usuario);
	}

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listarTodos() {
        return usuarioService.listarTodos();
    }

    @PostMapping
    public Usuario crear(@RequestBody Usuario usuario) {
        return usuarioService.guardar(usuario);
    }
}