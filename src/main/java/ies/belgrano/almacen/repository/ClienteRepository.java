package ies.belgrano.almacen.repository;

import ies.belgrano.almacen.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdClienteNot(String dni, Integer idCliente);
}