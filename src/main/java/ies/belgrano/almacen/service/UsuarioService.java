package ies.belgrano.almacen.service;

import java.util.List;
import org.springframework.stereotype.Service;
import ies.belgrano.almacen.model.Usuario;
import ies.belgrano.almacen.repository.UsuarioRepository;

@Service
public class UsuarioService {
	public Usuario buscarPorId(Integer id) {
	    return usuarioRepository.findByIdUsuario(id);
	}

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
