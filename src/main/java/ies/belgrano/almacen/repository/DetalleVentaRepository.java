package ies.belgrano.almacen.repository;

import ies.belgrano.almacen.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleVentaRepository
        extends JpaRepository<DetalleVenta, Integer> {

    List<DetalleVenta> findByIdVenta(Integer idVenta);
}