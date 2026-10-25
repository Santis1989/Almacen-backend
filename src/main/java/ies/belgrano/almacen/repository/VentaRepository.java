package ies.belgrano.almacen.repository;

import ies.belgrano.almacen.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    boolean existsByCliente_IdCliente(Integer idCliente);
}