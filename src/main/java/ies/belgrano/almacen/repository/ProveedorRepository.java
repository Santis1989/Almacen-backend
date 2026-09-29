package ies.belgrano.almacen.repository;

import ies.belgrano.almacen.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    Optional<Proveedor> findByCuit(String cuit);

    boolean existsByCuit(String cuit);

    boolean existsByCuitAndIdProveedorNot(String cuit, Integer idProveedor);

    List<Proveedor> findByActivo(Boolean activo);

    List<Proveedor> findByRazonSocialContainingIgnoreCase(String razonSocial);
}
