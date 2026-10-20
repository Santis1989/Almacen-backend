
package ies.belgrano.almacen.repository;

import ies.belgrano.almacen.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleVentaRepository
        extends JpaRepository<DetalleVenta, Integer> {

    // Buscar detalles de una venta
    List<DetalleVenta> findByVenta_IdVenta(Integer idVenta);

    // Comprobar si un producto aparece en alguna venta
    boolean existsByProducto_IdProducto(Integer idProducto);
}
