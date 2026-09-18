package ies.belgrano.almacen.repository;

import ies.belgrano.almacen.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

}