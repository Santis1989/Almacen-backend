package ies.belgrano.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ies.belgrano.almacen.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
	Usuario findByIdUsuario(Integer idUsuario);
}
